package fi.livi.digitraffic.tie.service.v1.camera;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

import fi.livi.digitraffic.tie.dao.v1.CameraPresetHistoryRepository;
import fi.livi.digitraffic.tie.dao.v1.CameraPresetRepository;
import fi.livi.digitraffic.tie.dao.v1.RoadStationRepository;
import fi.livi.digitraffic.tie.dao.v1.WeatherStationRepository;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPresetHistory;

@Service
public class CameraPresetService {
    private final EntityManager entityManager;
    private final RoadStationRepository roadStationRepository;
    private final WeatherStationRepository weatherStationRepository;

    private static final Logger log = LoggerFactory.getLogger(CameraPresetService.class);

    private final CameraPresetHistoryRepository cameraPresetHistoryRepository;
    private final CameraPresetRepository cameraPresetRepository;

    @Autowired
    public CameraPresetService(final EntityManager entityManager, final CameraPresetRepository cameraPresetRepository, final RoadStationRepository roadStationRepository,
        final WeatherStationRepository weatherStationRepository, final CameraPresetHistoryRepository cameraPresetHistoryRepository) {
        this.entityManager = entityManager;
        this.roadStationRepository = roadStationRepository;
        this.weatherStationRepository = weatherStationRepository;
        this.cameraPresetRepository = cameraPresetRepository;
        this.cameraPresetHistoryRepository = cameraPresetHistoryRepository;
    }

    private CriteriaBuilder createCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    @Transactional(readOnly = true)
    public Map<Long, CameraPreset> findAllCameraPresetsMappedByLotjuId() {
        final List<CameraPreset> allStations = cameraPresetRepository.findAll();
        return allStations.stream().collect(Collectors.toMap(CameraPreset::getLotjuId, Function.identity()));
    }

    public CameraPreset save(final CameraPreset cameraPreset) {
        try {
            // Cascade none
            roadStationRepository.save(cameraPreset.getRoadStation());
            if (cameraPreset.getNearestWeatherStation() != null) {
                weatherStationRepository.save(cameraPreset.getNearestWeatherStation());
            }
            final CameraPreset saved = cameraPresetRepository.save(cameraPreset);
            // Without this detached entity errors occurs
            cameraPresetRepository.flush();
            return saved;
        } catch (final Exception e) {
            log.error("Could not save " + cameraPreset);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<CameraPreset> findAllPublishableCameraPresets() {
        return cameraPresetRepository.findByPublishableIsTrueAndRoadStationPublishableNowIsTrueOrderByPresetId();
    }

    @Transactional(readOnly = true)
    public List<String> findAllNotPublishableCameraPresetsPresetIds() {
        return cameraPresetRepository.findAllNotPublishableCameraPresetsPresetIds();
    }

    @Transactional(readOnly = true)
    public Map<Long, CameraPreset> findAllCameraPresetsByCameraLotjuIdMappedByPresetLotjuId(final Long cameraLotjuId) {
        final List<CameraPreset> all = cameraPresetRepository.findByRoadStation_LotjuId(cameraLotjuId);
        return all.stream().collect(Collectors.toMap(CameraPreset::getLotjuId, Function.identity()));
    }

    @Transactional
    public int obsoleteCameraPresetsExcludingCameraLotjuIds(final Set<Long> camerasLotjuIds) {
        final CriteriaBuilder cb = createCriteriaBuilder();
        final CriteriaUpdate<CameraPreset> update = cb.createCriteriaUpdate(CameraPreset.class);
        final Root<CameraPreset> root = update.from(CameraPreset.class);
        final EntityType<CameraPreset> rootModel = root.getModel();
        update.set("obsoleteDate", LocalDate.now());

        List<Predicate> predicates = new ArrayList<>();
        predicates.add( cb.isNull(root.get(rootModel.getSingularAttribute("obsoleteDate", LocalDate.class))));
        for (List<Long> ids : Iterables.partition(camerasLotjuIds, 1000)) {
            predicates.add(cb.not(root.get("cameraLotjuId").in(ids)));
        }
        update.where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(update).executeUpdate();
    }

    @Transactional
    public int obsoleteCameraRoadStationsWithoutPublishablePresets() {
        return cameraPresetRepository.obsoleteCameraRoadStationsWithoutPublishablePresets();
    }

    @Transactional(readOnly = true)
    public CameraPreset findCameraPresetByLotjuId(final long presetLotjuId) {
        return cameraPresetRepository.findByLotjuId(presetLotjuId);
    }

    @Transactional
    public void updateCameraPresetAndHistory(final long cameraPresetLotjuId, final boolean isImagePublic, final boolean isPresetPublic,
                                             final ImageUpdateInfo updateInfo) {
        final CameraPreset cameraPreset = findCameraPresetByLotjuId(cameraPresetLotjuId);
        // Update version data only if write has succeeded
        if (updateInfo.isSuccess()) {
            final CameraPresetHistory history =
                new CameraPresetHistory(cameraPreset.getPresetId(), updateInfo.getVersionId(), cameraPreset.getId(), updateInfo.getLastUpdated(),
                                        isImagePublic, updateInfo.getSizeBytes(), isPresetPublic);

            cameraPresetHistoryRepository.save(history);
        }
        // Preset can be public when camera is secret. If camera is secret then public presets are not returned by the api.
        if (cameraPreset.isPublic() != isPresetPublic) {
            cameraPreset.setPublic(isPresetPublic);
            cameraPreset.setPictureLastModified(updateInfo.getLastUpdated());
            log.info("method=updateCameraPreset cameraPresetId={} isPublicExternal from {} to {} lastModified={}",
                cameraPreset.getPresetId(), !isPresetPublic, isPresetPublic, updateInfo.getLastUpdated());
        } else if (updateInfo.isSuccess()) {
            cameraPreset.setPictureLastModified(updateInfo.getLastUpdated());
        }

        cameraPresetRepository.save(cameraPreset);
    }

    @Transactional(readOnly = true)
    public CameraPreset findCameraPresetByPresetId(String presetId) {
        return cameraPresetRepository.findByPresetId(presetId);
    }
}