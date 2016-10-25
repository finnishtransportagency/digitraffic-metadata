package fi.livi.digitraffic.tie.conf;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import fi.livi.digitraffic.tie.data.controller.LamDataWebsocketEndpoint;
import fi.livi.digitraffic.tie.data.controller.LamsDataWebsocketEndpoint;
import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.data.websocket.LAMMessage;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

@ConditionalOnProperty(name = "websocket.lam.enabled")
@Configuration
public class LAMWebSocketConfiguration {

    private LocalDateTime lastUpdated = null;
    private final RoadStationSensorService roadStationSensorService;

    @Autowired
    public LAMWebSocketConfiguration(final RoadStationSensorService roadStationSensorService) {
        this.roadStationSensorService = roadStationSensorService;

        lastUpdated = roadStationSensorService.getSensorValueLastUpdated(RoadStationType.LAM_STATION);

        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
    }

    @Scheduled(fixedRateString = "${websocket.lam.pollingIntervalMs}")
    public void pollLamData() {

        List<SensorValueDto> data =
                roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(
                        lastUpdated,
                        RoadStationType.LAM_STATION);

        // Single LAM Station listeners are notified every time
        LamDataWebsocketEndpoint.sendStatus();
        if (data.isEmpty()) {
            LamsDataWebsocketEndpoint.sendStatus();
        }
        for (SensorValueDto sensorValue : data) {
            lastUpdated = DateHelper.getNewest(lastUpdated, sensorValue.getUpdated());
            final LAMMessage message = new LAMMessage(sensorValue);
            LamsDataWebsocketEndpoint.sendMessage(message);
            LamDataWebsocketEndpoint.sendMessage(message);
        }

    }
}
