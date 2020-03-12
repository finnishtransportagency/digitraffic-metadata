package fi.livi.digitraffic.tie.service.v2.maintenance;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
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

import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceRealizationDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceRealizationRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTaskRepository;
import fi.livi.digitraffic.tie.external.harja.ReittitoteumanKirjausRequestSchema;
import fi.livi.digitraffic.tie.external.harja.entities.KoordinaattisijaintiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.ReittiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.ReittitoteumaSchema;
import fi.livi.digitraffic.tie.external.harja.entities.ReittitoteumatSchema;
import fi.livi.digitraffic.tie.external.harja.entities.TehtavatSchema;
import fi.livi.digitraffic.tie.helper.PostgisGeometryHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealization;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTask;
import fi.livi.digitraffic.tie.service.DataStatusService;

@Service
public class V2MaintenanceRealizationUpdateService {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceRealizationUpdateService.class);
    private final V2MaintenanceRealizationRepository v2RealizationRepository;
    private final V2MaintenanceRealizationDataRepository v2RealizationDataRepository;
    private final ObjectWriter jsonWriter;
    private final ObjectReader jsonReader;
    private final DataStatusService dataStatusService;

    private final Map<Long, MaintenanceTask> tasksMap;
    private final int distinctObservationGapMinutes;

    @Autowired
    public V2MaintenanceRealizationUpdateService(final V2MaintenanceRealizationRepository v2RealizationRepository,
                                                 final V2MaintenanceRealizationDataRepository v2RealizationDataRepository,
                                                 final ObjectMapper objectMapper,
                                                 final V2MaintenanceTaskRepository v2MaintenanceTaskRepository,
                                                 final DataStatusService dataStatusService,
                                                 @Value("${workmachine.tracking.distinct.observation.gap.minutes}")
                                                 final int distinctObservationGapMinutes) {
        this.v2RealizationRepository = v2RealizationRepository;
        this.v2RealizationDataRepository = v2RealizationDataRepository;
        this.jsonWriter = objectMapper.writerFor(ReittitoteumanKirjausRequestSchema.class);
        this.jsonReader = objectMapper.readerFor(ReittitoteumanKirjausRequestSchema.class);
        this.dataStatusService = dataStatusService;
        tasksMap = v2MaintenanceTaskRepository.findAll().stream().collect(Collectors.toMap(MaintenanceTask::getId, Function.identity()));
        this.distinctObservationGapMinutes = distinctObservationGapMinutes;
    }

    @Transactional
    public void saveNewWorkMachineRealization(final Long jobId, final ReittitoteumanKirjausRequestSchema reittitoteumanKirjaus) throws JsonProcessingException {
        final String json = jsonWriter.writeValueAsString(reittitoteumanKirjaus);
        MaintenanceRealizationData realization = new MaintenanceRealizationData(jobId, json);
        v2RealizationDataRepository.save(realization);
        log.debug("method=saveWorkMachineRealizationData jsonData={}", json);
    }

    @Transactional
    public long handleUnhandledRealizations(int maxToHandle) {
        final Stream<MaintenanceRealizationData> data = v2RealizationDataRepository.findUnhandled(maxToHandle);
        final long count = data.filter(this::handleWorkMachineRealization).count();
        if (count > 0) {
            dataStatusService.updateDataUpdated(DataType.MAINTENANCE_REALIZATION_DATA);
        }
        dataStatusService.updateDataUpdated(DataType.MAINTENANCE_REALIZATION_DATA_CHECKED);
        return count;
    }

    private boolean handleWorkMachineRealization(final MaintenanceRealizationData wmrd) {
        final ReittitoteumanKirjausRequestSchema kirjaus;
        try {
            kirjaus = jsonReader.readValue(wmrd.getJson());

            // Message info
            final String sendingSystem = kirjaus.getOtsikko().getLahettaja().getJarjestelma();
            final Integer messageId = kirjaus.getOtsikko().getViestintunniste().getId();
            final ZonedDateTime sendingTime = kirjaus.getOtsikko().getLahetysaika();

            // Holder for one task-set data
            final V2MaintenanceRealizationDataHolder currentDataHolder = new V2MaintenanceRealizationDataHolder(wmrd, sendingSystem, messageId, sendingTime);

            // Data is either in reittitoteuma or in reittitoteumat depending of sending system
            final List<ReittitoteumaSchema> toteumat = getReittitoteumas(kirjaus);

            // Route
            toteumat.forEach(reittitoteuma -> handleRoute(reittitoteuma.getReitti(), currentDataHolder));

            wmrd.updateStatusToHandled();

        } catch (Exception e) {
            log.error(String.format("HandleUnhandledRealizations failed for id %d", wmrd.getId()), e);
            wmrd.updateStatusToError();
            return false;
        }

        return true;
    }

    private void handleRoute(final List<ReittiSchema> reitti,
                             final V2MaintenanceRealizationDataHolder currentDataHolder) {
        reitti.forEach(r -> {
            final List<TehtavatSchema> tehtavat = r.getReittipiste().getTehtavat();

            if (isTransition(tehtavat) ) { // Transition -> no saving to db. Persis previous values if they exists.
                saveRealizationIfContainsValidLineString(currentDataHolder);
                currentDataHolder.resetCoordinatesAndTasks();
            } else {
                // If current has data and task is changed -> save and init new realization
                // If there is longer gap between times in saved points -> save and init new realization
                final ZonedDateTime nextCoordinateTime = r.getReittipiste().getAika();
                if (currentDataHolder.containsCoordinateData() &&
                    (isTasksChanged(tehtavat, currentDataHolder.getTaskids()) || !isNextCoordinateTimeAfterPreviousAndInsideLimit(currentDataHolder.getEndTime(), nextCoordinateTime) )) {
                    saveRealizationIfContainsValidLineString(currentDataHolder);
                    currentDataHolder.resetCoordinatesAndTasks();
                }

                final KoordinaattisijaintiSchema koordinaatit = r.getReittipiste().getKoordinaatit();

                final Coordinate pgPoint = PostgisGeometryHelper.createCoordinateWithZFromETRS89ToWGS84(koordinaatit.getX(), koordinaatit.getY(), koordinaatit.getZ());
                currentDataHolder.addCoordinate(pgPoint, nextCoordinateTime, getMaintenanceTasks(tehtavat, currentDataHolder.getRealizationData()));
            }
        });
        // At the end of each route save it and reset. That way we don't combine it to next realization that might
        // come from different machine
        saveRealizationIfContainsValidLineString(currentDataHolder);
        currentDataHolder.resetCoordinatesAndTasks();
    }

    private boolean isNextCoordinateTimeAfterPreviousAndInsideLimit(final ZonedDateTime previousCoordinateTime, final ZonedDateTime nextCoordinateTime) {
        // It's allowed for next to be same or after the previous time
        final boolean nextIsSameOrAfter = !nextCoordinateTime.isBefore(previousCoordinateTime);
        final boolean timeGapInsideTheLimit = ChronoUnit.MINUTES.between(previousCoordinateTime, nextCoordinateTime) <= distinctObservationGapMinutes;
        if (!nextIsSameOrAfter || !timeGapInsideTheLimit) {
            log.info("previousCoordinateTime: {}, nextCoordinateTime: {} nextIsSameOrAfter: {}, timeGapInsideTheLimit: {}", previousCoordinateTime, nextCoordinateTime, nextIsSameOrAfter, timeGapInsideTheLimit);
        }
        return  nextIsSameOrAfter && timeGapInsideTheLimit;
    }

    private void saveRealizationIfContainsValidLineString(final V2MaintenanceRealizationDataHolder holder) {
        if (holder.isValidLineString()) {

            final MaintenanceRealization realization = creteRealization(holder);
            v2RealizationRepository.save(realization);
            log.info("Saved MaintenanceRealization with {} coordinates", realization.getLineString().getNumPoints());
        } else if (holder.containsCoordinateData()){
            final String msg = String.format("RealizationData id %d invalid LineString size %d last coordinateIndex %d",
                holder.getRealizationData().getId(), holder.getCoordinates().size(), holder.getCoordinateIndex());
            log.error(msg);
            holder.getRealizationData().appendHandlingInfo(msg);
        }
    }

    private MaintenanceRealization creteRealization(final V2MaintenanceRealizationDataHolder holder) {
        final LineString lineString = PostgisGeometryHelper.createLineStringWithZ(holder.getCoordinates());

        return new MaintenanceRealization(holder.getRealizationData(), holder.getSendingSystem(), holder.getMessageId(),
                                          holder.getSendingTime(), holder.getStartTime(), holder.getEndTime(), lineString,
                                          holder.getTasks());
    }

    private boolean isTransition(List<TehtavatSchema> tehtavat) {
        return tehtavat.isEmpty();
    }

    private static boolean isTasksChanged(final List<TehtavatSchema> tehtavas, final Set<Long> previousTaskIds) {
        final Set<Long> newTaskIds = getTaskIds(tehtavas);
        final boolean changed = !newTaskIds.equals(previousTaskIds);
        if (changed) {
            log.info("Changed {} from {} to {}", changed,
                previousTaskIds.stream().map(Object::toString).collect(Collectors.joining(",")),
                newTaskIds.stream().map(Object::toString).collect(Collectors.joining(",")));
        }
        return changed;
    }

    private static Set<Long> getTaskIds(final List<TehtavatSchema> tehtavas) {
        return tehtavas.stream().filter(t -> t.getTehtava() != null).map(t -> t.getTehtava().getId().longValue()).collect(Collectors.toSet());
    }

    private List<MaintenanceTask> getMaintenanceTasks(final List<TehtavatSchema> tehtavat,
                                                      final MaintenanceRealizationData realizationData) {
        return tehtavat.stream()
            .map(t -> {
                final MaintenanceTask task = tasksMap.get(t.getTehtava().getId().longValue());
                if (task == null) {
                    final String msg = String.format("MaintenanceTask with id %d not found", t.getTehtava().getId());
                    log.error(msg);
                    realizationData.appendHandlingInfo(msg);
                }
                return task;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Gets reittitoteuma from reittitoteuma or reittitoteumat property
     */
    private static List<ReittitoteumaSchema> getReittitoteumas(final ReittitoteumanKirjausRequestSchema kirjaus) {
        return kirjaus.getReittitoteuma() != null ?
                    Collections.singletonList(kirjaus.getReittitoteuma()) :
                    kirjaus.getReittitoteumat().stream().map(ReittitoteumatSchema::getReittitoteuma).collect(Collectors.toList());
    }
}
