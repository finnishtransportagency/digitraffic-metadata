package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask.UNKNOWN;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService.NextObservationStatus.Status.NEW;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService.NextObservationStatus.Status.SAME;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService.NextObservationStatus.Status.TRANSITION;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingWorkMachineRepository;
import fi.livi.digitraffic.tie.external.harja.Havainto;
import fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat;
import fi.livi.digitraffic.tie.external.harja.Tyokone;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.external.harja.entities.GeometriaSijaintiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.KoordinaattisijaintiSchema;
import fi.livi.digitraffic.tie.helper.PostgisGeometryHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingData;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingWorkMachine;
import fi.livi.digitraffic.tie.service.DataStatusService;

@Service
public class V2MaintenanceTrackingUpdateService {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceTrackingUpdateService.class);
    private final V2MaintenanceTrackingDataRepository v2MaintenanceTrackingDataRepository;
    private final V2MaintenanceTrackingWorkMachineRepository v2MaintenanceTrackingWorkMachineRepository;
    private final ObjectWriter jsonWriter;
    private final ObjectReader jsonReader;
    private final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository;
    private final DataStatusService dataStatusService;

    private final int distinctObservationGapMinutes;

    @Autowired
    public V2MaintenanceTrackingUpdateService(final V2MaintenanceTrackingDataRepository v2MaintenanceTrackingDataRepository,
                                              final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository,
                                              final V2MaintenanceTrackingWorkMachineRepository v2MaintenanceTrackingWorkMachineRepository,
                                              final ObjectMapper objectMapper,
                                              final DataStatusService dataStatusService,
                                              @Value("${workmachine.tracking.distinct.observation.gap.minutes}")
                                              final int distinctObservationGapMinutes) {
        this.v2MaintenanceTrackingDataRepository = v2MaintenanceTrackingDataRepository;
        this.v2MaintenanceTrackingWorkMachineRepository = v2MaintenanceTrackingWorkMachineRepository;
        this.jsonWriter = objectMapper.writerFor(TyokoneenseurannanKirjausRequestSchema.class);
        this.jsonReader = objectMapper.readerFor(TyokoneenseurannanKirjausRequestSchema.class);
        this.v2MaintenanceTrackingRepository = v2MaintenanceTrackingRepository;
        this.dataStatusService = dataStatusService;
        this.distinctObservationGapMinutes = distinctObservationGapMinutes;
    }

    @Transactional
    public void saveMaintenanceTrackingData(final TyokoneenseurannanKirjausRequestSchema tyokoneenseurannanKirjaus) throws JsonProcessingException {
        try {
            final String json = jsonWriter.writeValueAsString(tyokoneenseurannanKirjaus);
            final MaintenanceTrackingData tracking = new MaintenanceTrackingData(json);
            v2MaintenanceTrackingDataRepository.save(tracking);
            log.debug("method=saveMaintenanceTrackingData jsonData={}", json);
        } catch (Exception e) {
            log.error("method=saveMaintenanceTrackingData failed ", e);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public int handleUnhandledMaintenanceTrackingData(int maxToHandle) {
        final Stream<MaintenanceTrackingData> data = v2MaintenanceTrackingDataRepository.findUnhandled(maxToHandle);
        final int count = (int) data.filter(this::handleMaintenanceTrackingData).count();
        if (count > 0) {
            dataStatusService.updateDataUpdated(DataType.MAINTENANCE_TRACKING_DATA);
        }
        dataStatusService.updateDataUpdated(DataType.MAINTENANCE_TRACKING_DATA_CHECKED);
        return count;
    }

    private boolean handleMaintenanceTrackingData(final MaintenanceTrackingData trackingData) {
        try {
            final TyokoneenseurannanKirjausRequestSchema kirjaus = jsonReader.readValue(trackingData.getJson());

            // Message info
            final String sendingSystem = kirjaus.getOtsikko().getLahettaja().getJarjestelma();
            final ZonedDateTime sendingTime = kirjaus.getOtsikko().getLahetysaika();
            final List<Havainto> havaintos = getHavaintos(kirjaus);

            // Route
            havaintos.forEach(havainto -> handleRoute(havainto, trackingData, sendingSystem, sendingTime));

            trackingData.updateStatusToHandled();

        } catch (Exception e) {
            log.error(String.format("HandleUnhandledRealizations failed for id %d", trackingData.getId()), e);
            trackingData.updateStatusToError();
            trackingData.appendHandlingInfo(e.toString());
            return false;
        }

        return true;
    }



    private void handleRoute(final Havainto havainto,
                             final MaintenanceTrackingData trackingData, String sendingSystem, ZonedDateTime sendingTime) {

        final Geometry geometry = resolveGeometry(havainto.getSijainti());
        if (geometry != null && !geometry.isEmpty()) {

            final Tyokone harjaWorkMachine = havainto.getTyokone();
            final int harjaWorkMachineId = harjaWorkMachine.getId();
            final Integer harjaContractId = havainto.getUrakkaid();

            final MaintenanceTracking previousTracking =
                v2MaintenanceTrackingRepository
                    .findFirstByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdOrderByModifiedDescIdDesc(harjaWorkMachineId, harjaContractId);

            final NextObservationStatus status = resolveNextObservationStatus(previousTracking, havainto);
            final ZonedDateTime harjaObservationTime = havainto.getHavaintoaika();

            if ( status.is(TRANSITION) ) {
                log.info("WorkMachine tracking in transition");
                // Mark found one to finished as the work machine is in transition after that
                // Append latest point (without the task) to tracking if it's inside time limits.
                updateAsFinishedNullSafeAndAppendLastGeometry(previousTracking, geometry, getDirection(havainto, trackingData.getId()), harjaObservationTime, status.isNextInsideLimits());
            // If previous is finished or tasks has changed or time gap is too long, we create new tracking for the machine
            } else if ( status.is(NEW) ) {

                // Append latest point to tracking if it's inside time limits. This happens only when task changes and
                // last point will be new tasks first point.
                updateAsFinishedNullSafeAndAppendLastGeometry(previousTracking, geometry, getDirection(havainto, trackingData.getId()), harjaObservationTime, status.isNextInsideLimits());

                final MaintenanceTrackingWorkMachine workMachine =
                    getOrCreateWorkMachine(harjaWorkMachineId, harjaContractId, harjaWorkMachine.getTyokonetyyppi());
                final Point lastPoint = resolveLastPoint(geometry);
                final Set<MaintenanceTrackingTask> performedTasks = getMaintenanceTrackingTasksFromHarjaTasks(havainto.getSuoritettavatTehtavat());

                final MaintenanceTracking created =
                    new MaintenanceTracking(trackingData, workMachine, harjaContractId, sendingSystem, sendingTime,
                        harjaObservationTime, harjaObservationTime, lastPoint, geometry.getLength() > 0.0 ? (LineString) geometry : null,
                        performedTasks, getDirection(havainto, trackingData.getId()));
                v2MaintenanceTrackingRepository.save(created);
            } else {
                previousTracking.appendGeometry(geometry, harjaObservationTime, getDirection(havainto, trackingData.getId()));
                previousTracking.addWorkMachineTrackingData(trackingData);
            }
        }
    }

    private BigDecimal getDirection(final Havainto havainto, final long trackingDataId) {
        if (havainto.getSuunta() != null) {
            final BigDecimal value = BigDecimal.valueOf(havainto.getSuunta());
            if (value.intValue() > 360 || value.intValue() < 0) {
                log.error("Illegal direction value {} for trackingData id {}. Value should be between 0-360 degrees.", value, trackingDataId);
                return null;
            }
            return value;
        }
        return null;
    }

    private NextObservationStatus resolveNextObservationStatus(final MaintenanceTracking previousTracking, final Havainto havainto) {

        final Set<MaintenanceTrackingTask> performedTasks = getMaintenanceTrackingTasksFromHarjaTasks(havainto.getSuoritettavatTehtavat());
        final ZonedDateTime harjaObservationTime = havainto.getHavaintoaika();
        final boolean isNextInsideTheTimeLimit = isNextCoordinateTimeInsideTheLimitNullSafe(harjaObservationTime, previousTracking);
        final boolean isNextTimeSameOrAfter = isNextCoordinateTimeSameOrAfterPreviousNullSafe(harjaObservationTime, previousTracking);
        final boolean isTasksChanged = isTasksChangedNullSafe(performedTasks, previousTracking);

        if (isTransition(performedTasks)) {
            return new NextObservationStatus(TRANSITION, isNextInsideTheTimeLimit, isNextTimeSameOrAfter);
        } else if ( previousTracking == null ||
                    previousTracking.isFinished() ||
                    isTasksChanged ||
                    !isNextInsideTheTimeLimit ||
                    !isNextTimeSameOrAfter) {
            return new NextObservationStatus(NEW, isNextInsideTheTimeLimit, isNextTimeSameOrAfter);
        } else {
            return new NextObservationStatus(SAME, isNextInsideTheTimeLimit, isNextTimeSameOrAfter);
        }
    }

    private Set<MaintenanceTrackingTask> getMaintenanceTrackingTasksFromHarjaTasks(List<SuoritettavatTehtavat> harjaTasks) {
        return harjaTasks == null ? null : harjaTasks.stream()
            .map(tehtava -> {
                final MaintenanceTrackingTask task = MaintenanceTrackingTask.getByharjaEnumName(tehtava.name());
                if (task == UNKNOWN) {
                    log.error("Failed to convert SuoritettavatTehtavat {} to WorkMachineTask", tehtava.toString());
                }
                return task;
            })
            .collect(Collectors.toSet());
    }

    private void updateAsFinishedNullSafeAndAppendLastGeometry(final MaintenanceTracking trackingToFinish, final Geometry latestGeometry,
                                                               final BigDecimal direction, final ZonedDateTime latestGeometryOservationTime,
                                                               final boolean appendLatestGeometry) {
        if (trackingToFinish != null && !trackingToFinish.isFinished()) {
            if (appendLatestGeometry) {
                trackingToFinish.appendGeometry(latestGeometry, latestGeometryOservationTime, direction);
            }
            trackingToFinish.setFinished();
        }
    }

    /**
     *
     * @param geometry Must be either Point or LineString
     * @return Point it self or LineString's last point
     */
    private Point resolveLastPoint(final Geometry geometry) {
        if (geometry.getNumPoints() > 1) {
            final LineString lineString = (LineString) geometry;
            return lineString.getEndPoint();
        } else if (geometry.getNumPoints() == 1) {
            return (Point)geometry;
        }
        throw new IllegalArgumentException("Geometry " + geometry + " is not LineString of Point");
    }

    /**
     *
     * @param sijainti where to read geometry
     * @return either Point or LineString geometry, null if no geometry resolved
     */
    private Geometry resolveGeometry(final GeometriaSijaintiSchema sijainti) {

        List<Coordinate> coordinates = resolveCoordinates(sijainti);
        if (coordinates.isEmpty()) {
            return null;
        }
        if (coordinates.size() == 1) { // Point
            return PostgisGeometryHelper.createPointWithZ(coordinates.get(0));
        }
        return PostgisGeometryHelper.createLineStringWithZ(coordinates);

    }

    private List<Coordinate> resolveCoordinates(final GeometriaSijaintiSchema sijainti) {
        if (sijainti.getViivageometria() != null) {
            final List<List<Object>> lineStringCoords = sijainti.getViivageometria().getCoordinates();
            return lineStringCoords.stream().map(point -> {
                try {
                    final double x = (double) point.get(0);
                    final double y = (double) point.get(1);
                    final double z = point.size() > 2 ? Double.valueOf((Integer) point.get(2)) : 0.0;
                    final Coordinate coordinate = PostgisGeometryHelper.createCoordinateWithZFromETRS89ToWGS84(x, y, z);
                    log.debug("From ETRS89: [{}, {}, {}] -> WGS84: [{}, {}, {}}",
                             x, y, z, coordinate.getX(), coordinate.getY(), coordinate.getZ());
                    return PostgisGeometryHelper.createCoordinateWithZFromETRS89ToWGS84(x, y, z);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }).collect(Collectors.toList());
        } else if (sijainti.getKoordinaatit() != null) {
            final KoordinaattisijaintiSchema koordinaatit = sijainti.getKoordinaatit();
            final Coordinate coordinate = PostgisGeometryHelper.createCoordinateWithZFromETRS89ToWGS84(koordinaatit.getX(), koordinaatit.getY(), koordinaatit.getZ());
            log.debug("From ETRS89: [{}, {}, {}] -> WGS84: [{}, {}, {}}",
                     koordinaatit.getX(), koordinaatit.getY(), koordinaatit.getZ(),
                     coordinate.getX(), coordinate.getY(), coordinate.getZ());
            return Collections.singletonList(coordinate);
        }
        return Collections.emptyList();
    }

    private boolean isNextCoordinateTimeInsideTheLimitNullSafe(final ZonedDateTime nextCoordinateTime, final MaintenanceTracking previousTracking) {
        if (previousTracking != null) {
            final ZonedDateTime previousCoordinateTime = previousTracking.getEndTime();
            // It's allowed for next to be same or after the previous time
            final boolean timeGapInsideTheLimit =
                ChronoUnit.MINUTES.between(previousCoordinateTime, nextCoordinateTime) <= distinctObservationGapMinutes;
            if (!timeGapInsideTheLimit) {
                log.info("previousCoordinateTime: {}, nextCoordinateTime: {}, timeGapInsideTheLimit: {}",
                         previousCoordinateTime, nextCoordinateTime, timeGapInsideTheLimit);
            }
            return timeGapInsideTheLimit;
        }
        return false;
    }

    private boolean isNextCoordinateTimeSameOrAfterPreviousNullSafe(final ZonedDateTime nextCoordinateTime, final MaintenanceTracking previousTracking) {
        if (previousTracking != null) {
            final ZonedDateTime previousCoordinateTime = previousTracking.getEndTime();
            // It's allowed for next to be same or after the previous time
            final boolean nextIsSameOrAfter = !nextCoordinateTime.isBefore(previousCoordinateTime);
            if (!nextIsSameOrAfter) {
                log.info("previousCoordinateTime: {}, nextCoordinateTime: {} nextIsSameOrAfter: {}",
                         previousCoordinateTime, nextCoordinateTime, nextIsSameOrAfter);
            }
            return nextIsSameOrAfter;
        }
        return false;
    }

    private MaintenanceTrackingWorkMachine getOrCreateWorkMachine(final long harjaWorkMachineId, final long harjaContractId, final String workMachinetype) {
        final MaintenanceTrackingWorkMachine
            existingWorkmachine = v2MaintenanceTrackingWorkMachineRepository.findByHarjaIdAndHarjaUrakkaId(harjaWorkMachineId, harjaContractId);

        if (existingWorkmachine != null) {
            existingWorkmachine.setType(workMachinetype);
            return existingWorkmachine;
        } else {
            final MaintenanceTrackingWorkMachine
                createdWorkmachine = new MaintenanceTrackingWorkMachine(harjaWorkMachineId, harjaContractId, workMachinetype);
            v2MaintenanceTrackingWorkMachineRepository.save(createdWorkmachine);
            return createdWorkmachine;
        }
    }

    private boolean isTransition(Set<MaintenanceTrackingTask> tehtavat) {
        return tehtavat.isEmpty();
    }

    private static boolean isTasksChangedNullSafe(final Set<MaintenanceTrackingTask> newTasks, final MaintenanceTracking previousTracking) {
        if (previousTracking != null) {
            final Set<MaintenanceTrackingTask> previousTasks = previousTracking.getTasks();
            final boolean changed = !newTasks.equals(previousTasks);

            if (changed) {
                log.info("WorkMachineTrackingTask changed from {} to {}",
                    previousTasks.stream().map(Object::toString).collect(Collectors.joining(",")),
                    newTasks.stream().map(Object::toString).collect(Collectors.joining(",")));
            }
            return changed;
        }
        return false;
    }

    /**
     * Gets reittitoteuma from reittitoteuma or reittitoteumat property
     * @return
     */
    private static List<Havainto> getHavaintos(final TyokoneenseurannanKirjausRequestSchema kirjaus) {
        return kirjaus.getHavainnot().stream().map(h -> h.getHavainto()).collect(Collectors.toList());
    }

    static class NextObservationStatus {

        public enum Status {
            TRANSITION,
            NEW,
            SAME
        }

        private final Status status;
        private final boolean nextInsideTheTimeLimit;
        private final boolean nextTimeSameOrAfterPrevious;

        private NextObservationStatus(
            Status status, boolean nextInsideTheTimeLimit, boolean nextTimeSameOrAfterPrevious) {
            this.status = status;
            this.nextInsideTheTimeLimit = nextInsideTheTimeLimit;
            this.nextTimeSameOrAfterPrevious = nextTimeSameOrAfterPrevious;
        }

        public Status getStatus() {
            return status;
        }

        public boolean isNextInsideTheTimeLimit() {
            return nextInsideTheTimeLimit;
        }

        public boolean isNextTimeSameOrAfterPrevious() {
            return nextTimeSameOrAfterPrevious;
        }

        public boolean isNextInsideLimits() {
            return nextInsideTheTimeLimit && nextTimeSameOrAfterPrevious;
        }

        public boolean is(final Status isStatus) {
            return status.equals(isStatus);
        }
    }
}