package fi.livi.digitraffic.tie.conf;

import static fi.livi.digitraffic.tie.service.v1.MqttRelayService.StatisticsType.TMS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.service.LockingService;
import fi.livi.digitraffic.tie.service.v1.MqttRelayService;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;

@ConditionalOnProperty("mqtt.tms.enabled")
@ConditionalOnNotWebApplication
@Component
public class TmsMqttConfiguration extends AbstractMqttSensorConfiguration {
    // tms/{roadStationId}/{sensorId}
    private static final String TMS_TOPIC = "tms/%d/%d";
    private static final String TMS_STATUS_TOPIC = "tms/status";

    @Autowired
    public TmsMqttConfiguration(final MqttRelayService mqttRelay,
                                final RoadStationSensorService roadStationSensorService,
                                final ObjectMapper objectMapper,
                                final LockingService lockingService) {

        super(LoggerFactory.getLogger(TmsMqttConfiguration.class),
              mqttRelay, roadStationSensorService, objectMapper, RoadStationType.TMS_STATION, TMS_TOPIC, TMS_STATUS_TOPIC, lockingService, TMS);
    }

    @Scheduled(fixedDelayString = "${mqtt.tms.pollingIntervalMs}")
    public void pollAndSendMessages() {
        try {
            super.pollAndSendMessages();;
        } catch(final Exception e) {
            log.error("Polling failed", e);
        }
    }
}
