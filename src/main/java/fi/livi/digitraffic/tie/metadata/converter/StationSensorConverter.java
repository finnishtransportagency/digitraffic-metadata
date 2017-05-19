package fi.livi.digitraffic.tie.metadata.converter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.metadata.dto.StationSensors;

@Component
public class StationSensorConverter {
    private final RoadStationSensorRepository roadStationSensorRepository;

    @Autowired
    public StationSensorConverter(final RoadStationSensorRepository roadStationSensorRepository) {
        this.roadStationSensorRepository = roadStationSensorRepository;
    }

    public Map<Long, List<Long>> createSensorMap(final String type) {
        final List<StationSensors> list = roadStationSensorRepository.listStationSensorsByType(type);

        return createMap(list);
    }

    public Map<Long, List<Long>> createSensorMap(final Long roadStationId, final String type) {
        return roadStationId == null ?
            Collections.emptyMap() :
            createMap(roadStationSensorRepository.getStationSensorsByIdAndType(roadStationId, type));
    }

    private static Map<Long, List<Long>> createMap(final List<StationSensors> sensors) {
        final Map<Long, List<Long>> sensorMap = new HashMap<>();

        sensors.forEach(ss -> sensorMap.put(ss.getRoadStationId(), sensorList(ss.getSensors())));

        return sensorMap;
    }

    private static List<Long> sensorList(final String sensorList) {
        return Stream.of(sensorList.split(",")).map(Long::valueOf).collect(Collectors.toList());
    }

}
