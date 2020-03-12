package fi.livi.digitraffic.tie.service.v1.camera;

import static fi.livi.digitraffic.tie.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.LockingService;
import fi.livi.digitraffic.tie.service.RoadStationService;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuCameraStationMetadataService;

@ConditionalOnNotWebApplication
@Service
public class CameraStationUpdater {
    private static final Logger log = LoggerFactory.getLogger(CameraStationUpdater.class);

    private final LotjuCameraStationMetadataService lotjuCameraStationMetadataService;
    private final CameraStationUpdateService cameraStationUpdateService;
    private final CameraPresetService cameraPresetService;
    private final RoadStationService roadStationService;
    private final CameraMetadataUpdateLock lock;

    @Autowired
    public CameraStationUpdater(final LotjuCameraStationMetadataService lotjuCameraStationMetadataService,
                                final CameraStationUpdateService cameraStationUpdateService,
                                final CameraPresetService cameraPresetService,
                                final RoadStationService roadStationService,
                                final LockingService lockingService) {
        this.lotjuCameraStationMetadataService = lotjuCameraStationMetadataService;
        this.cameraStationUpdateService = cameraStationUpdateService;
        this.cameraPresetService = cameraPresetService;
        this.roadStationService = roadStationService;
        this.lock = new CameraMetadataUpdateLock(lockingService);
    }

    private class CameraMetadataUpdateLock {
        private final String lockName = CameraMetadataUpdateLock.class.getSimpleName();
        private final StopWatch stopWatch;
        private final LockingService lockingService;


        public CameraMetadataUpdateLock(final LockingService lockingService) {
            this.lockingService = lockingService;
            stopWatch = new StopWatch();
        }

        protected void lock() {
            lockingService.lock(lockName, 10000);
            stopWatch.start();
        }

        protected void unlock() {
            final long time = stopWatch.getTime();
            stopWatch.reset();
            lockingService.unlock(lockName);
            log.debug("method=unlock lockedTimeMs={}", time);
        }
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 450000)
    public boolean updateCameras() {
        log.info("method=updateCameras start");

        final Set<Long> camerasLotjuIds = lotjuCameraStationMetadataService.getKamerasLotjuids();
        final Pair<Integer, Integer> updatedInsertedCount =
            camerasLotjuIds.stream().map(lotjuId -> updateCameraStationAndPresets(lotjuId))
                .reduce(Pair.of(0, 0), (p1, p2) -> Pair.of(p1.getLeft() + p2.getLeft(), p1.getRight() + p2.getRight()));

        long obsoletePresets = cameraPresetService.obsoleteCameraPresetsExcludingCameraLotjuIds(camerasLotjuIds);
        long obsoletedRoadStations = cameraPresetService.obsoleteCameraRoadStationsWithoutPublishablePresets();

        log.info("obsoletedCameraPresetsCount={} CameraPresets that are not active", obsoletePresets);
        log.info("obsoletedRoadStationsCount={} Camera RoadStations without active presets", obsoletedRoadStations);
        log.info("updatedCameraPresetsCount={} CameraPresets", updatedInsertedCount.getLeft());
        log.info("insertedCameraPresetsCount={} CameraPresets", updatedInsertedCount.getRight());
        final boolean updatedCameras = updatedInsertedCount.getLeft() > 0 || updatedInsertedCount.getRight() > 0;
        log.info("method=updateCameras end updatedBoolean={}", updatedCameras);

        return updatedCameras;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    public int updateCameraStationsStatuses() {
        log.info("method=updateCameraStationsStatuses start");
        final Set<Long> kamerasLotjuids = lotjuCameraStationMetadataService.getKamerasLotjuids();
        return kamerasLotjuids.stream().collect(Collectors.summingInt(cameraLotjuId -> {
            log.info("method=updateCameraStationsStatuses start lotjuId={}", cameraLotjuId);
            return updateCameraStation(cameraLotjuId.longValue()) ? 1 : 0;
        }));
    }

    /**
     * @param cameraLotjuId to update
     * @return Pair of updated and inserted count of presets
     */
    private Pair<Integer, Integer> updateCameraStationAndPresets(final long cameraLotjuId) {
        lock.lock();
        try {
            log.debug("method=updateCameraStationAndPresets got the lock");
            final KameraVO kamera = lotjuCameraStationMetadataService.getKamera(cameraLotjuId);
            if (kamera == null) {
                log.error("No Camera with lotjuId={} found", cameraLotjuId);
                return Pair.of(0,0);
            } else if (!validate(kamera)) {
                return Pair.of(0,0);
            }
            final List<EsiasentoVO> eas = lotjuCameraStationMetadataService.getEsiasentos(cameraLotjuId);
            return cameraStationUpdateService.updateOrInsertRoadStationAndPresets(kamera, eas);

        } finally {
            lock.unlock();
        }
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 5000)
    public boolean updateCameraStationFromJms(final long cameraLotjuId) {
        log.info("method=updateCameraStationFromJms start lotjuId={}", cameraLotjuId);
        return updateCameraStation(cameraLotjuId);
    }

    private boolean updateCameraStation(final long cameraLotjuId) {

        // If camera station doesn't exist, we have to create it and the presets.
        if (roadStationService.findByTypeAndLotjuId(RoadStationType.CAMERA_STATION, cameraLotjuId) == null) {
            final Pair<Integer, Integer> updated = updateCameraStationAndPresets(cameraLotjuId);
            return updated.getLeft() > 0 || updated.getRight() > 0;
        }

        // Otherwise we update only the station
        lock.lock();
        try {
            log.debug("method=updateCameraStation got the lock lotjuId={}", cameraLotjuId);
            final KameraVO kamera = lotjuCameraStationMetadataService.getKamera(cameraLotjuId);
            if (kamera == null) {
                log.error("No Camera with lotjuId={} found", cameraLotjuId);
                return false;
            } else if (!validate(kamera)) {
                return false;
            }
            return cameraStationUpdateService.updateCamera(kamera);

        } finally {
            lock.unlock();
        }
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 5000)
    public boolean updateCameraPresetFromJms(final long presetLotjuId) {
        log.info("method=updateCameraPresetFromJms start lotjuId={}", presetLotjuId);
        final EsiasentoVO esiasento = lotjuCameraStationMetadataService.getEsiasento(presetLotjuId);

        if (esiasento == null) {
            log.error("No CameraPreset with lotjuId={} found", presetLotjuId);
            return false;
        }

        // If camera preset doesn't exist, we have to create it -> just update the whole station
        if (cameraPresetService.findCameraPresetByLotjuId(presetLotjuId) == null) {
            final Pair<Integer, Integer> updated = updateCameraStationAndPresets(esiasento.getKameraId());
            return updated.getLeft() > 0 || updated.getRight() > 0;
        }

        // Otherwise update only the given preset
        lock.lock();
        try {
            log.debug("method=updateCameraPreset got the lock lotjuId={}", presetLotjuId);
            final KameraVO kamera = lotjuCameraStationMetadataService.getKamera(esiasento.getKameraId());
            if (validate(kamera)) {
                return cameraStationUpdateService.updatePreset(esiasento, kamera);
            } else {
                return false;
            }

        } finally {
            lock.unlock();
        }
    }

    private boolean validate(final KameraVO kamera) {
        final boolean valid = kamera.getVanhaId() != null;
        if (!valid && !isPermanentlyDeletedKeruunTila(kamera.getKeruunTila())) {
            log.error("{} is invalid: has null vanhaId", ToStringHelper.toString(kamera));
        }
        return valid;
    }
}
