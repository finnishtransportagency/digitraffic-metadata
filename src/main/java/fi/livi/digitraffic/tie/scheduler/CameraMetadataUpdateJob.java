package fi.livi.digitraffic.tie.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.service.v1.camera.CameraImageUpdateService;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.v1.camera.CameraStationUpdater;

/**
 * Updates camera station's and preset's (RoadStation + CameraPreset) metadata
 */
@DisallowConcurrentExecution
public class CameraMetadataUpdateJob extends SimpleUpdateJob {
    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @Autowired
    private CameraImageUpdateService cameraImageUpdateService;

    @Override
    protected void doExecute(JobExecutionContext context) {
        if (cameraStationUpdater.updateCameras()) {
            dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.CAMERA_STATION_METADATA_CHECK);
    }
}
