package fi.livi.digitraffic.tie.service.v1.camera;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.feature.CameraPresetMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeatureCollection;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;

@ConditionalOnWebApplication
@Service
public class CameraWebService {
    private static final Logger log = LoggerFactory.getLogger(CameraWebService.class);

    private final CameraPresetMetadata2FeatureConverter cameraPresetMetadata2FeatureConverter;
    private final CameraPresetService cameraPresetService;
    private final DataStatusService dataStatusService;

    @Autowired
    public CameraWebService(final CameraPresetMetadata2FeatureConverter cameraPresetMetadata2FeatureConverter,
                            final CameraPresetService cameraPresetService,
                            final DataStatusService dataStatusService) {
        this.cameraPresetMetadata2FeatureConverter = cameraPresetMetadata2FeatureConverter;
        this.cameraPresetService = cameraPresetService;
        this.dataStatusService = dataStatusService;
    }

    @Transactional(readOnly = true)
    public CameraStationFeatureCollection findAllPublishableCameraStationsAsFeatureCollection(final boolean onlyUpdateInfo) {
        return cameraPresetMetadata2FeatureConverter.convert(
                onlyUpdateInfo ?
                Collections.emptyList() :
                cameraPresetService.findAllPublishableCameraPresets(),
                dataStatusService.findDataUpdatedTime(DataType.CAMERA_STATION_METADATA),
                dataStatusService.findDataUpdatedTime(DataType.CAMERA_STATION_METADATA_CHECK));
    }

}