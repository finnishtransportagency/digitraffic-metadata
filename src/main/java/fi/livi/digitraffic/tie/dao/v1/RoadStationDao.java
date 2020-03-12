package fi.livi.digitraffic.tie.dao.v1;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.RoadStationType;

@Repository
public class RoadStationDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public RoadStationDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<Long, Long> findPublishableRoadStationsIdsMappedByLotjuId(final RoadStationType stationType) {
        return jdbcTemplate.query(
            "SELECT station.id, station.lotju_id\n" +
            "FROM ROAD_STATION station\n" +
            "WHERE station.road_station_type = :stationType\n" +
            "  AND station.publishable = true",
            new MapSqlParameterSource().addValue("stationType", stationType, Types.VARCHAR),
            rs -> {
                final Map<Long, Long> map = new HashMap<>();
                while (rs.next()) {
                    map.put(rs.getLong("lotju_id"), rs.getLong("id"));
                }
                return map;
            });
    }
}
