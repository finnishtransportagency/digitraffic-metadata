package fi.livi.digitraffic.tie.conf;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.v1.MqttRelayQueue;
import fi.livi.digitraffic.tie.service.v1.MqttRelayQueue.StatisticsType;

public abstract class AbstractMqttSensorConfiguration extends AbstractMqttConfiguration {

    private final RoadStationSensorService roadStationSensorService;
    private final RoadStationType roadStationType;

    public AbstractMqttSensorConfiguration(final Logger log,
                                           final MqttRelayQueue mqttRelay,
                                           final RoadStationSensorService roadStationSensorService,
                                           final ObjectMapper objectMapper,
                                           final RoadStationType roadStationType,
                                           final String messageTopic,
                                           final String statusTopic,
                                           final StatisticsType statisticsType,
                                           final ClusteredLocker clusteredLocker) {
        super(log, mqttRelay, objectMapper, messageTopic, statusTopic, statisticsType, clusteredLocker);
        this.roadStationSensorService = roadStationSensorService;
        this.roadStationType = roadStationType;

        setLastUpdated(roadStationSensorService.getLatestSensorValueUpdatedTime(roadStationType));
    }

    /**
     * Call this from @Scheduled etc. scheduler to poll new messages and send them to MQTT.
     * Don't call concurrently from same instance.
     */
    public void pollAndSendMessages() {
        final List<DataMessage> messages = fetchMessagesToSend();
        log.debug("method=pollAndSendMessages polled {} messages to send", messages.size());

        sendMqttMessages(messages);
    }

    /**
     * Fetch all messages to be send to MQTT
     * @return messages to be send to MQTT
     */
    protected List<DataMessage> fetchMessagesToSend() {
        final List<SensorValueDto> sensorValues =
            roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(getLastUpdated(), roadStationType);

        return sensorValues.stream().map(sv ->
            new DataMessage(DateHelper.getNewestAtUtc(getLastUpdated(), sv.getUpdatedTime()),
                            getTopic(sv.getRoadStationNaturalId(), sv.getSensorNaturalId()),
                            sv))
            .collect(Collectors.toList());
    }
}
