package fi.livi.digitraffic.tie.service.v1.camera;

import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.CameraPresetHistoryRepository;
import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPresetHistory;

@ConditionalOnNotWebApplication
@Service
public class CameraPresetHistoryUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraPresetService.class);
    private final CameraPresetHistoryRepository cameraPresetHistoryRepository;
    private final CameraImageUpdateService cameraImageUpdateService;

    @Autowired
    public CameraPresetHistoryUpdateService(final CameraPresetHistoryRepository cameraPresetHistoryRepository,
                                            final CameraImageUpdateService cameraImageUpdateService) {
        this.cameraPresetHistoryRepository = cameraPresetHistoryRepository;
        this.cameraImageUpdateService = cameraImageUpdateService;
    }

    @Transactional
    public void saveHistory(final CameraPresetHistory history) {
        cameraPresetHistoryRepository.save(history);
    }

    @Transactional
    public int deleteAllWithPresetId(final String presetId) {
        return cameraPresetHistoryRepository.deleteByIdPresetId(presetId);
    }

    @Transactional
    public void updatePresetHistoryPublicityForCamera(final RoadStation rs) {
        // If statTime is null it means now -> no history to update or
        // if startTime is in the future -> no history to update
        if (rs.getPublicityStartTime() != null && !rs.getPublicityStartTime().isAfter(ZonedDateTime.now())) {
            final String cameraId = CameraHelper.convertNaturalIdToCameraId(rs.getNaturalId());
            log.info("method=updatePresetHistoryPublicityForCamera cameraId={} toPublic={} fromPublicityStartTime={}",
                cameraId, rs.internalIsPublic(), rs.getPublicityStartTime().toInstant());
            cameraPresetHistoryRepository.updatePresetHistoryPublicityForCameraId(
                cameraId, rs.internalIsPublic(), rs.getPublicityStartTime().toInstant());
        }
        // If camera is not public, hide current image
        if (!rs.isPublicNow()) {
            cameraImageUpdateService.hideCurrentImagesForCamera(rs);
        }
    }

    @Transactional
    public void deleteOlderThanHoursHistory(final int hours) {
        cameraPresetHistoryRepository.deleteOlderThanHours(hours);
    }
}
