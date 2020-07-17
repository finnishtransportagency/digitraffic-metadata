package fi.livi.digitraffic.tie.service.v1;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.LongAccumulator;

import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.conf.MqttConfig;

@Component
@ConditionalOnExpression("'${app.type}' == 'daemon' and '${config.test}' != 'true'")
public class MqttRelayService {
    private static final Logger logger = LoggerFactory.getLogger(MqttRelayService.class);

    private static final Map<StatisticsType, Integer> sentStatisticsMap = new ConcurrentHashMap<>();
    private static final Map<StatisticsType, Integer> sendErrorStatisticsMap = new ConcurrentHashMap<>();
    private final BlockingQueue<Triple<String, String, StatisticsType>> messageList = new LinkedBlockingQueue<>();
    private final LongAccumulator maxQueueLength = new LongAccumulator(Long::max, 0L);

    public enum StatisticsType {TMS, WEATHER, MAINTENANCE_TRACKING, STATUS}

    @Autowired
    public MqttRelayService(final MqttConfig.MqttGateway mqttGateway) {

        for (final StatisticsType type : StatisticsType.values()) {
            sentStatisticsMap.put(type, 0);
            sendErrorStatisticsMap.put(type, 0);
        }

        // in a threadsafe way, take messages from message list and send them to mqtt gateway
        new Thread(() -> {
            while(true) {

                final Triple<String, String, StatisticsType> topicPayloadStatisticsType = getNextMessage();

                if (topicPayloadStatisticsType != null) {
                    try {
                        mqttGateway.sendToMqtt(topicPayloadStatisticsType.getLeft(), topicPayloadStatisticsType.getMiddle());
                        if (topicPayloadStatisticsType.getRight() != null) {
                            updateSentMqttStatistics(topicPayloadStatisticsType.getRight(), 1);
                        }
                    } catch (final Exception e) {
                        if (topicPayloadStatisticsType.getRight() != null) {
                            updateSendErrorMqttStatistics(topicPayloadStatisticsType.getRight(), 1);
                        }
                        logger.error("MqttGateway send failure", e);
                    }
                }
            }
        }).start();
    }

    private Triple<String, String, StatisticsType> getNextMessage() {
        try {
            return messageList.take();
        } catch (Exception e) {
            logger.error("Mqtt messageList.take() failed", e);
            return null;
        }
    }

    @Scheduled(fixedRate = 60000)
    public void logMqttQueue() {
        logger.info("mqttQueueLength={}", maxQueueLength.getThenReset());
    }

    /**
     * Add mqtt message to messagelist. Messagelist is synchronized and threadsafe.
     * @param topic Mqtt message topic
     * @param payLoad Mqtt message payload
     * @param statisticsType Statistics type for the message
     */
    public void queueMqttMessage(final String topic, final String payLoad, final StatisticsType statisticsType) {
        if (topic == null || payLoad == null || statisticsType == null) {
            throw new IllegalArgumentException(String.format("All parameters must be set topic:%s, payload:%s, statisticsType:%s",
                                                             topic, payLoad, statisticsType));
        }
        messageList.add(Triple.of(topic, payLoad, statisticsType));
        maxQueueLength.accumulate(messageList.size());
    }

    /**
     * Update send messages statistics
     * @param type Statistics type
     * @param messages Count of messages send
     */
    public synchronized void updateSentMqttStatistics(final StatisticsType type, final int messages) {
        sentStatisticsMap.put(type, sentStatisticsMap.get(type) + messages);
    }

    /**
     * Update sendig error statistics
     * @param type Statistics type
     * @param messages Count of messages send
     */
    public synchronized void updateSendErrorMqttStatistics(final StatisticsType type, final int messages) {
        sendErrorStatisticsMap.put(type, sendErrorStatisticsMap.get(type) + messages);
    }

    @Scheduled(fixedRate = 60000)
    public void logMessageCount() {
        for (final StatisticsType type : StatisticsType.values()) {

            final Integer sentMessages = sentStatisticsMap.put(type, 0);
            final Integer sendErrors = sendErrorStatisticsMap.put(type, 0);

            logger.info("method=logMessageCount Mqtt message statistics for type={} send messages={} errors={}", type, sentMessages != null ? sentMessages : 0, sendErrors != null ? sendErrors : 0);
        }
    }
}
