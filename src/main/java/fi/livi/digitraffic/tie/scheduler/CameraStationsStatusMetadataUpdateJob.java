package fi.livi.digitraffic.tie.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.v1.camera.CameraStationUpdater;

/**
 * Updates only camera station's (RoadStation) metadata
 */
@DisallowConcurrentExecution
public class CameraStationsStatusMetadataUpdateJob extends SimpleUpdateJob {

    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) {
        final int csCount = cameraStationUpdater.updateCameraStationsStatuses();
        if (csCount > 0) {
            dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_METADATA_CHECK);
        log.info("updatedCount={} camera stations statuses", csCount);
    }
}
