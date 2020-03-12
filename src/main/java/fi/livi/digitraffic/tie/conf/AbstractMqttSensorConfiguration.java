package fi.livi.digitraffic.tie.conf;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.service.LockingService;
import fi.livi.digitraffic.tie.service.v1.MqttRelayService;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;

public abstract class AbstractMqttSensorConfiguration {
    private final MqttRelayService mqttRelay;
    private final RoadStationSensorService roadStationSensorService;
    private final ObjectMapper objectMapper;
    private final RoadStationType roadStationType;
    private final Logger logger;
    private final String statusTopic;
    private final String messageTopic;
    private final LockingService lockingService;
    private final String mqttClassName;
    private final long instanceId;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private final AtomicReference<ZonedDateTime> lastUpdated = new AtomicReference<>();
    private final AtomicReference<ZonedDateTime> lastError = new AtomicReference<>();
    private final MqttRelayService.StatisticsType statisticsType;

    public AbstractMqttSensorConfiguration(final MqttRelayService mqttRelay,
                                           final RoadStationSensorService roadStationSensorService,
                                           final ObjectMapper objectMapper,
                                           final RoadStationType roadStationType,

                                           final String statusTopic,
                                           final String messageTopic,
                                           final Logger logger,
                                           final LockingService lockingService,
                                           final String mqttClassName) {

        this.mqttRelay = mqttRelay;
        this.roadStationSensorService = roadStationSensorService;
        this.objectMapper = objectMapper;
        this.roadStationType = roadStationType;
        this.statusTopic = statusTopic;
        this.messageTopic = messageTopic;
        this.logger = logger;
        this.lockingService = lockingService;
        this.mqttClassName = mqttClassName;

        if (roadStationType == RoadStationType.TMS_STATION) {
            statisticsType = MqttRelayService.StatisticsType.TMS;
        } else {
            statisticsType = MqttRelayService.StatisticsType.WEATHER;
        }

        lastUpdated.set(Objects.requireNonNullElse(
            roadStationSensorService.getLatestSensorValueUpdatedTime(roadStationType),
            ZonedDateTime.now())
        );

        instanceId = LockingService.generateInstanceId();

        executor.scheduleAtFixedRate(this::sendStatus, 30, 10, TimeUnit.SECONDS);
    }

    // Implement this with @Scheduled
    public abstract void pollData();

    protected void handleData() {
        final boolean lockAcquired = lockingService.tryLock(mqttClassName, 60, instanceId);

        if (lockAcquired) {
            final List<SensorValueDto> data = roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(
                lastUpdated.get(),
                roadStationType);

            final AtomicInteger messagesCount = new AtomicInteger(0);

            data.forEach(sensorValueDto -> {
                lastUpdated.set(DateHelper.getNewest(lastUpdated.get(), sensorValueDto.getUpdatedTime()));

                try {
                    mqttRelay.sendMqttMessage(
                        String.format(messageTopic, sensorValueDto.getRoadStationNaturalId(), sensorValueDto.getSensorNaturalId()),
                        objectMapper.writeValueAsString(sensorValueDto));

                    messagesCount.incrementAndGet();

                    lastError.set(null);
                } catch (final Exception e) {
                    lastError.set(ZonedDateTime.now());
                    logger.error("error sending message", e);
                }
            });

            mqttRelay.sentMqttStatistics(statisticsType, messagesCount.get());
        }
    }

    private void sendStatus() {
        if(lockingService.tryLock(mqttClassName, 60, instanceId)) {
            try {
                final StatusMessage message = new StatusMessage(lastUpdated.get(), lastError.get(), "Ok", statisticsType.toString());

                mqttRelay.sendMqttMessage(statusTopic, objectMapper.writeValueAsString(message));
            } catch (final Exception e) {
                logger.error("error sending message", e);
            }
        }
    }

    protected class StatusMessage {
        private final ZonedDateTime lastUpdated;
        private final ZonedDateTime lastError;
        private final String status;
        private final String type;

        public StatusMessage(final ZonedDateTime lastUpdated, final ZonedDateTime lastError, final String status, final String type) {
            this.lastUpdated = lastUpdated;
            this.lastError = lastError;
            this.status = status;
            this.type = type;
        }

        public ZonedDateTime getLastUpdated() {
            return lastUpdated;
        }

        public ZonedDateTime getLastError() {
            return lastError;
        }

        public String getStatus() {
            return status;
        }

        public String getType() {
            return type;
        }
    }
}
