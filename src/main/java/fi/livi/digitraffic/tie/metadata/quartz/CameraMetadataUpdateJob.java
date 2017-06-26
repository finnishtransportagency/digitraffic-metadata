package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.data.service.CameraImageUpdateService;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraStationUpdater;

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
        cameraImageUpdateService.deleteAllImagesForNonPublishablePresets();
    }
}