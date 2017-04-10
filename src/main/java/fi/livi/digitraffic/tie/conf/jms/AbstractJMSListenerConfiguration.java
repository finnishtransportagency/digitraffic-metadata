package fi.livi.digitraffic.tie.conf.jms;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

import fi.livi.digitraffic.tie.data.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import progress.message.jclient.Connection;
import progress.message.jclient.Queue;
import progress.message.jclient.QueueConnectionFactory;
import progress.message.jclient.Topic;

public abstract class AbstractJMSListenerConfiguration<T> {

    protected static final int JMS_CONNECTION_LOCK_EXPIRATION_S = 10;

    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);
    private final AtomicInteger lockAcquiredCounter = new AtomicInteger();
    private final AtomicInteger lockNotAcquiredCounter = new AtomicInteger();

    private final QueueConnectionFactory connectionFactory;
    private final LockingService lockingService;
    private final Logger log;
    private QueueConnection connection;
    private JMSMessageListener<T> messageListener;

    public AbstractJMSListenerConfiguration(QueueConnectionFactory connectionFactory,
                                            final LockingService lockingService,
                                            Logger log) {
        this.connectionFactory = connectionFactory;
        this.lockingService = lockingService;
        this.log = log;
        log.info("Init JMS configuration");
    }

    public abstract JMSParameters getJmsParameters();

    protected abstract JMSMessageListener<T> createJMSMessageListener() throws JAXBException;

    private JMSMessageListener<T> getJMSMessageListener() throws JAXBException {
        if (messageListener == null) {
            messageListener = createJMSMessageListener();
        }
        return messageListener;
    }

    @PreDestroy
    public void onShutdown() {
        log.info("Shutdown...");
        shutdownCalled.set(true);
        log.info("Closing JMS connection");
        closeConnectionQuietly(connection);
        connection = null;
    }

    /** Log statistics once in minute */
    @Scheduled(fixedRate = 60 * 1000, initialDelay = 60 * 1000)
    public void logMessagesReceived() throws JAXBException {
        int messagesPerMinute = getJMSMessageListener().getAndResetMessageCounter();
        int lockedPerMinute = lockAcquiredCounter.getAndSet(0);
        int notLockedPerMinute = lockNotAcquiredCounter.getAndSet(0);
        log.info("STATISTICS: Received {} messages per minute", messagesPerMinute);
        log.info("STATISTICS: MessageListener lock acquired {} and not acquired {} times per minute for {} (instanceId: {})",
                lockedPerMinute, notLockedPerMinute, getJmsParameters().getLockInstanceName(), getJmsParameters().getLockInstanceId());
    }

    /**
     * Drain queue and calls handleData if data available.
     */
    @Scheduled(fixedRateString = "${jms.queue.pollingIntervalMs}")
    public void drainQueueScheduled() throws JAXBException {
        getJMSMessageListener().drainQueueScheduled();
    }


    /**
     * Checks if connection can be created and starts
     * listening JMS-messages if lock is acquired for this
     * thread
     */
    @Scheduled(fixedRateString = "${jms.connection.intervalMs}")
    public void connectAndListen() throws JMSException, JAXBException {

        if (shutdownCalled.get()) {
            return;
        }

        // Try to connect if not connected and not shutting down
        if (connection == null) {
            connection = createConnection(getJmsParameters(), connectionFactory);
        }

        JMSParameters jmsParameters = getJmsParameters();
        try {

            // If lock can be acquired then start listening
            boolean lockAcquired = lockingService.acquireLock(getJmsParameters().getLockInstanceName(),
                    getJmsParameters().getLockInstanceId(),
                    JMS_CONNECTION_LOCK_EXPIRATION_S);
            // If acquired lock then start listening otherwise stop listening
            if (lockAcquired && !shutdownCalled.get()) {
                lockAcquiredCounter.incrementAndGet();
                log.debug("MessageListener lock acquired for " + jmsParameters.getLockInstanceName() +
                          " (instanceId: " + jmsParameters.getLockInstanceId() + ")");
                // Calling start multiple times is safe
                connection.start();
            } else {
                lockNotAcquiredCounter.incrementAndGet();
                log.debug("MessageListener lock not acquired for " + jmsParameters.getLockInstanceName() +
                          " (instanceId: " + jmsParameters.getLockInstanceId() + "), another instance is holding the lock");
                // Calling stop multiple times is safe
                connection.stop();
            }
        } catch (Exception e) {
            log.error("Error in connectAndListen", e);
            closeConnectionQuietly(connection);
            connection = null;
        }

        // Check if shutdown was called during connection initialization
        if (shutdownCalled.get() && connection != null) {
            closeConnectionQuietly(connection);
            connection = null;
        }
    }

    protected QueueConnection createConnection(JMSParameters jmsParameters,
                                               QueueConnectionFactory connectionFactory) throws JMSException, JAXBException {

        log.info("Create JMS connection with parameters: " + jmsParameters);

        try {
            QueueConnection queueConnection = connectionFactory.createQueueConnection(
                    jmsParameters.getJmsUserId(), jmsParameters.getJmsPassword());
            JMSExceptionListener jmsExceptionListener =
                    new JMSExceptionListener(queueConnection, jmsParameters);
            queueConnection.setExceptionListener(jmsExceptionListener);
            Connection sonicCon = (Connection) queueConnection;
            log.info("Connection created: " + connectionFactory.toString());
            log.info("Jms connection url " + sonicCon.getBrokerURL() + ", connection fault tolerant: " + sonicCon.isFaultTolerant() +
                    ", broker urls: " + connectionFactory.getConnectionURLs());
            ConnectionMetaData meta = queueConnection.getMetaData();
            log.info("Sonic version : " + meta.getJMSProviderName() + " " + meta.getProviderVersion());
            // Reguire at least Sonic 8.6
            if (meta.getProviderMajorVersion() < 8 || (meta.getProviderMajorVersion() == 8 && meta.getProviderMinorVersion() < 6)) {
                throw new JMSInitException("Sonic JMS library version is too old. Should bee >= 8.6.0. Was " + meta.getProviderVersion() + ".");
            }

            createSessionAndConsumer(jmsParameters.getJmsQueueKey(), queueConnection);

            log.info("Connection initialized");

            return queueConnection;
        } catch (Exception e) {
            log.error("Connection initialization failed", e);
            closeConnectionQuietly(connection);
            throw e;
        }
    }

    private Session createSessionAndConsumer(String jmsQueueKey, QueueConnection queueConnection) throws JMSException, JAXBException {
        boolean drainScheduled = isQueueTopic(jmsQueueKey);
        Session session = drainScheduled ?
                          queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE) : // ACKNOWLEDGE automatically when message received
                          queueConnection.createSession(false,
                                  progress.message.jclient.Session.SINGLE_MESSAGE_ACKNOWLEDGE); // ACKNOWLEDGE after successful handling

        final MessageConsumer consumer = session.createConsumer(createDestination(jmsQueueKey));
        consumer.setMessageListener(getJMSMessageListener());
        return session;
    }

    private void closeConnectionQuietly(QueueConnection queueConnection) {
        if (queueConnection != null) {
            try {
                queueConnection.close();
            } catch (JMSException e) {
                log.debug("Closing connection failed", e);
            }
        }
    }

    /*
     * If queue is a TOPIC then handling must be done as quickly as possible because otherwice
     * topic will jam for all users/listeners.
     * In case of QUEUE it is private queue and notification of handling should be sent after successful
     * handling of message ie. after saving to db. After that it will be removed from server.
     */
    public static boolean isQueueTopic(String jmsQueueKey) {
        return jmsQueueKey.startsWith("topic://");
    }

    protected Destination createDestination(String jmsQueueKey) throws JMSException {
        boolean topic = isQueueTopic(jmsQueueKey);
        String jmsQueue = jmsQueueKey.replaceFirst(".*://", "");
        return topic ? new Topic(jmsQueue) : new Queue(jmsQueue);
    }

    public class JMSExceptionListener implements ExceptionListener {

        private QueueConnection connection;
        private final JMSParameters jmsParameters;

        public JMSExceptionListener(final QueueConnection connection,
                                    final JMSParameters jmsParameters) {
            this.connection = connection;
            this.jmsParameters = jmsParameters;
        }

        @Override
        public void onException(final JMSException jsme) {
            log.error("JMSException: errorCode: " + JMSErrorResolver.resolveErrorMessageByErrorCode(jsme.getErrorCode()) + " for " + jmsParameters.getLockInstanceName(), jsme);
            // Always try to disconnect old connection and then reconnect
            closeConnectionQuietly(connection);
            connection = null;
        }
    }

    protected class JMSParameters {

        private final String jmsUserId;
        private final String jmsPassword;
        private final String lockInstanceId;
        private final String jmsQueueKey;
        private final String lockInstanceName;

        public JMSParameters(String jmsQueueKey,
                             String jmsUserId,
                             String jmsPassword,
                             String lockInstanceName,
                             String lockInstanceId) {
            this.jmsQueueKey = jmsQueueKey;
            this.jmsUserId = jmsUserId;
            this.jmsPassword = jmsPassword;
            this.lockInstanceName = lockInstanceName;
            this.lockInstanceId = lockInstanceId;
        }

        public String getJmsPassword() {
            return jmsPassword;
        }

        public String getJmsUserId() {
            return jmsUserId;
        }

        public String getLockInstanceId() {
            return lockInstanceId;
        }

        @Override
        public String toString() {
            return ToStringHelper.toStringFull(this, "jmsPassword");
        }

        public String getJmsQueueKey() {
            return jmsQueueKey;
        }

        public String getLockInstanceName() {
            return lockInstanceName;
        }
    }
}
