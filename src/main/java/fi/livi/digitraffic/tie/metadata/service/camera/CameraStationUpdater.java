package fi.livi.digitraffic.tie.metadata.service.camera;

import static fi.livi.digitraffic.tie.metadata.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.MetadataUpdatedMessageDto.UpdateType;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuCameraStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.KameraVO;

@ConditionalOnNotWebApplication
@Service
public class CameraStationUpdater {
    private static final Logger log = LoggerFactory.getLogger(CameraStationUpdater.class);

    private final LotjuCameraStationMetadataService lotjuCameraStationMetadataService;
    private final CameraStationUpdateService cameraStationUpdateService;
    private final CameraPresetService cameraPresetService;
    private final RoadStationService roadStationService;

    private final ReentrantLock lock = new ReentrantLock();

    @Autowired
    public CameraStationUpdater(final LotjuCameraStationMetadataService lotjuCameraStationMetadataService,
                                final CameraStationUpdateService cameraStationUpdateService,
                                final CameraPresetService cameraPresetService,
                                final RoadStationService roadStationService) {
        this.lotjuCameraStationMetadataService = lotjuCameraStationMetadataService;
        this.cameraStationUpdateService = cameraStationUpdateService;
        this.cameraPresetService = cameraPresetService;
        this.roadStationService = roadStationService;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 450000)
    public boolean updateCameras() {
        boolean updatedCameras;
        lock.lock();
        try {
            log.info("method=updateCameras got the lock");
            final Map<Long, Pair<KameraVO, List<EsiasentoVO>>> lotjuIdToKameraAndEsiasentos =
                lotjuCameraStationMetadataService.getLotjuIdToKameraAndEsiasentoMap();
            updatedCameras = updateCamerasAndPresets(lotjuIdToKameraAndEsiasentos);
            log.info("method=updateCameras end updated={}", updatedCameras);
        } finally {
            lock.unlock();
        }
        return updatedCameras;
    }

    private boolean updateCamerasAndPresets(final Map<Long, Pair<KameraVO, List<EsiasentoVO>>> lotjuIdToKameraAndEsiasentos) {
        final Set<Long> presetsLotjuIdsNotToObsolete = new HashSet<>();
        int updated = 0;
        int inserted = 0;
        int invalidCount = 0;

        for (Pair<KameraVO, List<EsiasentoVO>> kameraAndEsiasento : lotjuIdToKameraAndEsiasentos.values()) {

            final KameraVO kamera = kameraAndEsiasento.getLeft();
            final List<EsiasentoVO> esiasentos = kameraAndEsiasento.getRight();

            if (validate(kamera)) {
                presetsLotjuIdsNotToObsolete.addAll(esiasentos.stream().map(EsiasentoVO::getId).collect(Collectors.toList()));
                try {
                    Pair<Integer, Integer> updatedObsoleted =
                        cameraStationUpdateService.updateOrInsert(kamera, esiasentos);
                    updated += updatedObsoleted.getLeft();
                    inserted += updatedObsoleted.getRight();
                } catch (Exception e) {
                    log.error("Update or insert {} failed", ToStringHelper.toString(kamera));
                    throw e;
                }

            } else {
                invalidCount++;
            }
        }

        if (invalidCount > 0) {
            log.error("invalidCount={} invalid Kameras from LOTJU", invalidCount);
        }

        // camera presets in database, but not in server
        long obsoletePresets = cameraPresetService.obsoletePresetsExcludingLotjuIds(presetsLotjuIdsNotToObsolete);

        long obsoletedRoadStations = cameraPresetService.obsoleteCameraRoadStationsWithoutPublishablePresets();
        long nonOsoletedRoadStations = cameraPresetService.nonObsoleteCameraRoadStationsWithPublishablePresets();

        log.info("obsoletedCameraPresetsCount={} CameraPresets that are not active", obsoletePresets);
        log.info("obsoletedRoadStationsCount={} Camera RoadStations without active presets", obsoletedRoadStations);
        log.info("nonObsoletedCameraRoadStationsCount={} Camera RoadStations with active presets", nonOsoletedRoadStations);
        log.info("updatedCameraPresetsCount={} CameraPresets", updated);
        log.info("insertedCameraPresetsCount={} CameraPresets", inserted);

        return obsoletePresets > 0 || obsoletedRoadStations > 0 || nonOsoletedRoadStations > 0 || updated > 0 || inserted > 0;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    public int updateCameraStationsStatuses() {

        int updated = 0;
        lock.lock();
        try {
            log.info("method=updateCameraStationsStatuses got the lock");
            final List<KameraVO> allKameras = lotjuCameraStationMetadataService.getKameras();
            for (KameraVO from : allKameras) {
                try {
                    if (validate(from) && roadStationService.updateRoadStation(from)) {
                        updated++;
                    }
                } catch (Exception e) {
                    log.error("method=updateCameraStationsStatuses : Updating roadstation nimiFi=\"{}\" lotjuId={} naturalId={} keruunTila={} failed",
                        from.getNimiFi(), from.getId(), from.getVanhaId(), from.getKeruunTila());
                    throw e;
                }
            }
        } finally {
            lock.unlock();
        }
        return updated;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 5000)
    public boolean updateCameraStation(final long lotjuId, final UpdateType updateType) {

        lock.lock();
        try {
            log.info("method=updateCameraStation got the lock");
            final KameraVO kamera = lotjuCameraStationMetadataService.getKamera(lotjuId);

            if (!validate(kamera)) {
                return false;
            }

            if (UpdateType.INSERT.equals(updateType) ||
                roadStationService.findByTypeAndLotjuId(RoadStationType.CAMERA_STATION, lotjuId) == null) {
                // Update station and presets metadata
                final List<EsiasentoVO> eas = lotjuCameraStationMetadataService.getEsiasentos(lotjuId);
                Pair<Integer, Integer> result = cameraStationUpdateService.updateOrInsert(kamera, eas);
                return result.getKey() > 0 || result.getValue() > 0;
            } else {
                // Update only station metadata
                return cameraStationUpdateService.updateCamera(kamera);
            }
        } finally {
            lock.unlock();
        }
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 5000)
    public boolean updateCameraPreset(final Long lotjuId, final UpdateType updateType) {

        lock.lock();
        try {
            log.info("method=updateCameraPreset got the lock");
            final EsiasentoVO esiasento = lotjuCameraStationMetadataService.getEsiasento(lotjuId);
            if (UpdateType.INSERT.equals(updateType) ||
                cameraPresetService.findCameraPresetByLotjuId(lotjuId) == null) {
                // Update station and presets metadata
                return updateCameraStation(esiasento.getKameraId(), UpdateType.INSERT);
            } else {
                final KameraVO kamera = lotjuCameraStationMetadataService.getKamera(esiasento.getKameraId());
                if (validate(kamera)) {
                    return cameraStationUpdateService.updatePreset(esiasento, kamera);
                } else {
                    return false;
                }
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
