package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Rollback;

import com.fasterxml.jackson.core.JsonProcessingException;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.WorkMachineTrackingDto;

public class MaintenanceDataServiceTest extends AbstractWorkmachineDataServiceTest {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceDataServiceTest.class);

    @Test
    public void findAllNotHandledWorkMachineTrackings() throws IOException {
        readTrackingJsonAndSave("timegap", 3);
        List<WorkMachineTrackingDto> all =
            maintenanceDataService.findAllNotHandledWorkMachineTrackingsOldestFirst();
        log.info("all: {}", all);
    }

    @Test
    public void handleUnhandledWorkMachineTrackings() throws JsonProcessingException {
        log.info("handleUnhandledWorkMachineTrackings count {}",
                 maintenanceDataService.handleUnhandledWorkMachineTrackings(null));
    }

    /**
     * For development
     */
    @Rollback(false)
    @Test
    @Ignore("For manual integration testing")
    public void devTestConvertAllUnhandledWorkMachineTrackingsInDbToObservations() throws JsonProcessingException {
        int count = 0;
        do {
            count = maintenanceDataService.handleUnhandledWorkMachineTrackings(100);
            log.info("handleUnhandledWorkMachineTrackings handledCount={} trackings", count);
        } while (count > 0);
    }

    /**
     * For development
     */
    @Rollback(false)
    @Test
    @Ignore("For manual integration testing")
    public void devTestConvertNext100UnhandledWorkMachineTrackingsInDbToObservations() throws JsonProcessingException {
        final int count = maintenanceDataService.handleUnhandledWorkMachineTrackings(100);
        log.info("handleUnhandledWorkMachineTrackings handledCount={} trackings", count);
    }
}