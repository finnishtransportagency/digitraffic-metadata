package fi.livi.digitraffic.tie.data.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.data.dao.WorkMachineTrackingRepository;
import fi.livi.digitraffic.tie.data.model.maintenance.WorkMachineTracking;
import fi.livi.digitraffic.tie.data.model.maintenance.WorkMachineTrackingRecord;
import fi.livi.digitraffic.tie.harja.TyokoneenseurannanKirjausRequestSchema;

@Service
public class MaintenanceDataService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceDataService.class);
    private final ObjectMapper objectMapper;
    private final WorkMachineTrackingRepository workMachineTrackingRepository;
    private final ConversionService conversionService;

    @Autowired
    public MaintenanceDataService(final ObjectMapper objectMapper,
                                  final WorkMachineTrackingRepository workMachineTrackingRepository,
                                  @Qualifier("mvcConversionService")
                                  final ConversionService conversionService) {
        this.objectMapper = objectMapper;
        this.workMachineTrackingRepository = workMachineTrackingRepository;
        this.conversionService = conversionService;
    }

    @Transactional
    public WorkMachineTracking saveWorkMachineTrackingData(final TyokoneenseurannanKirjausRequestSchema tyokoneenseurannanKirjaus) throws JsonProcessingException {

        final WorkMachineTrackingRecord record = conversionService.convert(tyokoneenseurannanKirjaus, WorkMachineTrackingRecord.class);
        final WorkMachineTracking tracking = new WorkMachineTracking(record);
        workMachineTrackingRepository.save(tracking);
        log.info("Saved WorkMachineTracking {}", tracking);
        return tracking;
    }

    @Transactional(readOnly = true)
    public List<WorkMachineTracking> findAll() {
        return workMachineTrackingRepository.findAll();
    }
}
