package fi.livi.digitraffic.tie.service.v1.camera;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.converter.CameraPreset2CameraDataConverter;
import fi.livi.digitraffic.tie.dao.v1.CameraPresetRepository;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraRootDataObjectDto;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

@ConditionalOnWebApplication
@Service
public class CameraDataService {
    private final CameraPresetRepository cameraPresetRepository;
    private final CameraPreset2CameraDataConverter cameraPreset2CameraDataConverter;

    @Autowired
    CameraDataService(final CameraPresetRepository cameraPresetRepository,
                      final CameraPreset2CameraDataConverter cameraPreset2CameraDataConverter) {
        this.cameraPresetRepository = cameraPresetRepository;
        this.cameraPreset2CameraDataConverter = cameraPreset2CameraDataConverter;
    }

    @Transactional(readOnly = true)
    public CameraRootDataObjectDto findPublishableCameraStationsData(final boolean onlyUpdateInfo) {
        final ZonedDateTime updated = DateHelper.toZonedDateTimeAtUtc(cameraPresetRepository.getLatestMeasurementTime());

        if (onlyUpdateInfo) {
            return new CameraRootDataObjectDto(updated);
        } else {
            return cameraPreset2CameraDataConverter.convert(
                    cameraPresetRepository.findByPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(),
                    updated);
        }
    }

    @Transactional(readOnly = true)
    public CameraRootDataObjectDto findPublishableCameraStationsData(final String cameraId) {
        final ZonedDateTime updated = DateHelper.toZonedDateTimeAtUtc(cameraPresetRepository.getLatestMeasurementTime());
        final List<CameraPreset> data = cameraPresetRepository
                .findByCameraIdAndPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId(cameraId);

        if (data.isEmpty()) {
            throw new ObjectNotFoundException("CameraStation", cameraId);
        }
        return cameraPreset2CameraDataConverter.convert(
                data,
                updated);
    }
}
