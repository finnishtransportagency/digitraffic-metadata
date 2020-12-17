package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.helper.DateHelper.toZonedDateTimeAtUtc;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingViewRepository;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingFeature;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingLatestFeature;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingLatestFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingLatestProperties;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingProperties;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingViewDto;
import fi.livi.digitraffic.tie.helper.PostgisGeometryHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingDto;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.service.DataStatusService;

/**
 * This service returns Harja tracking data for public use
 *
 * @see V2MaintenanceTrackingUpdateService
 * @see <a href="https://github.com/finnishtransportagency/harja">https://github.com/finnishtransportagency/harja</a>
 */
@Service
public class V2MaintenanceTrackingDataService {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceTrackingDataService.class);
    private final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository;
    private final V2MaintenanceTrackingDataRepository v2MaintenanceTrackingDataRepository;
    private final V2MaintenanceTrackingViewRepository v2MaintenanceTrackingViewRepository;
    private final DataStatusService dataStatusService;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public V2MaintenanceTrackingDataService(final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository,
                                            final V2MaintenanceTrackingDataRepository v2MaintenanceTrackingDataRepository,
                                            final V2MaintenanceTrackingViewRepository v2MaintenanceTrackingViewRepository,
                                            final DataStatusService dataStatusService) {
        this.v2MaintenanceTrackingRepository = v2MaintenanceTrackingRepository;
        this.v2MaintenanceTrackingDataRepository = v2MaintenanceTrackingDataRepository;
        this.v2MaintenanceTrackingViewRepository = v2MaintenanceTrackingViewRepository;
        this.dataStatusService = dataStatusService;
    }

    @Transactional(readOnly = true)
    public MaintenanceTrackingLatestFeatureCollection findLatestMaintenanceTrackings(final Instant endTimefrom, final Instant endTimeto,
                                                                                     final double xMin, final double yMin,
                                                                                     final double xMax, final double yMax,
                                                                                     final List<MaintenanceTrackingTask> taskIds) {
        final ZonedDateTime lastUpdated = toZonedDateTimeAtUtc(dataStatusService.findDataUpdatedTime(DataType.MAINTENANCE_TRACKING_DATA));
        final ZonedDateTime lastChecked = toZonedDateTimeAtUtc(dataStatusService.findDataUpdatedTime(DataType.MAINTENANCE_TRACKING_DATA_CHECKED));

        final Polygon area = PostgisGeometryHelper.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);

        final StopWatch start = StopWatch.createStarted();
        final List<MaintenanceTrackingDto> found = taskIds == null || taskIds.isEmpty() ?
                                                   v2MaintenanceTrackingViewRepository.findLatestByAgeAndBoundingBox(toZonedDateTimeAtUtc(endTimefrom), toZonedDateTimeAtUtc(endTimeto), area) :
                                                   v2MaintenanceTrackingViewRepository.findLatestByAgeAndBoundingBoxAndTasks(toZonedDateTimeAtUtc(endTimefrom), toZonedDateTimeAtUtc(endTimeto), area, taskIds);
        log.info("method=findMaintenanceRealizations with params xMin {}, xMax {}, yMin {}, yMax {} fromTime={} toTime={} foundCount={} tookMs={}",
            xMin, xMax, yMin, yMax, toZonedDateTimeAtUtc(endTimefrom), toZonedDateTimeAtUtc(endTimeto), found.size(), start.getTime());

        final List<MaintenanceTrackingLatestFeature> features = convertToTrackingLatestFeatures(found);
        return new MaintenanceTrackingLatestFeatureCollection(lastUpdated, lastChecked, features);
    }

    @Transactional(readOnly = true)
    public MaintenanceTrackingFeatureCollection findMaintenanceTrackings(final Instant endTimeFrom, final Instant endTimeTo,
                                                                         final double xMin, final double yMin,
                                                                         final double xMax, final double yMax,
                                                                         final List<MaintenanceTrackingTask> taskIds) {
        final ZonedDateTime lastUpdated = toZonedDateTimeAtUtc(dataStatusService.findDataUpdatedTime(DataType.MAINTENANCE_TRACKING_DATA));
        final ZonedDateTime lastChecked = toZonedDateTimeAtUtc(dataStatusService.findDataUpdatedTime(DataType.MAINTENANCE_TRACKING_DATA_CHECKED));

        final Polygon area = PostgisGeometryHelper.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);

        final StopWatch start = StopWatch.createStarted();
        final List<MaintenanceTrackingDto> found = taskIds == null || taskIds.isEmpty() ?
                                                   v2MaintenanceTrackingViewRepository.findByAgeAndBoundingBox(toZonedDateTimeAtUtc(endTimeFrom), toZonedDateTimeAtUtc(endTimeTo), area) :
                                                   v2MaintenanceTrackingViewRepository.findByAgeAndBoundingBoxAndTasks(toZonedDateTimeAtUtc(endTimeFrom), toZonedDateTimeAtUtc(endTimeTo), area, taskIds);
        log.info("method=findMaintenanceRealizations with params xMin {}, xMax {}, yMin {}, yMax {} fromTime={} toTime={} foundCount={} tookMs={}",
            xMin, xMax, yMin, yMax, toZonedDateTimeAtUtc(endTimeFrom), toZonedDateTimeAtUtc(endTimeTo), found.size(), start.getTime());

        final List<MaintenanceTrackingFeature> features = convertToTrackingFeatures(found);
        return new MaintenanceTrackingFeatureCollection(lastUpdated, lastChecked, features);
    }

    @Transactional(readOnly = true)
    public MaintenanceTrackingFeature getMaintenanceTrackingById(final long id) {
        final MaintenanceTrackingViewDto tracking = v2MaintenanceTrackingViewRepository.getOne(id);
        return convertToTrackingFeature(tracking);
    }

    @Transactional(readOnly = true)
    public List<JsonNode> findTrackingDataJsonsByTrackingId(final long trackingId) {
        return v2MaintenanceTrackingDataRepository.findJsonsByTrackingId(trackingId).stream().map(j -> {
            try {
                return objectMapper.readTree(j);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    private static List<MaintenanceTrackingFeature> convertToTrackingFeatures(final List<MaintenanceTrackingDto> trackings) {
        return trackings.stream().map(V2MaintenanceTrackingDataService::convertToTrackingFeature).collect(Collectors.toList());
    }

    private static List<MaintenanceTrackingLatestFeature> convertToTrackingLatestFeatures(final List<MaintenanceTrackingDto> trackings) {
        return trackings.stream().map(V2MaintenanceTrackingDataService::convertToTrackingLatestFeature).collect(Collectors.toList());
    }

    private static MaintenanceTrackingFeature convertToTrackingFeature(final MaintenanceTrackingDto tracking) {
        final Geometry<?> geometry = convertToGeoJSONGeometry(tracking, false);
        final MaintenanceTrackingProperties properties =
            new MaintenanceTrackingProperties(tracking.getId(),
                tracking.getWorkMachine(),
                toZonedDateTimeAtUtc(tracking.getSendingTime()),
                toZonedDateTimeAtUtc(tracking.getStartTime()),
                toZonedDateTimeAtUtc(tracking.getEndTime()),
                tracking.getTasks(), tracking.getDirection());
        return new MaintenanceTrackingFeature(geometry, properties);
    }

    public static MaintenanceTrackingLatestFeature convertToTrackingLatestFeature(final MaintenanceTrackingDto tracking) {
        final Geometry<?> geometry = convertToGeoJSONGeometry(tracking, true);
        final MaintenanceTrackingLatestProperties properties =
            new MaintenanceTrackingLatestProperties(tracking.getId(),
                                                    toZonedDateTimeAtUtc(tracking.getEndTime()),
                                                    tracking.getTasks(), tracking.getDirection());
        return new MaintenanceTrackingLatestFeature(geometry, properties);
    }
    /**
     *
     * @param tracking that contains the geometry
     * @param latestPointGeometry if true then only the latest point will be returned as the geometry.
     * @return either Point or LineString geometry
     */
    private static Geometry<?> convertToGeoJSONGeometry(final MaintenanceTrackingDto tracking, boolean latestPointGeometry) {
        if (latestPointGeometry || tracking.getLineString() == null || tracking.getLineString().getNumPoints() <= 1) {
            return PostgisGeometryHelper.convertToGeoJSONGeometry(tracking.getLastPoint());
        } else {
            return PostgisGeometryHelper.convertToGeoJSONGeometry(
                TopologyPreservingSimplifier.simplify(tracking.getLineString(), 0.00005));
        }
    }
}
