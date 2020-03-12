package fi.livi.digitraffic.tie.conf;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.tie.service.v1.MaintenanceDataService;

@ConditionalOnProperty(name = "maintenance.tracking.job.enabled", matchIfMissing = true)
@ConditionalOnNotWebApplication
@Component
public class MaintenanceDataJobConfiguration {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceDataJobConfiguration.class);

    private final MaintenanceDataService maintenanceDataService;

    @Autowired
    public MaintenanceDataJobConfiguration(final MaintenanceDataService maintenanceDataService) {
        this.maintenanceDataService = maintenanceDataService;
    }

    /**
     * This job extracts all unhandled maintenance trackings
     * from source JSON-format to db relations.
     */
    @Scheduled(fixedDelayString = "${maintenance.tracking.job.intervalMs}")
    public void handleUnhandledMaintenanceTrackings() throws JsonProcessingException {
        final StopWatch start = StopWatch.createStarted();
        int count = 0;
        int totalCount = 0;
        do {
            count = maintenanceDataService.handleUnhandledWorkMachineTrackings(100);
            totalCount += count;
            log.info("method=handleUnhandledWorkMachineTrackings handledCount={} trackings", count);
        } while (count > 0);
        log.info("method=handleUnhandledWorkMachineTrackings handledTotalCount={} trackings tookMs={}", totalCount, start.getTime());
    }
}
