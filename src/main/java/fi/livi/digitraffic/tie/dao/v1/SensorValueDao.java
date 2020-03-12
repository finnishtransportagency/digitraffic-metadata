package fi.livi.digitraffic.tie.dao.v1;

import java.sql.JDBCType;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.SensorValueUpdateParameterDto;

@Repository
public class SensorValueDao {
    private static final Logger log = LoggerFactory.getLogger(SensorValueDao.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String UPDATE =
        "UPDATE SENSOR_VALUE AS sv\n" +
            "SET value = :value\n" +
            "  , measured = :measured\n" +
            "  , updated = current_timestamp\n" +
            "  , time_window_start = :timeWindowStart\n" +
            "  , time_window_end = :timeWindowEnd\n" +
            "WHERE sv.road_station_id = :roadStationId\n" +
            "AND sv.road_station_sensor_id = (select id from road_station_sensor where lotju_id = :sensorLotjuId " +
            "and road_station_type = :stationType " +
            "and publishable = true)";

    private static final String INSERT =
            "INSERT INTO sensor_value(id, road_station_id, road_station_sensor_id, value,\n" +
            "                         measured, updated, time_window_start, time_window_end)\n" +
            "SELECT nextval('seq_sensor_value'), :roadStationId, sensor.id,\n" +
            "       :value, :measured, current_timestamp, :timeWindowStart, :timeWindowEnd\n" +
            "FROM ROAD_STATION_SENSOR sensor\n" +
            "WHERE sensor.lotju_id = :sensorLotjuId\n" +
            "  AND sensor.road_station_type = :stationType\n" +
            "  AND sensor.publishable = true";

    @Autowired
    public SensorValueDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @param params
     * @return the number of rows updated for each param. Zero value in return array means that
     *         the parameter in question didn't cause update -> should be called with inserted instead.
     */
    public int[] updateSensorData(final List<SensorValueUpdateParameterDto> params) {
        final MapSqlParameterSource[] batchData = getMapSqlParameterSources(params);

        return jdbcTemplate.batchUpdate(UPDATE, batchData);
    }

    /**
     * @param params
     * @return the number of rows inserted for each param.
     */
    public int[] insertSensorData(final List<SensorValueUpdateParameterDto> params) {
        final MapSqlParameterSource[] batchData = getMapSqlParameterSources(params);

        return jdbcTemplate.batchUpdate(INSERT, batchData);
    }

    private static MapSqlParameterSource[] getMapSqlParameterSources(final List<SensorValueUpdateParameterDto> params) {
        return params.stream().map(p -> new MapSqlParameterSource()
            .addValue("value", p.getValue(), JDBCType.NUMERIC.getVendorTypeNumber())
            .addValue("measured", p.getMeasured(), JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber())
            .addValue("roadStationId", p.getRoadStationId(), JDBCType.NUMERIC.getVendorTypeNumber())
            .addValue("sensorLotjuId", p.getSensorLotjuId(), JDBCType.NUMERIC.getVendorTypeNumber())
            .addValue("stationType", p.getStationType(), JDBCType.VARCHAR.getVendorTypeNumber())
            .addValue("timeWindowStart", p.getTimeWindowStart(), JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber())
            .addValue("timeWindowEnd", p.getTimeWindowEnd(), JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber()))
            .toArray(MapSqlParameterSource[]::new);
    }
}
