package fi.livi.digitraffic.tie.conf;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;
import javax.jms.Connection;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.conf.exception.JMSInitException;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import progress.message.jclient.ErrorCodes;
import progress.message.jclient.QueueConnectionFactory;

public abstract class AbstractJMSConfiguration {

    protected static final Logger log = LoggerFactory.getLogger(AbstractJMSConfiguration.class);
    protected final ConfigurableApplicationContext applicationContext;
    private final int jmsReconnectionDelayInSeconds;
    private final int jmsReconnectionTries;
    private AtomicBoolean shutdownCalled = new AtomicBoolean(false);
    private JMSExceptionListener currentJmsExceptionListener;

    public AbstractJMSConfiguration(final ConfigurableApplicationContext applicationContext,
                                    final int jmsReconnectionDelayInSeconds,
                                    final int jmsReconnectionTries) {
        Assert.notNull(applicationContext);
        Assert.notNull(jmsReconnectionDelayInSeconds);
        Assert.notNull(jmsReconnectionTries);
        this.applicationContext = applicationContext;
        this.jmsReconnectionDelayInSeconds = jmsReconnectionDelayInSeconds;
        this.jmsReconnectionTries = jmsReconnectionTries;
    }

    @PreDestroy
    public void onShutdown() {
        log.info("Shutdown " + getClass().getSimpleName());

        shutdownCalled.set(true);
        if (currentJmsExceptionListener != null) {
            try {
                log.info("Closing JMS connection for " + currentJmsExceptionListener.getJmsParameters().getDestinationBeanName());
                QueueConnection connection = currentJmsExceptionListener.getConnection();
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
                log.error("Error while closing JMS connection", e);
            }
        }
    }

    public abstract Destination createJMSDestinationBean(final String jmsInQueue) throws JMSException;
    public abstract MessageListener createJMSMessageListener(final int pollingInterval) throws JAXBException;
    public abstract JMSParameters createJMSParameters(String jmsUserId, String jmsPassword);
    public abstract Connection createJmsConnection();

    @Bean(name = "jmsQueueConnectionFactory")
    public QueueConnectionFactory queueConnectionFactory(@Value("${jms.connectionUrls}")
                                                         final String jmsConnectionUrls) throws JMSException {
        QueueConnectionFactory connectionFactory = new QueueConnectionFactory(jmsConnectionUrls);
        connectionFactory.setSequential(true);
        connectionFactory.setFaultTolerant(true);
        return connectionFactory;
    }

    protected QueueConnection startMessagelistener(final JMSParameters jmsParameters) throws JMSException, JAXBException {
        if (!shutdownCalled.get()) {
            log.info("Start Messagelistener with parameters: " + jmsParameters);
            QueueConnectionFactory connectionFactory = applicationContext.getBean(QueueConnectionFactory.class);
            Destination destination = applicationContext.getBean(jmsParameters.getDestinationBeanName(), Destination.class);
            MessageListener jmsMessageListener = applicationContext.getBean(jmsParameters.getMessageListenerBeanName(), MessageListener.class);

            QueueConnection connection = connectionFactory.createQueueConnection(jmsParameters.getJmsUserId(), jmsParameters.getJmsPassword());
            JMSExceptionListener jmsExceptionListener =
                    new JMSExceptionListener(connection,
                            jmsParameters);
            connection.setExceptionListener(jmsExceptionListener);

            log.info("Connection created for " + jmsParameters.getMessageListenerBeanName() + ": " + connectionFactory.toString());
            log.info("Jms connection urls: " + connectionFactory.getConnectionURLs());
            ConnectionMetaData meta = connection.getMetaData();
            log.info("Sonic version : " + meta.getJMSProviderName() + " " + meta.getProviderVersion());
            if (meta.getProviderMajorVersion() < 8 || meta.getProviderMinorVersion() < 6) {
                throw new JMSInitException("Sonic JMS library version is too old. Should bee >= 8.6.0. Was " + meta.getProviderVersion() + ".");
            }

            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            final MessageConsumer consumer = session.createConsumer(destination);

            consumer.setMessageListener(jmsMessageListener);
            log.info("Listener " + jmsParameters.getMessageListenerBeanName() + " activated");

            this.currentJmsExceptionListener = jmsExceptionListener;
            connection.start();
            log.info("Connection for " + jmsParameters.getMessageListenerBeanName() + " started");
            return connection;
        } else {
            log.info("Not starting connection because shutdown has been called");
            return null;
        }
    }

    private class JMSExceptionListener implements ExceptionListener {

        private QueueConnection connection;
        private final JMSParameters jmsParameters;

        public JMSExceptionListener(final QueueConnection connection, final JMSParameters jmsParameters) {
            this.connection = connection;
            this.jmsParameters = jmsParameters;
        }

        public QueueConnection getConnection() {
            return connection;
        }

        public JMSParameters getJmsParameters() {
            return jmsParameters;
        }

        @Override
        public void onException(final JMSException jsme) {

            log.error("JMSException: errorCode: " + resolveErrorByCode(jsme.getErrorCode()) + " for " + jmsParameters.getMessageListenerBeanName(), jsme);

            int triesLeft = jmsReconnectionTries;

            while (triesLeft > 0 && !shutdownCalled.get()) {
                // If connection was dropped try to reconnect
                // NOTE: the test is against Progress SonicMQ error codes.
                // progress.message.jclient.ErrorCodes.ERR_CONNECTION_DROPPED = -5

                // Always try to disconnect old connection and then reconnect
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Exception e) {
                    log.error("Connection closing error", e);
                }
                connection = null;

                log.info("Try to reconnect... (tries left " + triesLeft + ")");
                triesLeft--;
                boolean success = tryToReconnect(jmsParameters, triesLeft);
                if (success) {
                    triesLeft = 0;
                }
            }
            if (triesLeft > 0 && shutdownCalled.get()) {
                log.info("Shutdown " + jmsParameters.getMessageListenerBeanName() + " " + getClass().getSimpleName());
            }
        }
    }

    protected boolean tryToReconnect(JMSParameters jmsParameters, final int triesLeft) {
        try {
            startMessagelistener(jmsParameters);
            log.info("Reconnect success " + jmsParameters.getMessageListenerBeanName());
            return true;
        } catch (Exception ex) {
            log.error("Reconnect failed (tries left " + triesLeft + ", trying again in " + jmsReconnectionDelayInSeconds + " seconds)", ex);
            if (triesLeft > 0 && !shutdownCalled.get()) {
                try {
                    Thread.sleep((long)jmsReconnectionDelayInSeconds * 1000);
                } catch (InterruptedException ignore) {
                    log.debug("Interrupted " + jmsParameters.getMessageListenerBeanName(), ignore);
                }
            } else {
                log.error("Reconnect failed, no tries left. Shutting down application.");
                // If reconnection fails too many times shut down whole application
                applicationContext.close();
            }
        }
        return false;
    }

    private static String resolveErrorByCode(String errCode) {
        Optional<Field> errorField = Arrays.stream(ErrorCodes.class.getDeclaredFields())
                .filter(field -> {
                    try {
                        if (Modifier.isStatic(field.getModifiers())) {
                            Object value = FieldUtils.readDeclaredStaticField(ErrorCodes.class, field.getName());
                            return value != null && StringUtils.equals(errCode, "" + value);
                        }
                    } catch (Exception e) {
                        return false;
                    }
                    return false;
                })
                .findFirst();
        if (errorField.isPresent()) {
            return ErrorCodes.class.getSimpleName() + "." + errorField.get().getName();
        }
        return null;
    }

    protected class JMSParameters {

        private final String destinationBeanName;
        private final String messageListenerBeanName;
        private final String jmsUserId;
        private final String jmsPassword;

        public JMSParameters(String destinationBeanName, String messageListenerBeanName, String jmsUserId, String jmsPassword) {
            this.destinationBeanName = destinationBeanName;
            this.messageListenerBeanName = messageListenerBeanName;
            this.jmsUserId = jmsUserId;
            this.jmsPassword = jmsPassword;
        }

        public String getDestinationBeanName() {
            return destinationBeanName;
        }

        public String getMessageListenerBeanName() {
            return messageListenerBeanName;
        }

        public String getJmsPassword() {
            return jmsPassword;
        }

        public String getJmsUserId() {
            return jmsUserId;
        }

        @Override
        public String toString() {
            return ToStringHelpper.toStringFull(this, "jmsPassword");
        }
    }
}
