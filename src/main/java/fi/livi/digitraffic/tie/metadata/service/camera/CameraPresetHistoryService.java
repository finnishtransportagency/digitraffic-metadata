package fi.livi.digitraffic.tie.metadata.service.camera;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.CameraPresetHistoryRepository;
import fi.livi.digitraffic.tie.metadata.model.CameraPresetHistory;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

@Service
public class CameraPresetHistoryService {

    private CameraPresetHistoryRepository cameraPresetHistoryRepository;
    private final int historyMaxAgeHours;

    @Autowired
    public CameraPresetHistoryService(final CameraPresetHistoryRepository cameraPresetHistoryRepository,
                                      @Value("${dt.amazon.s3.weathercam.history.maxAgeHours}") final int historyMaxAgeHours) {
        this.cameraPresetHistoryRepository = cameraPresetHistoryRepository;
        this.historyMaxAgeHours = historyMaxAgeHours;
    }

    @Transactional
    public void saveHistory(final CameraPresetHistory history) {
        cameraPresetHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public CameraPresetHistory findHistory(final String presetId, final String versionId) {
        return cameraPresetHistoryRepository.findByIdPresetIdAndIdVersionId(presetId, versionId).orElse(null);
    }

    @Transactional(readOnly = true)
    public CameraPresetHistory findHistory(final String presetId, final ZonedDateTime atTime) {
        return cameraPresetHistoryRepository
            .findLatestByPresetIdAndTime(presetId, atTime.toInstant(), Instant.now().minus(historyMaxAgeHours, ChronoUnit.HOURS) )
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public CameraPresetHistory findLatestWithPresetId(final String presetId) {
        return cameraPresetHistoryRepository.findLatestByPresetId(presetId).orElse(null);
    }

    /** Orderer from oldest to newest */
    @Transactional(readOnly = true)
    public List<CameraPresetHistory> findAllByPresetId(final String presetId) {
        return cameraPresetHistoryRepository.findByIdPresetIdOrderByLastModifiedAsc(presetId);
    }

    @Transactional
    public int deleteAllWithPresetId(final String presetId) {
        return cameraPresetHistoryRepository.deleteByIdPresetId(presetId);
    }

    @Transactional
    public void updatePresetHistoryPublicityForCamera(final RoadStation rs) {
        // TODO DPO-462 get start time of public / not public state and update history acordingly
        // rs.isPublic() && rs.GetPublicStartTime() etc.?
        // getPresets and update presetHistory
    }
}