package fi.livi.digitraffic.tie.service.v1;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.workmachine.WorkMachineObservationDao;
import fi.livi.digitraffic.tie.dao.v1.workmachine.WorkMachineObservationRepository;
import fi.livi.digitraffic.tie.dao.v1.workmachine.WorkMachineRepository;
import fi.livi.digitraffic.tie.dao.v1.workmachine.WorkMachineTrackingRepository;
import fi.livi.digitraffic.tie.model.v1.maintenance.WorkMachine;
import fi.livi.digitraffic.tie.model.v1.maintenance.WorkMachineObservation;
import fi.livi.digitraffic.tie.model.v1.maintenance.WorkMachineObservation.WorkMachineObservationType;
import fi.livi.digitraffic.tie.model.v1.maintenance.WorkMachineTask;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.ObservationFeature;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.PerformedTask;
import fi.livi.digitraffic.tie.service.v1.MaintenanceDataService.ObservationFeatureWrapper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;

/**
 * This service handles Harja data in Digitraffic domain model as work machine observations.
 *
 * @see {@link MaintenanceDataService}
 * @See <a href="https://github.com/finnishtransportagency/harja">https://github.com/finnishtransportagency/harja</a>
 */

@Service
public class WorkMachineObservationService {

    private static final Logger log = LoggerFactory.getLogger(WorkMachineObservationService.class);

    private final WorkMachineTrackingRepository workMachineTrackingRepository;
    private final WorkMachineObservationRepository workMachineObservationRepository;
    private final WorkMachineRepository workMachineRepository;
    private final WorkMachineObservationDao workMachineObservationDao;

    private final int distinctObservationGapMinutes;

    @Autowired
    public WorkMachineObservationService(final WorkMachineTrackingRepository workMachineTrackingRepository,
                                         final WorkMachineObservationRepository workMachineObservationRepository,
                                         final WorkMachineRepository workMachineRepository,
                                         final WorkMachineObservationDao workMachineObservationDao,
                                         @Value("${workmachine.tracking.distinct.observation.gap.minutes}")
                                         final int distinctObservationGapMinutes) {
        this.workMachineTrackingRepository = workMachineTrackingRepository;
        this.workMachineObservationRepository = workMachineObservationRepository;
        this.workMachineRepository = workMachineRepository;
        this.workMachineObservationDao = workMachineObservationDao;
        this.distinctObservationGapMinutes = distinctObservationGapMinutes;
    }

    @Transactional(readOnly = true)
    public List<WorkMachineObservation> findWorkMachineObservationsByWorkMachineHarjaIdAndHarjaUrakkaId(final long workMachineHarjaId, final long harjaUrakkaId) {
      return workMachineObservationRepository.findByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdOrderByUpdatedAscIdAsc(workMachineHarjaId, harjaUrakkaId);
    }

    @Transactional(readOnly = true)
    public WorkMachineObservation findLastWorkMachineObservationByWorkMachineHarjaIdAndHarjaUrakkaId(final long workMachineHarjaId, final long harjaUrakkaId) {
        return workMachineObservationRepository.findFirstByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdOrderByUpdatedDescIdDesc(workMachineHarjaId, harjaUrakkaId);
    }

    @Transactional(readOnly = true)
    public List<WorkMachineObservation> finAllWorkMachineObservations() {
        return workMachineObservationRepository.findAll(Sort.by("updated", "id"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int convertUnhandledWorkMachineTrackingsToObservations(
        final Map.Entry<Pair<Integer, Integer>, List<ObservationFeatureWrapper>> harjaMachineIdContractIdPairWithObservationFeature) {

        final int harjaWorkMachineId = harjaMachineIdContractIdPairWithObservationFeature.getKey().getLeft();
        final int harjaContractId = harjaMachineIdContractIdPairWithObservationFeature.getKey().getRight();

        final List<ObservationFeatureWrapper> observationFeatures = harjaMachineIdContractIdPairWithObservationFeature.getValue();

        log.info("method=convertUnhandledWorkMachineTrackingsToObservations observationFeatures size={}", observationFeatures.size());
        observationFeatures.forEach(observationFeatureWrapper -> {

            final ObservationFeature observationFeatureToHandle = observationFeatureWrapper.getObservationFeature();
            final long workMachineTrackingId = observationFeatureWrapper.getWorkMachineTrackingId();

            WorkMachineObservation lastObservation =
                findLastWorkMachineObservationByWorkMachineHarjaIdAndHarjaUrakkaId(harjaWorkMachineId, harjaContractId);

            final ZonedDateTime observationTime = observationFeatureToHandle.getProperties().getObservationTime();

            final List<PerformedTask> currentPerformedTasks = observationFeatureToHandle.getProperties().getPerformedTasks();

            boolean createNewObservation = isNewObservationNeeded(lastObservation, currentPerformedTasks, observationTime);

            if (createNewObservation)  {
                final WorkMachine machine = getOrCreateWorkMachine(harjaWorkMachineId, harjaContractId, observationFeatureToHandle.getProperties().getWorkMachine().getType());
                final Geometry.Type currentObservationGeometryType = observationFeatureToHandle.getGeometry().getType();
                lastObservation = new WorkMachineObservation(machine, observationTime,
                                                             WorkMachineObservationType.valueOf(currentObservationGeometryType));
            }

            lastObservation.setTransition(currentPerformedTasks.isEmpty());
            final Double direction = observationFeatureToHandle.getProperties().getDirection();
            lastObservation.setDirection(direction != null ? BigDecimal.valueOf(direction) : null);
            lastObservation.setUpdatedNow();

            if (lastObservation.getId() == null) {
                workMachineObservationRepository.save(lastObservation);
                log.info("Created new: {}", lastObservation);
            }

            addCoordinatesToObservationInDb(observationFeatureToHandle, lastObservation);

            workMachineObservationRepository.save(lastObservation);
            workMachineTrackingRepository.markHandled(workMachineTrackingId);
            log.info("Saved: {}", ToStringHelper.toStringFull(lastObservation, "coordinates"));
        });

        log.info("method=convertUnhandledWorkMachineTrackingsToObservations urakka={} tyokone={} unhandledCount={} observations={}", harjaContractId, harjaWorkMachineId, observationFeatures.size(), ToStringHelper.toStringFull(observationFeatures));
        return observationFeatures.size();
    }

    /**
     * Takes coordinates from parameter observationFeatureFrom and adds them to toObservation by inserting
     * them to db. Coordinates are not added directly to toObservation for performance reasons.
     * @param observationFeatureFrom Feature containing coordinates in Geometry object
     * @param toObservation to add coordinates for in db
     */
    private void addCoordinatesToObservationInDb(final ObservationFeature observationFeatureFrom,
                                                 final WorkMachineObservation toObservation) {

        final ZonedDateTime observationTime = observationFeatureFrom.getProperties().getObservationTime();
        final Geometry.Type currentObservationGeometryType = observationFeatureFrom.getGeometry().getType();
        final List<PerformedTask> currentPerformedTasks = observationFeatureFrom.getProperties().getPerformedTasks();
        final Long observationId = toObservation.getId();

        final List<WorkMachineTask.Task> currentTasks =
            currentPerformedTasks.stream().map(task -> WorkMachineTask.Task.valueOf(task.name())).collect(Collectors.toList());

        int nextCoordinateOrder = workMachineObservationDao.getLastCoordinateOrder(observationId) + 1;

        if (Geometry.Type.LineString.equals(currentObservationGeometryType)) {
            List<List<Double>> lineStringCoordinates = (List<List<Double>>) observationFeatureFrom.getGeometry().getCoordinates();
            Iterator<List<Double>> coordinateIterator = lineStringCoordinates.iterator();
            while (coordinateIterator.hasNext()) {

                List<Double> coordinates = coordinateIterator.next();
                if (!coordinateIterator.hasNext()) {
                    // Add time only to last item of list as it's closest to right
                    addNewCoordinateInDb(observationId, nextCoordinateOrder, coordinates, observationTime);
                } else {
                    addNewCoordinateInDb(observationId, nextCoordinateOrder, coordinates, null);
                }
                addNewTasksToCoordinateInDb(observationId, nextCoordinateOrder, currentTasks);
                nextCoordinateOrder++;
            }

            if (toObservation.getType().equals(WorkMachineObservationType.Point)) {
                toObservation.setObservationType(WorkMachineObservationType.LineString);
                log.info("Updated to LineString Observation {}", toObservation);
            }
        } else if (Geometry.Type.Point.equals(currentObservationGeometryType)) {
            if (toObservation.getType().equals(WorkMachineObservationType.Point)) {
                final List<Double> pointCoordinates = (List<Double>) observationFeatureFrom.getGeometry().getCoordinates();
                addNewCoordinateInDb(observationId, nextCoordinateOrder, pointCoordinates, observationTime);
                addNewTasksToCoordinateInDb(observationId, nextCoordinateOrder, currentTasks);
            } else {
                log.info("Observation is already LineString type, not adding Point coordinates");
            }
        }
    }

    /**
     * @param lastObservation last found observation in db for current harja work machine and harja contract
     * @param currentPerformedTasks performed tasks in new observation
     * @param currentObservationTime new observation's observation time
     * @return
     */
    private boolean isNewObservationNeeded(final WorkMachineObservation lastObservation, final List<PerformedTask> currentPerformedTasks,
                                           final ZonedDateTime currentObservationTime) {
        final boolean currentIsTransition = currentPerformedTasks.isEmpty();
        final boolean lastIsTransition = lastObservation != null ? lastObservation.isTransition() : false;
        // * TODO: if machine stays on same place for a long time then task has ended (30 min?)
        // Create new if
        // * There is no previous observation
        // * Observation changes from transition to work with tasks (or opposite)
        // * Time between observations is too long between observations with task
        // Don't care if transitions time gap is too long, we only wan't to track
        // observations with tasks.
        if (lastObservation == null) {
            log.info("method=isNewObservationNeeded Create new observation as previous doesn't exist");
            return true;
        } else if ( lastIsTransition != currentIsTransition ) {
            log.info("method=isNewObservationNeeded Create new observation as changes from {}", lastIsTransition ? "transition to task" : "task to transition");
            return true;
        } else if ( lastObservation.getLastObservationTime().plusMinutes(distinctObservationGapMinutes)
                        .isBefore(currentObservationTime) &&
                    !lastObservation.isTransition() &&
                    !currentIsTransition) {
            log.info("method=isNewObservationNeeded Create new observation as distinctObservationGapMinutes={} exceeded", distinctObservationGapMinutes);
            return true;
        }
        log.info("method=isNewObservationNeeded Not creating new observation as last observation exists");
        return false;
    }

    private void addNewTasksToCoordinateInDb(final long observationId, final int orderNumber,
                                             final List<WorkMachineTask.Task> currentTasks) {
        try {
            currentTasks.forEach(t ->
                workMachineObservationRepository.addCoordinateTaskToLastCoordinate(observationId, orderNumber, t.name()));
        } catch (Exception e) {
            log.error("Adding task to last work machine observation failed.", e);
        }
    }

    private void addNewCoordinateInDb(final long observationId,final int orderNumber, final List<Double> pointCoordinates, final ZonedDateTime observationTime) {
        workMachineObservationDao.addCoordinates(observationId, orderNumber, BigDecimal.valueOf(pointCoordinates.get(0)),
                                                 BigDecimal.valueOf(pointCoordinates.get(1)), observationTime);
    }

    private WorkMachine getOrCreateWorkMachine(final long harjaWorkMachineId, final long harjaContractId, final String type) {
        WorkMachine workmachine = workMachineRepository.findByHarjaIdAndHarjaUrakkaId(harjaWorkMachineId, harjaContractId);
        if (workmachine == null) {
            workmachine = new WorkMachine(harjaWorkMachineId, harjaContractId, type);
        } else {
            workmachine.setType(type);
        }
        workMachineRepository.save(workmachine);
        return workmachine;
    }

}
