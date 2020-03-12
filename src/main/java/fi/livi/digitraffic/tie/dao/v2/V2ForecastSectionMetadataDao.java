package fi.livi.digitraffic.tie.dao.v2;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.helper.DaoUtils;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Feature;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Properties;
import fi.livi.digitraffic.tie.model.v1.forecastsection.RoadSegment;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.Coordinate;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2.ForecastSectionV2FeatureDto;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2.RoadSegmentDto;

@Repository
public class V2ForecastSectionMetadataDao {
    private static final Logger log = LoggerFactory.getLogger(V2ForecastSectionMetadataDao.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String INSERT_FORECAST_SECTION =
        "INSERT INTO forecast_section(id, natural_id, description, length, version) " +
        "VALUES(nextval('seq_forecast_section'), :naturalId, :description, :length, :version) " +
        "ON CONFLICT ON CONSTRAINT forecast_section_unique " +
        "DO NOTHING ";

    private static final String UPDATE_FORECAST_SECTION =
        "UPDATE forecast_section SET description = :description, length = :length " +
        "WHERE natural_id = :naturalId AND version = :version AND obsolete_date IS null";

    private static final String INSERT_COORDINATE_LIST =
        "INSERT INTO forecast_section_coordinate_list(forecast_section_id, order_number) " +
        "VALUES((SELECT id FROM forecast_Section WHERE natural_id = :naturalId and version = :version), :orderNumber)";

    private static final String INSERT_COORDINATE =
        "INSERT INTO forecast_section_coordinate(forecast_section_id, list_order_number, order_number, longitude, latitude) " +
        "VALUES((SELECT id FROM forecast_section WHERE natural_id = :naturalId), :listOrderNumber, :orderNumber, :longitude, :latitude)";

    private static final String SELECT_ALL =
        "SELECT rs.order_number as rs_order_number, " +
        "rs.start_distance as rs_start_distance, rs.end_distance as rs_end_distance, rs.carriageway as rs_carriageway," +
        "li.order_number as li_order_number, * " +
        "FROM forecast_section f " +
        "          LEFT OUTER JOIN road_segment rs ON rs.forecast_section_id = f.id\n" +
        "          LEFT OUTER JOIN link_id li ON li.forecast_section_id = f.id\n" +
        "WHERE f.version = 2 " +
        "AND (:roadNumber IS NULL OR f.road_number::integer = :roadNumber) " +
        "AND (:minLongitude IS NULL OR :minLatitude IS NULL OR :maxLongitude IS NULL OR :maxLatitude IS NULL " +
        " OR f.id IN (SELECT forecast_section_id FROM forecast_section_coordinate co " +
        "             WHERE :minLongitude <= co.longitude AND co.longitude <= :maxLongitude AND :minLatitude <= co.latitude AND co.latitude <= :maxLatitude)) \n" +
        "AND (:naturalIdsIsEmpty IS TRUE OR f.natural_id IN (:naturalIds))\n" +
        "ORDER BY f.natural_id";

    private static final String SELECT_COORDINATES =
        "SELECT f.natural_id, c.list_order_number, '[' || array_to_string(array_agg('['|| c.longitude ||','|| c.latitude ||']' ORDER BY c.order_number), ',') || ']' AS coordinates\n" +
        "FROM forecast_section_coordinate c INNER JOIN forecast_section f ON c.forecast_section_id = f.id\n" +
        "WHERE f.version = 2 " +
        "AND (:roadNumber IS NULL OR f.road_number::integer = :roadNumber) " +
        "AND (:minLongitude IS NULL OR :minLatitude IS NULL OR :maxLongitude IS NULL OR :maxLatitude IS NULL " +
        " OR f.id IN (SELECT forecast_section_id FROM forecast_section_coordinate co " +
        "             WHERE :minLongitude <= co.longitude AND co.longitude <= :maxLongitude AND :minLatitude <= co.latitude AND co.latitude <= :maxLatitude)) \n" +
        "AND (:naturalIdsIsEmpty IS TRUE OR f.natural_id IN (:naturalIds))\n" +
        "GROUP BY f.natural_id, c.list_order_number\n" +
        "ORDER BY f.natural_id, c.list_order_number";

    private static final String INSERT_ROAD_SEGMENT =
        "INSERT INTO road_segment(forecast_section_id, order_number, start_distance, end_distance, carriageway) " +
        "VALUES((SELECT id FROM forecast_section WHERE natural_id = :naturalId), :orderNumber, :startDistance, :endDistance, :carriageway)";

    private static final String INSERT_LINK_IDS =
        "INSERT INTO link_id(forecast_section_id, order_number, link_id) " +
        "VALUES((SELECT id FROM forecast_section WHERE natural_id = :naturalId), :orderNumber, :linkId)";

    @Autowired
    public V2ForecastSectionMetadataDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void upsertForecastSections(final List<ForecastSectionV2FeatureDto> features) {

        final MapSqlParameterSource sources[] = new MapSqlParameterSource[features.size()];
        int i = 0;
        for (final ForecastSectionV2FeatureDto feature : features) {
            sources[i] = forecastSectionParameterSource(feature);
            i++;
        }

        // Only DO NOTHING is supported for EXCLUDE constraint (ON CONFLICT ON CONSTRAINT forecast_section_unique)
        jdbcTemplate.batchUpdate(INSERT_FORECAST_SECTION, sources);
        jdbcTemplate.batchUpdate(UPDATE_FORECAST_SECTION, sources);
    }

    private static MapSqlParameterSource forecastSectionParameterSource(final ForecastSectionV2FeatureDto feature) {
        final HashMap<String, Object> args = new HashMap<>();
        args.put("naturalId", feature.getProperties().getId());
        args.put("description", feature.getProperties().getDescription());
        args.put("length", feature.getProperties().getTotalLengthKm() * 1000);
        args.put("version", 2);
        return new MapSqlParameterSource(args);
    }

    public void insertCoordinates(final List<ForecastSectionV2FeatureDto> features) {

        final int listCount = features.stream().mapToInt(f -> f.getGeometry().getCoordinates().size()).sum();
        final MapSqlParameterSource listSources[] = new MapSqlParameterSource[listCount];

        final int coordinateCount = features.stream().mapToInt(f -> f.getGeometry().getCoordinates().stream().mapToInt(l -> l.size()).sum()).sum();
        final MapSqlParameterSource coordinateSources[] = new MapSqlParameterSource[coordinateCount];

        int listNumber = 0;
        int coordinateNumber = 0;
        for (final ForecastSectionV2FeatureDto feature : features) {

            int listOrderNumber = 1;
            for (final List<Coordinate> coordinates : feature.getGeometry().getCoordinates()) {
                listSources[listNumber] = coordinateListParameterSource(feature.getProperties().getId(), listOrderNumber);

                int coordinateOrderNumber = 1;
                for (final Coordinate coordinate : coordinates) {
                    coordinateSources[coordinateNumber] = coordinateParameterSource(feature.getProperties().getId(), listOrderNumber, coordinateOrderNumber, coordinate);
                    coordinateOrderNumber++;
                    coordinateNumber++;
                }
                listNumber++;
                listOrderNumber++;
            }
        }

        jdbcTemplate.batchUpdate(INSERT_COORDINATE_LIST, listSources);
        jdbcTemplate.batchUpdate(INSERT_COORDINATE, coordinateSources);
    }

    public List<ForecastSectionV2Feature> findForecastSectionV2Features(final Integer roadNumber, final Double minLongitude, final Double minLatitude,
                                                                        final Double maxLongitude, final Double maxLatitude,
                                                                        final List<String> naturalIds) {

        final HashMap<String, ForecastSectionV2Feature> featureMap = new HashMap<>();

        final MapSqlParameterSource paramSource = new MapSqlParameterSource()
            .addValue("roadNumber", roadNumber, Types.INTEGER)
            .addValue("minLongitude", minLongitude, Types.DOUBLE)
            .addValue("minLatitude", minLatitude, Types.DOUBLE)
            .addValue("maxLongitude", maxLongitude, Types.DOUBLE)
            .addValue("maxLatitude", maxLatitude, Types.DOUBLE)
            .addValue("naturalIdsIsEmpty", naturalIds == null || naturalIds.isEmpty())
            .addValue("naturalIds", naturalIds);

        jdbcTemplate.query(SELECT_ALL, paramSource, rs -> {
            final String naturalId = rs.getString("natural_id");

            if (!featureMap.containsKey(naturalId)) {
                final ForecastSectionV2Feature feature = new ForecastSectionV2Feature(rs.getLong("forecast_section_id"),
                                                                                      new MultiLineString(),
                                                                                      new ForecastSectionV2Properties(naturalId,
                                                                                                                      rs.getString("description"),
                                                                                                                      Integer.parseInt(rs.getString("road_number")),
                                                                                                                      Integer.parseInt(rs.getString("road_section_number")),
                                                                                                                      rs.getInt("length"),
                                                                                                                      new ArrayList<>(),
                                                                                                                      new ArrayList<>()));
                setRoadSegment(rs, feature);
                setLinkId(rs, feature);

                featureMap.put(naturalId, feature);
            } else {
                setRoadSegment(rs, featureMap.get(naturalId));
                setLinkId(rs, featureMap.get(naturalId));
            }
        });

        jdbcTemplate.query(SELECT_COORDINATES, paramSource, rs -> {
            final TypeReference<List<List<Double>>> typeReference = new TypeReference<List<List<Double>>>() {};
            List coordinates = new ArrayList();
            try {
                coordinates = new ObjectMapper().readValue(rs.getString("coordinates"), typeReference);
            } catch (IOException e) {
                log.error("method=findForecastSectionV2Features coordinates objectMapper readValue error");
            }
            final ForecastSectionV2Feature feature = featureMap.get(rs.getString("natural_id"));
            feature.getGeometry().getCoordinates().add(coordinates);
        });

        return featureMap.values().stream()
            .sorted(Comparator.comparing(f -> f.getProperties().getNaturalId())).collect(Collectors.toList());
    }

    private static void setLinkId(final ResultSet rs, final ForecastSectionV2Feature feature) throws SQLException {
        final int orderNumber = rs.getInt("li_order_number");

        while (feature.getProperties().getLinkIdList().size() < orderNumber) {
            feature.getProperties().getLinkIdList().add(0L);
        }

        feature.getProperties().getLinkIdList().set(orderNumber - 1, rs.getLong("link_id"));
    }

    private static void setRoadSegment(final ResultSet rs, final ForecastSectionV2Feature feature) throws SQLException {
        final int orderNumber = rs.getInt("rs_order_number");

        while (feature.getProperties().getRoadSegments().size() < orderNumber) {
            feature.getProperties().getRoadSegments().add(new RoadSegment());
        }
        final RoadSegment roadSegment = feature.getProperties().getRoadSegments().get(orderNumber - 1);

        roadSegment.setStartDistance(rs.getInt("rs_start_distance"));
        roadSegment.setEndDistance(rs.getInt("rs_end_distance"));
        roadSegment.setCarriageway(DaoUtils.findInteger(rs, "rs_carriageway"));
    }

    private static MapSqlParameterSource coordinateParameterSource(final String naturalId, final int listOrderNumber, final int coordinateOrderNumber,
                                                                   final Coordinate coordinate) {
        return new MapSqlParameterSource()
            .addValue("naturalId", naturalId)
            .addValue("listOrderNumber", listOrderNumber)
            .addValue("orderNumber", coordinateOrderNumber)
            .addValue("longitude", coordinate.longitude)
            .addValue("latitude", coordinate.latitude);
    }

    private static MapSqlParameterSource coordinateListParameterSource(final String naturalId, final int orderNumber) {
        return new MapSqlParameterSource()
            .addValue("naturalId", naturalId)
            .addValue("orderNumber", orderNumber)
            .addValue("version", 2);
    }

    public void insertRoadSegments(final List<ForecastSectionV2FeatureDto> features) {
        final MapSqlParameterSource[] segmentSources = new MapSqlParameterSource[features.stream().mapToInt(f -> f.getProperties().getRoadSegmentList().size()).sum()];

        int i = 0;
        for (final ForecastSectionV2FeatureDto feature : features) {
            int orderNumber = 1;
            for (final RoadSegmentDto roadSegmentDto : feature.getProperties().getRoadSegmentList()) {
                segmentSources[i] = roadSegmentParameterSource(feature, roadSegmentDto, orderNumber);
                i++;
                orderNumber++;
            }
        }

        jdbcTemplate.batchUpdate(INSERT_ROAD_SEGMENT, segmentSources);
    }

    private static MapSqlParameterSource roadSegmentParameterSource(final ForecastSectionV2FeatureDto feature, final RoadSegmentDto roadSegmentDto, final int orderNumber) {
        return new MapSqlParameterSource()
            .addValue("naturalId", feature.getProperties().getId())
            .addValue("orderNumber", orderNumber)
            .addValue("startDistance", roadSegmentDto.getStartDistance())
            .addValue("endDistance", roadSegmentDto.getEndDistance())
            .addValue("carriageway", roadSegmentDto.getCarriageway());
    }

    public void insertLinkIds(final List<ForecastSectionV2FeatureDto> features) {
        final MapSqlParameterSource[] linkIdSources = new MapSqlParameterSource[features.stream().mapToInt(f -> f.getProperties().getLinkIdList().size()).sum()];

        int i = 0;
        for (final ForecastSectionV2FeatureDto feature : features) {
            int orderNumber = 1;
            for (final Long linkId : feature.getProperties().getLinkIdList()) {
                linkIdSources[i] = linkIdParameterSource(feature, linkId, orderNumber);
                i++;
                orderNumber++;
            }
        }

        jdbcTemplate.batchUpdate(INSERT_LINK_IDS, linkIdSources);
    }

    private static MapSqlParameterSource linkIdParameterSource(final ForecastSectionV2FeatureDto feature, final Long linkId, final int orderNumber) {
        return new MapSqlParameterSource()
            .addValue("naturalId", feature.getProperties().getId())
            .addValue("orderNumber", orderNumber)
            .addValue("linkId", linkId);
    }
}
