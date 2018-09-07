package fi.livi.digitraffic.tie.conf;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;

import fi.livi.digitraffic.tie.data.service.MqttDataStatistics;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

@ConditionalOnProperty("mqtt.tms.enabled")
@Component
public class TmsMqttConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(TmsMqttConfiguration.class);

    @Lazy // this will not be available if mqtt is not enabled
    private final MqttConfig.MqttGateway tmsGateway;
    private final RoadStationSensorService roadStationSensorService;
    private final ObjectMapper objectMapper;

    // tms/{roadStationId}/{sensorId}
    private final String TMS_TOPIC = "tms/%d/%d";
    private final String TMS_STATUS_TOPIC = "tms/status";

    private ZonedDateTime lastUpdated = null;
    private int counter = 0;
    private int emptyDataCounter = 0;

    private final String statusOK = "{\"status\": \"OK\"}";
    private final String statusNOCONTENT = "{\"status\": \"no content\"}";

    @Autowired
    public TmsMqttConfiguration(final MqttConfig.MqttGateway tmsGateway,
                                final RoadStationSensorService roadStationSensorService,
                                final ObjectMapper objectMapper) {
        this.tmsGateway = tmsGateway;
        this.roadStationSensorService = roadStationSensorService;
        this.objectMapper = objectMapper;

        lastUpdated = roadStationSensorService.getSensorValueLastUpdated(RoadStationType.TMS_STATION);

        if (lastUpdated == null) {
            lastUpdated = ZonedDateTime.now();
        }
    }

    @Scheduled(fixedDelayString = "${mqtt.tms.pollingIntervalMs}")
    public void pollTmsData() {
        counter++;

        final List<SensorValueDto> data = roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(
                lastUpdated,
                RoadStationType.TMS_STATION);

        if (data.isEmpty()) {
            emptyDataCounter++;
        } else {
            emptyDataCounter = 0;
        }

        // Listeners are notified every 10th time
        if (counter >= 10) {
            try {
                sendMessage(emptyDataCounter < 10 ? statusOK : statusNOCONTENT, TMS_STATUS_TOPIC);
            } catch (Exception e) {
                logger.error("error sending status", e);
            }

            counter = 0;
        }

        final AtomicInteger messagesCount = new AtomicInteger(0);

        data.forEach(sensorValueDto -> {
            lastUpdated = DateHelper.getNewest(lastUpdated, sensorValueDto.getUpdatedTime());

            try {
                final String messageAsString = objectMapper.writeValueAsString(sensorValueDto);

                sendMessage(messageAsString, String.format(TMS_TOPIC, sensorValueDto.getRoadStationNaturalId(), sensorValueDto.getSensorNaturalId()));

                messagesCount.incrementAndGet();
            } catch (Exception e) {
                logger.error("error sending message", e);
            }
        });

        MqttDataStatistics.sentMqttStatistics(MqttDataStatistics.ConnectionType.TMS, messagesCount.get());
    }

    // This must be synchronized, because Paho does not support concurrency!
    private synchronized void sendMessage(final String payLoad, final String topic) {
        tmsGateway.sendToMqtt(topic, payLoad);
    }
}
