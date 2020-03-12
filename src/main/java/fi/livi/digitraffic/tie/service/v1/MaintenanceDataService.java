package fi.livi.digitraffic.tie.service.v1;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.livi.digitraffic.tie.dao.v1.workmachine.WorkMachineTrackingRepository;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.ObservationFeature;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.WorkMachineTracking;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.WorkMachineTrackingDto;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.WorkMachineTrackingRecord;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;

/**
 * This service handles data from Harja to db as JSON work machine trackings.
 * {@link WorkMachineObservationService} converts this data to Digitraffic domain model.
 *
 * @see {@link WorkMachineObservationService}
 * @See <a href="https://github.com/finnishtransportagency/harja">https://github.com/finnishtransportagency/harja</a>
 */
@Service
public class MaintenanceDataService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceDataService.class);
    private final WorkMachineTrackingRepository workMachineTrackingRepository;
    private final ConversionService conversionService;
    private final WorkMachineObservationService workMachineObservationService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MaintenanceDataService(final WorkMachineTrackingRepository workMachineTrackingRepository,
                                  @Qualifier("conversionService")
                                  final ConversionService conversionService,
                                  final WorkMachineObservationService workMachineObservationService,
                                  final ObjectMapper objectMapper) {
        this.workMachineTrackingRepository = workMachineTrackingRepository;
        this.conversionService = conversionService;
        this.workMachineObservationService = workMachineObservationService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public WorkMachineTracking saveWorkMachineTrackingData(final TyokoneenseurannanKirjausRequestSchema tyokoneenseurannanKirjaus) throws JsonProcessingException {

        try {
            final WorkMachineTrackingRecord record = conversionService.convert(tyokoneenseurannanKirjaus, WorkMachineTrackingRecord.class);
            final WorkMachineTracking tracking = new WorkMachineTracking(record);
            workMachineTrackingRepository.save(tracking);
            log.info("method=saveWorkMachineTrackingData Saved={}", tracking);
            return tracking;
        } catch (Exception e) {
            log.error("method=saveWorkMachineTrackingData failed for JSON:\n{}",
                      objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tyokoneenseurannanKirjaus));
            throw e;
        }
    }

    @Transactional
    public int updateWorkMachineTrackingTypes() throws JsonProcessingException {
        return workMachineTrackingRepository.updateWorkMachineTrackingTypes();
    }

    @Transactional(readOnly = true)
    public List<WorkMachineTracking> findAll() {
        return workMachineTrackingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<WorkMachineTrackingDto> findAllNotHandledWorkMachineTrackingsOldestFirst() {
        return findAllNotHandledWorkMachineTrackingsOldestFirst(null);
    }

    @Transactional(readOnly = true)
    public List<WorkMachineTrackingDto> findAllNotHandledWorkMachineTrackingsOldestFirst(final Integer maxToFind) {
        return workMachineTrackingRepository.findUnhandeldOldestFirst(maxToFind);
    }

    @Transactional
    public int handleUnhandledWorkMachineTrackings(final Integer maxCountToHandle) throws JsonProcessingException {
        updateWorkMachineTrackingTypes();
        Map<Pair<Integer, Integer>, List<ObservationFeatureWrapper>> unhandledMap = findUnhandledTrakkingsOldestFirstMappedByHarjaWorkMachineAndContract(maxCountToHandle);

        return unhandledMap.entrySet().stream().mapToInt(value -> workMachineObservationService.convertUnhandledWorkMachineTrackingsToObservations(value)).sum();
    }

    @Transactional(readOnly = true)
    public Map<Pair<Integer, Integer>, List<ObservationFeatureWrapper>> findUnhandledTrakkingsOldestFirstMappedByHarjaWorkMachineAndContract(final Integer maxToFind) {

        List<WorkMachineTrackingDto> allNotHandled = findAllNotHandledWorkMachineTrackingsOldestFirst(maxToFind);
        Map<Pair<Integer, Integer>, List<ObservationFeatureWrapper>> result = allNotHandled.stream()
            .flatMap(workMachineTracking -> workMachineTracking.getRecord().getObservationFeatureCollection().getFeatures().stream()
                .map(f -> new ObservationFeatureWrapper(f, workMachineTracking.getId())))
            .collect(Collectors.groupingBy(a -> a.getHarjaTyokoneUrakkaIdPair()));
        return result;
    }

    public static class ObservationFeatureWrapper {
        private final ObservationFeature observationFeature;
        private final long workMachineTrackingId;

        public ObservationFeatureWrapper(final ObservationFeature observationFeature, final long workMachineTrackingId) {
            this.observationFeature = observationFeature;
            this.workMachineTrackingId = workMachineTrackingId;
        }

        public ObservationFeature getObservationFeature() {
            return observationFeature;
        }

        public Long getWorkMachineTrackingId() {
            return workMachineTrackingId;
        }

        public Pair<Integer, Integer> getHarjaTyokoneUrakkaIdPair() {
            return observationFeature.getHarjaTyokoneUrakkaIdPair();
        }
    }

}
