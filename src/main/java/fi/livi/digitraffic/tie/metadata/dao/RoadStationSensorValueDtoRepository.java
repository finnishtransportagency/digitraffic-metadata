package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fi.livi.digitraffic.tie.data.model.dto.RoadStationSensorValueDto;

public interface RoadStationSensorValueDtoRepository extends JpaRepository<RoadStationSensorValueDto, Long> {

    @Query(value =
            "select rs.natural_id road_station_natural_id\n" +
            "     , rs.id road_station_id\n" +
            "     , s.natural_id sensor_natural_id\n" +
            "     , s.id sensor_id\n" +
            "     , sv.id sensor_value_id\n" +
            "     , sv.value sensor_value\n" +
            "     , sv.measured sensor_value_measured\n" +
            "     , s.name sensor_name\n" +
            "     , s.unit sensor_unit\n" +
            "from road_station rs\n" +
            "inner join sensor_value sv on sv.road_station_id = rs.id\n" +
            "inner join road_station_sensor s on sv.road_station_sensor_id = s.id\n" +
            "where rs.type = :sensorTypeId\n" +
            "  and rs.obsolete = 0\n" +
            "  and s.obsolete = 0\n" +
            "  and sv.measured > (\n" +
            "    select max(measured) - NUMTODSINTERVAL(:timeLimitInMinutes, 'MINUTE')\n" +
            "    from sensor_value sensv\n" +
            "    where sensv.road_station_id = sv.road_station_id\n" +
            "  )\n" +
            "  and s.natural_id in ( :includedSensorNaturalIds ) \n" +
            "order by rs.natural_id, s.natural_id",
           nativeQuery = true)
    // sensor typeid 2 = rws
    List<RoadStationSensorValueDto> findAllNonObsoleteRoadStationSensorValues(
            @Param("sensorTypeId")
            final int sensorTypeId,
            @Param("timeLimitInMinutes")
            final String timeLimitInMinutes,
            @Param("includedSensorNaturalIds")
                    List<Long> includedSensorNaturalIds);
}