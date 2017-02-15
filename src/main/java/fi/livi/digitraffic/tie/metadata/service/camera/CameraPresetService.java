package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.converter.CameraPresetMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.CameraPresetRepository;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class CameraPresetService {

    private static final Logger log = LoggerFactory.getLogger(CameraPresetService.class);

    private final CameraPresetRepository cameraPresetRepository;
    private final CameraPresetMetadata2FeatureConverter cameraPresetMetadata2FeatureConverter;
    private final StaticDataStatusService staticDataStatusService;

    @Autowired
    CameraPresetService(final CameraPresetRepository cameraPresetRepository,
                        final CameraPresetMetadata2FeatureConverter cameraPresetMetadata2FeatureConverter,
                        final StaticDataStatusService staticDataStatusService) {
        this.cameraPresetRepository = cameraPresetRepository;
        this.cameraPresetMetadata2FeatureConverter = cameraPresetMetadata2FeatureConverter;
        this.staticDataStatusService = staticDataStatusService;
    }

    @Transactional(readOnly = true)
    public Map<Long, CameraPreset> findAllCameraPresetsMappedByLotjuId() {
        final List<CameraPreset> allStations = cameraPresetRepository.findAll();
        return allStations.stream().filter(cp -> cp.getLotjuId() != null).collect(Collectors.toMap(CameraPreset::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public List<CameraPreset> findAll() {
        return cameraPresetRepository.findAll();
    }

    @Transactional
    public CameraPreset save(final CameraPreset cameraPreset) {
        try {
            final CameraPreset value = cameraPresetRepository.save(cameraPreset);
            cameraPresetRepository.flush();
            return value;
        } catch (Exception e) {
            log.error("Could not save " + cameraPreset);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<CameraPreset> findAllCameraPresetsWithoutRoadStation() {
        return cameraPresetRepository.findAllCameraPresetsWithoutRoadStation();
    }

    @Transactional(readOnly = true)
    public CameraStationFeatureCollection findAllPublishableCameraStationsAsFeatureCollection(final boolean onlyUpdateInfo) {

        MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.CAMERA_STATION);

        return cameraPresetMetadata2FeatureConverter.convert(
                onlyUpdateInfo ?
                Collections.emptyList() :
                findAllPublishableCameraPresets(),
                updated != null ? updated.getUpdatedTime() : null);
    }

    @Transactional(readOnly = true)
    public CameraPreset findCameraPresetByPresetId(final String presetId) {
        return cameraPresetRepository.findCameraPresetByPresetId(presetId);
    }

    @Transactional(readOnly = true)
    public List<CameraPreset> findPublishableCameraPresetByLotjuIdIn(final Collection<Long> lotjuIds) {
        return cameraPresetRepository.findByPublishableIsTrueAndLotjuIdIn(lotjuIds);
    }

    @Transactional
    public List<CameraPreset> findAllPublishableCameraPresets() {
        return cameraPresetRepository.findByPublishableIsTrueAndRoadStationPublishableIsTrueOrderByPresetId();
    }

    public Map<String, List<CameraPreset>> findWithoutLotjuIdMappedByCameraId() {
        List<CameraPreset> all = cameraPresetRepository.findByCameraLotjuIdIsNullOrLotjuIdIsNull();
        return all.stream().collect(Collectors.groupingBy(CameraPreset::getCameraId));
    }
}
