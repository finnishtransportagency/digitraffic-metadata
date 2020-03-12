package fi.livi.digitraffic.tie.service.jms;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

public class JMSMessageListener<K> implements MessageListener {
    public static final String MESSAGE_UNMARSHALLING_ERROR = "Message unmarshalling error";
    public static final String MESSAGE_UNMARSHALLING_ERROR_FOR_MESSAGE = MESSAGE_UNMARSHALLING_ERROR + " for message: {}";

    public interface JMSDataUpdater<K> {
        int updateData(final List<K> data);
    }

    public interface MessageMarshaller<K> {
        List<K> unmarshalMessage(final Message message) throws JMSException;
    }

    private static final int QUEUE_SIZE_WARNING_LIMIT = 5000;
    private static final int QUEUE_SIZE_OVERFLOW_LIMIT = 10000;

    protected final Logger log;

    private final ConcurrentLinkedQueue<K> messageQueue = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);
    private final AtomicInteger messageCounter = new AtomicInteger();
    private final AtomicInteger messageDrainedCounter = new AtomicInteger();
    private final AtomicInteger dbRowsUpdatedCounter = new AtomicInteger();

    private final boolean drainScheduled;
    private final JMSDataUpdater<K> dataUpdater;
    private final MessageMarshaller<K> messageMarshaller;

    public JMSMessageListener(final MessageMarshaller<K> messageMarshaller, final JMSDataUpdater<K> dataUpdater, final boolean drainScheduled,
        final Logger log) {
        this.messageMarshaller = messageMarshaller;
        this.dataUpdater = dataUpdater;
        this.drainScheduled = drainScheduled;
        this.log = log;
        log.info("name={} JMSMessageListener initialized with drainScheduled={}", log.getName(), drainScheduled);
    }

    public boolean isDrainScheduled() {
        return drainScheduled;
    }

    @PreDestroy
    public void onShutdown() {
        log.info("Shutdown ... ");
        shutdownCalled.set(true);
    }

    @Override
    public void onMessage(final Message message) {
        messageCounter.incrementAndGet();
        if (shutdownCalled.get()) {
            log.error("Not handling any messages anymore because app is shutting down");
            return;
        }

        final List<K> data = unmarshalMessage(message);

        if (CollectionUtils.isNotEmpty(data)) {
            messageQueue.addAll(data);

            // if queue (= not topic) handle it immediately and acknowledge the handling of the message after successful saving to db.
            if (!isDrainScheduled()) {
                drainQueueInternal();
                try {
                    message.acknowledge();
                } catch (JMSException e) {
                    log.error("JMS message acknowledge failed", e);
                }
            }
        }
    }

    private List<K> unmarshalMessage(final Message message) {
        try {
            return messageMarshaller.unmarshalMessage(message);
        } catch (final JMSException jmse) {
            // getText() failed
            log.error(MESSAGE_UNMARSHALLING_ERROR_FOR_MESSAGE, ToStringHelper.toStringFull(message));
            throw new JMSUnmarshalMessageException(MESSAGE_UNMARSHALLING_ERROR, jmse);
        }
    }

    /**
     * Drain queue and calls handleData if data available.
     */
    public void drainQueueScheduled() {
        if (isDrainScheduled()) {
            drainQueueInternal();
        }
    }

    private void drainQueueInternal() {
        if ( !shutdownCalled.get() ) {
            final StopWatch start = StopWatch.createStarted();

            int queueToDrain = messageQueue.size();
            if ( queueToDrain <= 0 ) {
                return;
            } else if (queueToDrain > QUEUE_SIZE_OVERFLOW_LIMIT) {
                log.error("JMS message queue size queueToDrainSize={} exceeds maximum size queueOverflowLimitSize={}", queueToDrain, QUEUE_SIZE_OVERFLOW_LIMIT);
                int trashed = 0;
                while (queueToDrain > QUEUE_SIZE_OVERFLOW_LIMIT) {
                    messageQueue.poll();
                    queueToDrain--;
                    trashed++;
                }
                log.warn("JMS message queue size decreased by trashedCount={} messages by trashing to size messageQueueSizeAfter={}", trashed, messageQueue.size());
            } else if (queueToDrain > QUEUE_SIZE_WARNING_LIMIT) {
                log.warn("JMS message queue size queueToDrain={} exceeds error limit queueWarningLimitSize={}", queueToDrain, QUEUE_SIZE_WARNING_LIMIT);
            } else {
                log.info("JMS message queue size queueToDrainSize={}", queueToDrain );
            }

            // Allocate array with current message queue size and drain same amount of messages
            ArrayList<K> targetList = new ArrayList<>(queueToDrain);
            int counter = 0;
            while (counter < queueToDrain) {
                final K next = messageQueue.poll();
                if (next != null) {
                    targetList.add(next);
                    counter++;
                } else {
                    log.error("Next in message queue should never be null");
                    break;
                }
            }

            if ( counter > 0 && !shutdownCalled.get() ) {
                log.info("JMS messages drainedCount={} queueToDrain={}", counter, queueToDrain);
                messageDrainedCounter.addAndGet(counter);
                final int updated = dataUpdater.updateData(targetList);
                dbRowsUpdatedCounter.addAndGet(updated);
                log.info("JMS messages updated counter={} updated={} tookMs={}", counter, updated, start.getTime());
            }
        } else {
            log.info("drainQueueInternal: Shutdown called");
        }
    }

    public JmsStatistics getAndResetMessageCounter() {
        return new JmsStatistics(messageCounter.getAndSet(0),
                                 messageDrainedCounter.getAndSet(0),
                                 dbRowsUpdatedCounter.getAndSet(0),
                                 messageQueue.size());
    }

    public class JmsStatistics {
        public final int messagesReceived;
        public final int messagesDrained;
        public final int dbRowsUpdated;
        public final int queueSize;

        public JmsStatistics(final int messagesReceived,
                             final int messagesDrained,
                             final int dbRowsUpdated,
                             final int queueSize) {
            this.messagesReceived = messagesReceived;
            this.messagesDrained = messagesDrained;
            this.dbRowsUpdated = dbRowsUpdated;
            this.queueSize = queueSize;
        }
    }
}
