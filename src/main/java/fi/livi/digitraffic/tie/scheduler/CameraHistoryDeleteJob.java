package fi.livi.digitraffic.tie.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryUpdateService;

/**
 * Deletes camera history older than week
 */
@DisallowConcurrentExecution
public class CameraHistoryDeleteJob extends SimpleUpdateJob {
    @Autowired
    private CameraPresetHistoryUpdateService cameraPresetHistoryUpdateService;

    @Override
    protected void doExecute(final JobExecutionContext context) {
        // Keep 1 week history
        cameraPresetHistoryUpdateService.deleteOlderThanHoursHistory(24*7);
    }
}
