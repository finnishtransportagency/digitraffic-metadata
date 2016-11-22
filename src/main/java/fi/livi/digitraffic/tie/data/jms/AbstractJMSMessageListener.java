package fi.livi.digitraffic.tie.data.jms;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;

public abstract class AbstractJMSMessageListener<T> implements MessageListener {

    private static final int MAX_NORMAL_QUEUE_SIZE = 100;
    private static final int QUEUE_SIZE_WARNING_LIMIT = 2 * MAX_NORMAL_QUEUE_SIZE;
    private static final int QUEUE_SIZE_ERROR_LIMIT = 10 * MAX_NORMAL_QUEUE_SIZE;

    private final JAXBContext jaxbContext;
    private Logger log;

    protected final Unmarshaller jaxbUnmarshaller;
    private final BlockingQueue<Pair<T,String>> blockingQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);
    private boolean drainScheduled = true;

    public AbstractJMSMessageListener(final Class<T> typeClass,
                                      Logger log) throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(typeClass);
        this.log = log;
        this.jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        log.info("Initialized JMSMessageListener");
    }

    /**
     * Implement to handle received message data
     */
    protected abstract void handleData(List<Pair<T,String>> data);

    @PreDestroy
    public void onShutdown() {
        log.info("Shutdown ... ");
        shutdownCalled.set(true);
    }

    @Override
    public void onMessage(Message message) {
        if (!shutdownCalled.get()) {
            log.info("Received " + message.getClass().getSimpleName());
            Pair<T,String> data = unmarshalMessage(message);
            if (data != null) {
                blockingQueue.add(data);

                // if queue (= not topic) handle it immediately and acknowledge the handling of the message after successful saving to db.
                if (!drainScheduled) {
                    log.info("Handle JMS message immediately");
                    drainQueue();
                    try {
                        message.acknowledge();
                    } catch (JMSException e) {
                        log.error("JMS message acknowledge failed", e);
                    }
                }

            }
        } else {
            log.error("Not handling any messages anymore because app is shutting down");
        }
    }

    protected Pair<T, String> unmarshalMessage(Message message) {

        if (message instanceof TextMessage) {
            TextMessage xmlMessage = (TextMessage) message;
            try {
                String text = xmlMessage.getText().trim();
                if (text.length() <= 0) {
                    log.warn("Empty JMS message" + ToStringHelpper.toStringFull(message));
                    return null;
                }

                StringReader sr = new StringReader(text);
                Object object = jaxbUnmarshaller.unmarshal(sr);
                if (object instanceof JAXBElement) {
                    object = ((JAXBElement) object).getValue();
                }
                return Pair.of((T)object, text);
            } catch (JMSException jmse) {
                // getText() failed
                log.error("Message unmarshalling error for message: " + ToStringHelpper.toStringFull(message));
                throw new JMSUnmarshalMessageException("Message unmarshalling error", jmse);
            } catch (JAXBException e) {
                try {
                    log.error("Message unmarshalling error for message:\n" + xmlMessage.getText());
                } catch (JMSException e1) {
                    log.debug("Message unmarshalling error", e);
                }
                throw new JMSUnmarshalMessageException("Message unmarshalling error", e);
            }
        } else {
            log.error("Message unmarshalling error for message:" + ToStringHelpper.toStringFull(message));
            throw new IllegalArgumentException("Unknown message type: " + message.getClass());
        }
    }

    /**
     * Drain queue and calls handleData if data available.
     */
    @Scheduled(fixedRateString = "${jms.queue.pollingIntervalMs}")
    public void drainQueueScheduled() {
        if (drainScheduled) {
            drainQueue();
        }
    }

    private void drainQueue() {
        if ( !shutdownCalled.get() ) {
            long start = System.currentTimeMillis();

            if (blockingQueue.size() > QUEUE_SIZE_ERROR_LIMIT) {
                log.error("JMS message queue size " + blockingQueue.size() + " exceeds error limit " + QUEUE_SIZE_ERROR_LIMIT);
            } else if (blockingQueue.size() > QUEUE_SIZE_WARNING_LIMIT) {
                log.warn("JMS message queue size " + blockingQueue.size() + " exceeds warning limit " + QUEUE_SIZE_WARNING_LIMIT);
            }

            // Allocate array with some extra because queue size can change any time
            ArrayList<Pair<T, String>> targetList = new ArrayList<>(blockingQueue.size() + 5);
            int drained = blockingQueue.drainTo(targetList);
            if ( drained > 0 && !shutdownCalled.get() ) {
                log.info("Handle data");
                handleData(targetList);
                long took = System.currentTimeMillis() - start;
                log.info("DrainQueue of size " + drained + " took " + took + " ms");
            }
        }
    }

    /**
     * Default is true. If set to false messages will be handled immediately and
     * message will be notified of successful handling.
     * @param drainScheluled
     */
    public void setDrainScheduled(boolean drainScheluled) {
        this.drainScheduled = drainScheluled;
    }
}
