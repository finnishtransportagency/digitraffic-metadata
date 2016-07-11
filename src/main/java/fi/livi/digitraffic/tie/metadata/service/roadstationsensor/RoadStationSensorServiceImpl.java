package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dto.RoadStationSensorValueDto;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorValueDtoRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadWeatherStationRepository;
import fi.livi.digitraffic.tie.metadata.dto.RoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

@Service
public class RoadStationSensorServiceImpl implements RoadStationSensorService {

    private final RoadStationSensorValueDtoRepository roadStationSensorValueDtoRepository;
    private RoadStationSensorRepository roadStationSensorRepository;

    private final RoadWeatherStationRepository roadWeatherStationRepository;
    private final int roadWeatherStationSensorValueTimeLimitInMins;
    private final ArrayList<Long> includedSensorNaturalIds;

    @Autowired
    public RoadStationSensorServiceImpl(final RoadStationSensorValueDtoRepository roadStationSensorValueDtoRepository,
                                        final RoadStationSensorRepository roadStationSensorRepository,
                                        final RoadWeatherStationRepository roadWeatherStationRepository,
                                        @Value("${roadWeatherStation.sensorValue.timeLimitInMinutes}")
                                        final int roadWeatherStationSensorValueTimeLimitInMins,
                                        @Value("${roadWeatherStation.includedSensorNaturalIds}")
                                        final String includedSensorNaturalIdsStr) {
        this.roadStationSensorValueDtoRepository = roadStationSensorValueDtoRepository;
        this.roadStationSensorRepository = roadStationSensorRepository;
        this.roadWeatherStationRepository = roadWeatherStationRepository;
        this.roadWeatherStationSensorValueTimeLimitInMins = roadWeatherStationSensorValueTimeLimitInMins;

        String[] ids = StringUtils.splitPreserveAllTokens(includedSensorNaturalIdsStr, ',');
        includedSensorNaturalIds = new ArrayList<>();
        for (String id : ids) {
            includedSensorNaturalIds.add(Long.parseLong(id.trim()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoadStationSensor> findAllNonObsoleteRoadStationSensors() {
        return roadStationSensorRepository.findNonObsoleteRoadStationSensors();
    }

    @Override
    @Transactional(readOnly = true)
    public RoadStationsSensorsMetadata findRoadStationsSensorsMetadata() {
        return new RoadStationsSensorsMetadata(findAllNonObsoleteRoadStationSensors());
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, List<RoadStationSensorValueDto>> findAllNonObsoletePublicRoadWeatherStationSensorValues() {

        List<Long> stations =
                roadWeatherStationRepository.findNonObsoleteAndPublicRoadStationNaturalIds();
        Set<Long> allowedRoadStations =
                stations.stream().collect(Collectors.toSet());

        Map<Long, List<RoadStationSensorValueDto>> rsNaturalIdToRsSensorValues = new HashMap<>();
        List<RoadStationSensorValueDto> sensors =
                roadStationSensorValueDtoRepository.findAllNonObsoleteRoadStationSensorValues(
                        RoadStationType.WEATHER_STATION.getTypeNumber(),
                        roadWeatherStationSensorValueTimeLimitInMins,
                        includedSensorNaturalIds);
        for (RoadStationSensorValueDto sensor : sensors) {
            if (allowedRoadStations.contains(sensor.getRoadStationNaturalId())) {
                List<RoadStationSensorValueDto> values = rsNaturalIdToRsSensorValues.get(Long.valueOf(sensor.getRoadStationNaturalId()));
                if (values == null) {
                    values = new ArrayList<>();
                    rsNaturalIdToRsSensorValues.put(sensor.getRoadStationNaturalId(), values);
                }
                values.add(sensor);
            }
        }
        return rsNaturalIdToRsSensorValues;
    }

    @Transactional(readOnly = true)
    @Override
    public LocalDateTime getLatestMeasurementTime() {
        return roadStationSensorValueDtoRepository.getLatestMeasurementTime(
                RoadStationType.WEATHER_STATION.getTypeNumber(),
                roadWeatherStationSensorValueTimeLimitInMins,
                includedSensorNaturalIds);
    }
}
