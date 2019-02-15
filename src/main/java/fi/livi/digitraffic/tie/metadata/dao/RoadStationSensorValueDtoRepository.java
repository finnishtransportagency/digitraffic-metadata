package fi.livi.digitraffic.tie.metadata.dao;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;

public interface RoadStationSensorValueDtoRepository extends JpaRepository<SensorValueDto, Long> {
    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="2000"))
    @Query(value =
            "select rs.natural_id road_station_natural_id\n" +
            "     , s.natural_id sensor_natural_id\n" +
            "     , sv.id sensor_value_id\n" +
            "     , sv.value sensor_value\n" +
            "     , sv.time_window_start\n" +
            "     , sv.time_window_end\n" +
            "     , sv.measured as measured_time\n" +
            "     , s.name sensor_name_old\n" +
            "     , s.name_fi sensor_name_fi\n" +
            "     , s.short_name_fi sensor_short_name_fi\n" +
            "     , s.unit sensor_unit\n" +
            "     , svd.description_fi as sensor_value_description_fi\n" +
            "     , svd.description_en as sensor_value_description_en\n" +
            "     , max(sv.measured) over(partition by sv.road_station_id) station_latest_measured_time" +
            "     , sv.updated as updated_time\n" +
            "from road_station rs\n" +
            "inner join sensor_value sv on sv.road_station_id = rs.id\n" +
            "inner join road_station_sensor s on sv.road_station_sensor_id = s.id\n" +
            "left outer join sensor_value_description svd on svd.sensor_id = sv.road_station_sensor_id\n" +
            "                                            and svd.sensor_value = sv.value\n" +
            "where rs.type = :stationTypeId\n" +
            "  and rs.publishable = true\n" +
            "  and s.publishable = true\n" +
            "  and sv.measured > (\n" +
            "    select max(sensv.measured) - (:timeLimitInMinutes * interval '1 MINUTE')\n" +
            "    from sensor_value sensv\n" +
            "    where sensv.road_station_id = sv.road_station_id\n" +
            "  )\n" +
            "  and exists (\n" +
            "     select null\n" +
            "     from allowed_road_station_sensor allowed\n" +
            "     where allowed.natural_id = s.natural_id\n" +
            "       and allowed.road_station_type = s.road_station_type\n" +
            "  )",
           nativeQuery = true)
    // sensor typeid 2 = rws
    Stream<SensorValueDto> findAllPublicPublishableRoadStationSensorValues(
            @Param("stationTypeId")
            final int stationTypeId,
            @Param("timeLimitInMinutes")
            final int timeLimitInMinutes);

    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="2000"))
    @Query(value =
            "select rs.natural_id road_station_natural_id\n" +
            "     , s.natural_id sensor_natural_id\n" +
            "     , sv.id sensor_value_id\n" +
            "     , sv.value sensor_value\n" +
            "     , sv.time_window_start\n" +
            "     , sv.time_window_end\n" +
            "     , sv.measured as measured_time\n" +
            "     , s.name sensor_name_old\n" +
            "     , s.name_fi sensor_name_fi\n" +
            "     , s.short_name_fi sensor_short_name_fi\n" +
            "     , s.unit sensor_unit\n" +
            "     , svd.description_fi as sensor_value_description_fi\n" +
            "     , svd.description_en as sensor_value_description_en\n" +
            "     , max(sv.measured) over(partition by sv.road_station_id) station_latest_measured_time" +
            "     , sv.updated as updated_time\n" +
            "from road_station rs\n" +
            "inner join sensor_value sv on sv.road_station_id = rs.id\n" +
            "inner join road_station_sensor s on sv.road_station_sensor_id = s.id\n" +
            "left outer join sensor_value_description svd on svd.sensor_id = sv.road_station_sensor_id\n" +
            "                                            and svd.sensor_value = sv.value\n" +
            "where rs.type = :stationTypeId\n" +
            "  and rs.natural_id = :stationNaturalId\n" +
            "  and rs.publishable = true\n" +
            "  and s.publishable = true\n" +
            "  and sv.measured > (\n" +
            "    select max(sensv.measured) - (:timeLimitInMinutes * interval '1 MINUTE')\n" +
            "    from sensor_value sensv\n" +
            "    where sensv.road_station_id = sv.road_station_id\n" +
            "  )\n" +
            "  and exists (\n" +
            "     select null\n" +
            "     from allowed_road_station_sensor allowed\n" +
            "     where allowed.natural_id = s.natural_id\n" +
            "       and allowed.road_station_type = s.road_station_type\n" +
            "  )\n" +
            "order by rs.natural_id, s.natural_id",
           nativeQuery = true)
    List<SensorValueDto> findAllPublicPublishableRoadStationSensorValues(
            @Param("stationNaturalId")
            final long stationNaturalId,
            @Param("stationTypeId")
            final int stationTypeId,
            @Param("timeLimitInMinutes")
            final int timeLimitInMinutes);

    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="2000"))
    @Query(value =
                   "select rs.natural_id road_station_natural_id\n" +
                   "     , s.natural_id sensor_natural_id\n" +
                   "     , sv.id sensor_value_id\n" +
                   "     , sv.value sensor_value\n" +
                   "     , sv.time_window_start\n" +
                   "     , sv.time_window_end\n" +
                   "     , sv.measured as measured_time\n" +
                   "     , s.name sensor_name_old\n" +
                   "     , s.name_fi sensor_name_fi\n" +
                   "     , s.short_name_fi sensor_short_name_fi\n" +
                   "     , s.unit sensor_unit\n" +
                   "     , svd.description_fi as sensor_value_description_fi\n" +
                   "     , svd.description_en as sensor_value_description_en\n" +
                   "     , max(sv.measured) over(partition by sv.road_station_id) station_latest_measured_time" +
                   "     , sv.updated as updated_time\n" +
                   "from road_station rs\n" +
                   "inner join sensor_value sv on sv.road_station_id = rs.id\n" +
                   "inner join road_station_sensor s on sv.road_station_sensor_id = s.id\n" +
                   "left outer join sensor_value_description svd on svd.sensor_id = sv.road_station_sensor_id\n" +
                   "                                            and svd.sensor_value = sv.value\n" +
                   "where rs.type = :stationTypeId\n" +
                   "  and rs.publishable = true\n" +
                   "  and s.publishable = true\n" +
                   "  and sv.updated > :afterDate\n" +
                   "  and exists (\n" +
                   "     select null\n" +
                   "     from allowed_road_station_sensor allowed\n" +
                   "     where allowed.natural_id = s.natural_id\n" +
                   "       and allowed.road_station_type = s.road_station_type\n" +
                   "  )\n" +
                   "order by sv.updated",
                   nativeQuery = true)
    List<SensorValueDto> findAllPublicPublishableRoadStationSensorValuesUpdatedAfter(
            @Param("stationTypeId")
            final int stationTypeId,
            @Param("afterDate")
            final Date afterDate);

    @Query(value =
           "select max(sv.measured) as updated_time\n" +
           "from road_station rs\n" +
           "inner join sensor_value sv on sv.road_station_id = rs.id\n" +
           "inner join road_station_sensor s on sv.road_station_sensor_id = s.id\n" +
           "where rs.type = :stationTypeId\n" +
           "  and rs.publishable = true\n" +
           "  and s.publishable = true\n" +
           "  and sv.measured > (\n" +
           "    select max(sensv.measured) - (:timeLimitInMinutes * interval '1 minute')\n" +
           "    from sensor_value sensv\n" +
           "    where sensv.road_station_id = sv.road_station_id\n" +
           "  )\n" +
           "  and exists (\n" +
           "     select null\n" +
           "     from allowed_road_station_sensor allowed\n" +
           "     where allowed.natural_id = s.natural_id\n" +
           "       and allowed.road_station_type = s.road_station_type\n" +
           "  )",
           nativeQuery = true)
    Instant getLatestMeasurementTime(@Param("stationTypeId")
                                           final int stationTypeId,
                                           @Param("timeLimitInMinutes")
                                           final int timeLimitInMinutes);
}
