package fi.livi.digitraffic.tie.service.v1;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.LongAccumulator;

import org.apache.commons.lang3.tuple.Pair;
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
    private final BlockingQueue<Pair<String, String>> messageList = new LinkedBlockingQueue<>();
    private final LongAccumulator maxQueueLength = new LongAccumulator(Long::max, 0L);

    public enum StatisticsType {TMS, WEATHER}

    @Autowired
    public MqttRelayService(final MqttConfig.MqttGateway mqttGateway) {
        // in a threadsafe way, take messages from lessagelist and send them to mqtt gateway
        new Thread(() -> {
            while(true) {
                try {
                    final Pair<String, String> pair = messageList.take();

                    mqttGateway.sendToMqtt(pair.getLeft(), pair.getRight());
                } catch (final Exception e) {
                    logger.error("mqtt failure", e);
                }
            }
        }).start();
    }

    @Scheduled(fixedRate = 60000)
    private void logMqttQueue() {
        logger.info("MqttQueueLength={}", maxQueueLength.getThenReset());
    }

    /**
     * Add mqtt message to messagelist.  Messagelist is synchronized and threadsafe.
     * @param topic
     * @param payLoad
     */
    public void sendMqttMessage(final String topic, final String payLoad) {
        messageList.add(Pair.of(topic, payLoad));

        maxQueueLength.accumulate(messageList.size());
    }

    /**
     * Update send messages statistics
     * @param type
     * @param messages
     */
    public synchronized void sentMqttStatistics(final StatisticsType type, final int messages) {
        if (sentStatisticsMap.containsKey(type)) {
            sentStatisticsMap.put(type, sentStatisticsMap.get(type) + messages);
        } else {
            sentStatisticsMap.put(type, messages);
        }
    }

    @Scheduled(fixedRate = 60000)
    private void logMessageCount() {
        for (final StatisticsType type : StatisticsType.values()) {
            final Integer sentMessages = sentStatisticsMap.get(type);

            logger.info("Sent mqtt statistics for type={} messages={}", type, sentMessages != null ? sentMessages : 0);

            sentStatisticsMap.put(type, 0);
        }
    }
}
