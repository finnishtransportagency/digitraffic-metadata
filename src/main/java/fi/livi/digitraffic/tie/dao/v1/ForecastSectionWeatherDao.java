package fi.livi.digitraffic.tie.dao.v1;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.forecast.ForecastConditionReasonDto;
import fi.livi.digitraffic.tie.dto.v1.forecast.RoadConditionDto;
import fi.livi.digitraffic.tie.helper.DaoUtils;
import fi.livi.digitraffic.tie.model.v1.forecastsection.FrictionCondition;
import fi.livi.digitraffic.tie.model.v1.forecastsection.OverallRoadCondition;
import fi.livi.digitraffic.tie.model.v1.forecastsection.PrecipitationCondition;
import fi.livi.digitraffic.tie.model.v1.forecastsection.Reliability;
import fi.livi.digitraffic.tie.model.v1.forecastsection.RoadCondition;
import fi.livi.digitraffic.tie.model.v1.forecastsection.VisibilityCondition;
import fi.livi.digitraffic.tie.model.v1.forecastsection.WindCondition;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionApiVersion;

@Repository
public class ForecastSectionWeatherDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ForecastSectionWeatherDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, List<RoadConditionDto>> getForecastSectionWeatherData(final ForecastSectionApiVersion version, final Integer roadNumber,
                                                                             final Double minLongitude, final Double minLatitude,
                                                                             final Double maxLongitude, final Double maxLatitude,
                                                                             final List<String> naturalIds) {
        final HashMap<String, List<RoadConditionDto>> res = new HashMap<>();

        jdbcTemplate.query(
            "SELECT fs.natural_id, fsw.forecast_name, fsw.time, fsw.daylight, fsw.overall_road_condition, fsw.reliability, " +
            "fsw.road_temperature, fsw.temperature, fsw.weather_symbol, fsw.wind_direction, fsw.wind_speed, fsw.type, " +
            "fcr.precipitation_condition, fcr.road_condition, fcr.wind_condition, fcr.freezing_rain_condition, fcr.freezing_rain_condition, " +
            "fcr.winter_slipperiness, fcr.visibility_condition, fcr.friction_condition " +
            "FROM FORECAST_SECTION_WEATHER fsw " +
            "LEFT OUTER JOIN FORECAST_CONDITION_REASON fcr ON fsw.forecast_section_id = fcr.forecast_section_id AND fsw.forecast_name = fcr.forecast_name " +
            "LEFT OUTER JOIN FORECAST_SECTION fs ON fsw.forecast_section_id = fs.id " +
            "WHERE fs.version = :version " +
            "AND (:roadNumber IS NULL OR fs.road_number::integer = :roadNumber)\n" +
            "AND (:minLongitude IS NULL OR :minLatitude IS NULL OR :maxLongitude IS NULL OR :maxLatitude IS NULL " +
            "     OR fs.id IN (SELECT forecast_section_id FROM forecast_section_coordinate co " +
            "                 WHERE :minLongitude <= co.longitude AND co.longitude <= :maxLongitude AND :minLatitude <= co.latitude AND co.latitude <= :maxLatitude)) \n" +
            "AND (:naturalIdsIsEmpty IS TRUE OR fs.natural_id IN (:naturalIds))\n" +
            "ORDER BY fs.natural_id, fsw.time",
            new MapSqlParameterSource()
                .addValue("version", version.getVersion(), Types.INTEGER)
                .addValue("roadNumber", roadNumber, Types.INTEGER)
                .addValue("minLongitude", minLongitude, Types.DOUBLE)
                .addValue("minLatitude", minLatitude, Types.DOUBLE)
                .addValue("maxLongitude", maxLongitude, Types.DOUBLE)
                .addValue("maxLatitude", maxLatitude, Types.DOUBLE)
                .addValue("naturalIdsIsEmpty", naturalIds == null || naturalIds.isEmpty())
                .addValue("naturalIds", naturalIds),
            rs -> {
                final String forecastSectionNaturalId = rs.getString("natural_id");

                if (res.containsKey(forecastSectionNaturalId)) {
                    res.get(forecastSectionNaturalId).add(mapRoadConditionDto(rs));
                } else {
                    final ArrayList<RoadConditionDto> list = new ArrayList<>();
                    list.add(mapRoadConditionDto(rs));
                    res.put(forecastSectionNaturalId, list);
                }
            });

        return res;
    }

    private RoadConditionDto mapRoadConditionDto(final ResultSet rs) throws SQLException {
        final Calendar c = Calendar.getInstance();

        return new RoadConditionDto(
                        rs.getString("forecast_name"),
                        ZonedDateTime.ofInstant(rs.getTimestamp("time", c).toInstant(), c.getTimeZone().toZoneId()),
                        DaoUtils.findBoolean(rs, "daylight"),
                        DaoUtils.findEnum(rs, "overall_road_condition", OverallRoadCondition.class),
                        DaoUtils.findEnum(rs, "reliability", Reliability.class),
                        rs.getString("road_temperature"),
                        rs.getString("temperature"),
                        rs.getString("weather_symbol"),
                        DaoUtils.findInteger(rs, "wind_direction"),
                        DaoUtils.findDouble(rs, "wind_speed"),
                        rs.getString("type"),
                        mapForecastConditionReason(rs));
    }



    private static ForecastConditionReasonDto mapForecastConditionReason(final ResultSet rs) throws SQLException {
        final PrecipitationCondition precipitationCondition = DaoUtils.findEnum(rs, "precipitation_condition", PrecipitationCondition.class);
        final RoadCondition roadCondition = DaoUtils.findEnum(rs, "road_condition", RoadCondition.class);
        final WindCondition windCondition = DaoUtils.findEnum(rs, "wind_condition", WindCondition.class);
        final Boolean freezingRainCondition = DaoUtils.findBoolean(rs, "freezing_rain_condition");
        final Boolean winterSlipperiness = DaoUtils.findBoolean(rs, "winter_slipperiness");
        final VisibilityCondition visibilityCondition = DaoUtils.findEnum(rs, "visibility_condition", VisibilityCondition.class);
        final FrictionCondition frictionCondition = DaoUtils.findEnum(rs, "friction_condition", FrictionCondition.class);

        if (ObjectUtils.anyNotNull(precipitationCondition, roadCondition, windCondition, freezingRainCondition, winterSlipperiness,
                                   visibilityCondition, frictionCondition)) {
            return new ForecastConditionReasonDto(precipitationCondition, roadCondition, windCondition, freezingRainCondition,
                                               winterSlipperiness, visibilityCondition, frictionCondition);
        } else {
            return null;
        }
    }

}
