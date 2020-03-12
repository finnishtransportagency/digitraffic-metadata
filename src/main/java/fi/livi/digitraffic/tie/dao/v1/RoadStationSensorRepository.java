package fi.livi.digitraffic.tie.dao.v1;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import fi.livi.digitraffic.tie.dto.v1.StationSensors;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;
import fi.livi.digitraffic.tie.model.RoadStationType;

public interface RoadStationSensorRepository extends JpaRepository<RoadStationSensor, Long> {
    String TMS_STATION_TYPE = "TMS_STATION";
    String WEATHER_STATION_TYPE  ="WEATHER_STATION";

    @Query("SELECT s\n" +
           "FROM RoadStationSensor s\n" +
           "WHERE s.publishable = true\n" +
           "  AND s.roadStationType = ?1\n" +
           "  AND EXISTS (\n" +
           "     FROM AllowedRoadStationSensor allowed\n" +
           "     WHERE allowed.naturalId = s.naturalId\n" +
           "       AND allowed.roadStationType = s.roadStationType\n" +
           "  )" +
           "ORDER BY s.naturalId")
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<RoadStationSensor> findByRoadStationTypeAndPublishable(final RoadStationType roadStationType);

    @EntityGraph(attributePaths = {"sensorValueDescriptions"})
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<RoadStationSensor> findDistinctByRoadStationType(final RoadStationType roadStationType);

    @EntityGraph(attributePaths = {"sensorValueDescriptions"})
    RoadStationSensor findByRoadStationTypeAndLotjuId(final RoadStationType stationType, final Long sensorLotjuId);

    @Query(value =
        "SELECT rs_sensors.road_station_id roadStationId, string_agg(cast(sensor.natural_id as varchar), ',' order by sensor.natural_id) " +
            "AS sensors\n" +
            "FROM   road_station_sensor sensor\n" +
            "inner join road_station_sensors rs_sensors on rs_sensors.road_station_sensor_id = sensor.id\n" +
            "inner join allowed_road_station_sensor allowed on allowed.natural_id = sensor.natural_id\n" +
            "where sensor.publishable = true\n" +
            "  and sensor.road_station_type = :stationType\n" +
            "GROUP BY rs_sensors.road_station_id\n" +
            "order by rs_sensors.road_station_id", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<StationSensors> listStationPublishableSensorsByType(@Param("stationType") final String stationType);

    @Query(value =
        "SELECT rs_sensors.road_station_id roadStationId, string_agg(cast(sensor.natural_id as varchar), ',' order by sensor.natural_id) " +
            "AS sensors\n" +
            "FROM   road_station_sensor sensor\n" +
            "inner join road_station_sensors rs_sensors on rs_sensors.road_station_sensor_id = sensor.id\n" +
            "inner join allowed_road_station_sensor allowed on allowed.natural_id = sensor.natural_id\n" +
            "where rs_sensors.road_station_id = :id\n" +
            "  and sensor.publishable = true\n" +
            "  and sensor.road_station_type = :stationType\n" +
            "GROUP BY rs_sensors.road_station_id\n" +
            "order by rs_sensors.road_station_id", nativeQuery = true)
    List<StationSensors> getStationPublishableSensorsByStationIdAndType(@Param("id") final long roadStationId, @Param("stationType") final String
        stationType);


    @Modifying(clearAutomatically = true)
    @Query(value =
            "DELETE FROM ROAD_STATION_SENSORS\n" +
            "WHERE ROAD_STATION_ID = :roadStationId\n" +
            "AND ROAD_STATION_SENSOR_ID NOT IN (\n" +
            "    SELECT SENSOR.ID\n" +
            "    FROM ROAD_STATION_SENSOR SENSOR\n" +
            "    WHERE SENSOR.ROAD_STATION_TYPE = :roadStationType\n" +
            "      AND SENSOR.LOTJU_ID IN (:sensorsLotjuIds)\n" +
            ")",
           nativeQuery = true)
    int deleteNonExistingSensors(@Param("roadStationType") final String roadStationType,
                                 @Param("roadStationId") final Long roadStationId,
                                 @Param("sensorsLotjuIds") final List<Long> sensorsLotjuIds);

    @Modifying(clearAutomatically = true)
    @Query(value =
            "DELETE FROM ROAD_STATION_SENSORS\n" +
            "WHERE ROAD_STATION_ID = :roadStationId",
           nativeQuery = true)
    int deleteRoadStationsSensors(@Param("roadStationId") final Long roadStationId);

    @Modifying(clearAutomatically = true)
    @Query(value =
            "INSERT INTO ROAD_STATION_SENSORS (ROAD_STATION_ID, ROAD_STATION_SENSOR_ID)\n" +
            "  SELECT RS.ID AS ROAD_STATION_ID\n" +
            "       , SENSOR.ID AS ROAD_STATION_SENSOR_ID\n" +
            "  FROM ROAD_STATION_SENSOR SENSOR, ROAD_STATION RS\n" +
            "  WHERE SENSOR.ROAD_STATION_TYPE = :roadStationType\n" +
            "    AND RS.ROAD_STATION_TYPE = SENSOR.ROAD_STATION_TYPE\n" +
            "    AND SENSOR.LOTJU_ID IN (:sensorsLotjuIds)\n" +
            "    AND RS.ID = :roadStationId\n" +
            "    AND NOT EXISTS(\n" +
            "      SELECT NULL\n" +
            "      FROM ROAD_STATION_SENSORS RSS\n" +
            "      WHERE RSS.ROAD_STATION_ID = RS.ID\n" +
            "        AND RSS.ROAD_STATION_SENSOR_ID = SENSOR.ID\n" +
            "  )",
           nativeQuery = true)
    int insertNonExistingSensors(@Param("roadStationType") final String roadStationType,
                                 @Param("roadStationId") final Long roadStationId,
                                 @Param("sensorsLotjuIds") final List<Long> sensorsLotjuIds);
}