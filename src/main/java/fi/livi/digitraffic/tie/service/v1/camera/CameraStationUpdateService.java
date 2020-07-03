package fi.livi.digitraffic.tie.service.v1.camera;

import static fi.livi.digitraffic.tie.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.WeatherStation;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.model.v1.camera.CameraType;
import fi.livi.digitraffic.tie.service.RoadStationService;
import fi.livi.digitraffic.tie.service.RoadStationUpdateService;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationService;

@ConditionalOnNotWebApplication
@Service
public class CameraStationUpdateService extends AbstractCameraStationAttributeUpdater {

    private static final Logger log = LoggerFactory.getLogger(AbstractCameraStationAttributeUpdater.class);

    private final CameraPresetService cameraPresetService;
    private final RoadStationUpdateService roadStationUpdateService;
    private final RoadStationService roadStationService;
    private final WeatherStationService weatherStationService;
    private final EntityManager entityManager;
    private final CameraPresetHistoryUpdateService cameraPresetHistoryUpdateService;

    @Autowired
    public CameraStationUpdateService(final CameraPresetService cameraPresetService,
                                      final RoadStationUpdateService roadStationUpdateService,
                                      final RoadStationService roadStationService,
                                      final WeatherStationService weatherStationService,
                                      final EntityManager entityManager,
                                      final CameraPresetHistoryUpdateService cameraPresetHistoryUpdateService) {
        this.cameraPresetService = cameraPresetService;
        this.roadStationUpdateService = roadStationUpdateService;
        this.roadStationService = roadStationService;
        this.weatherStationService = weatherStationService;
        this.entityManager = entityManager;
        this.cameraPresetHistoryUpdateService = cameraPresetHistoryUpdateService;
    }

    /**
     * Updates or inserts camera station and it's presets. Marks non existing presets as obsolete.
     *
     * @return Pair of updated and inserted count of presets
     */
    @Transactional
    public Pair<Integer, Integer> updateOrInsertRoadStationAndPresets(final KameraVO kamera, final List<EsiasentoVO> esiasentos) {
        Map<Long, CameraPreset> presets = cameraPresetService.findAllCameraPresetsByCameraLotjuIdMappedByPresetLotjuId(kamera.getId());
        int updated = 0;
        int inserted = 0;

        // DPO-567 and DPO-681: Obsolete all presets before upgrading. Preset's LotjuIds and directions might change once in a while
        // so we want to get rid of ghosts and overlapping presetIds.
        presets.values().forEach(CameraPreset::obsolete);
        entityManager.flush();

        for (EsiasentoVO esiasento : esiasentos) {

            final CameraPreset cameraPreset = presets.remove(esiasento.getId());

            // Existing preset
            if (cameraPreset != null) {

                final int hash = HashCodeBuilder.reflectionHashCode(cameraPreset);
                final String before = cameraPreset.toString();

                log.debug("Updating camera preset " + cameraPreset);

                if ( updateCameraPresetAtributes(kamera, esiasento, cameraPreset) ||
                    hash != HashCodeBuilder.reflectionHashCode(cameraPreset) ) {
                    log.info("Updated CameraPreset:\n{} -> \n{}", before, cameraPreset.toString());
                    updated++;
                    cameraPresetService.save(cameraPreset);
                }

            } else { // New preset

                final CameraPreset cp = new CameraPreset();

                RoadStation rs = roadStationService.findByTypeAndLotjuId(RoadStationType.CAMERA_STATION, kamera.getId());
                boolean roadStationNew = false;
                if (rs == null) {
                    rs = RoadStation.createCameraStation();
                    roadStationNew = true;
                }
                cp.setRoadStation(rs);

                updateCameraPresetAtributes(kamera, esiasento, cp);

                cameraPresetService.save(cp);
                if (roadStationNew) {
                    log.info("Created new {} and RoadStation {}", cp, rs);
                } else {
                    log.info("Created new {}", cp);
                }
                inserted++;
            }
        }

        final RoadStation rs = roadStationService.findByTypeAndLotjuId(RoadStationType.CAMERA_STATION, kamera.getId());
        // Can happen if camera is new and doesn't have any presets yet
        if (rs == null) {
            log.info("No presets for camera {}", ToStringHelper.toString(kamera));
        } else {
            cameraPresetHistoryUpdateService.updatePresetHistoryPublicityForCamera(rs);
        }
        return Pair.of(updated, inserted);
    }

    private boolean updateCameraPresetAtributes(final KameraVO kameraFrom, final EsiasentoVO esiasentoFrom,
                                                final CameraPreset to) {

        final int hash = HashCodeBuilder.reflectionHashCode(to);
        final String cameraId = CameraHelper.convertNaturalIdToCameraId(kameraFrom.getVanhaId().longValue());
        final String presetId = CameraHelper.convertCameraIdToPresetId(cameraId, esiasentoFrom.getSuunta());

        if ( to.getCameraId() != null && !to.getCameraId().equals(cameraId) ) {
            log.warn("Update camera preset ( toId={}, toPresetId={} ) toCameraId={} cameraId={}",
                to.getId(), to.getPresetId(), to.getCameraId(), cameraId);
            log.debug("Old preset: {}", ToStringHelper.toStringFull(to));
            log.debug("New kamera: {}", ToStringHelper.toStringFull(kameraFrom));
            log.debug("New esiasento: {}", ToStringHelper.toStringFull(esiasentoFrom));
        }

        // Preset properties
        if ( to.getPresetId() != null && !to.getPresetId().equals(presetId) ) {
            log.info("Old preset: {}", ToStringHelper.toStringFull(to));
            log.info("New kamera: {}", ToStringHelper.toStringFull(kameraFrom));
            log.info("New esiasento: {}", ToStringHelper.toStringFull(esiasentoFrom));
            if (!isPermanentlyDeletedKeruunTila(kameraFrom.getKeruunTila())) {
                log.warn("Update: CameraPresetId doesn't match toPresetId={} vs new presetId={}", to.getPresetId(), presetId);
            }
        }
        to.setPresetId(presetId);

        to.setLotjuId(esiasentoFrom.getId());

        if (isPermanentlyDeletedKeruunTila(kameraFrom.getKeruunTila()) ||
            Objects.equals(isPublic(esiasentoFrom), false)) {
            to.obsolete();
        } else {
            to.unobsolete();
        }
        to.setPresetOrder(esiasentoFrom.getJarjestys());
        to.setPublic(isPublic(esiasentoFrom));
        to.setInCollection(esiasentoFrom.isKeruussa());
        to.setCompression(esiasentoFrom.getKompressio());
        to.setLotjuId(esiasentoFrom.getId());
        to.setPresetName1(esiasentoFrom.getNimiEsitys());
        to.setPresetName2(esiasentoFrom.getNimiLaitteella());
        to.setDefaultDirection(esiasentoFrom.isOletussuunta());
        to.setResolution(esiasentoFrom.getResoluutio());
        to.setDirection(esiasentoFrom.getSuunta());

        // Camera properties
        to.setCameraId(cameraId);
        to.setCameraLotjuId(kameraFrom.getId());
        to.setCameraType(CameraType.convertFromKameraTyyppi(kameraFrom.getTyyppi()));

        final Long tsaLotjuId = kameraFrom.getLahinTiesaaAsemaId();
        if (tsaLotjuId != null) {
            if (to.getNearestWeatherStation() == null || !tsaLotjuId.equals(to.getNearestWeatherStation().getLotjuId())) {
                final WeatherStation nearestWs = weatherStationService.findWeatherStationByLotjuId(tsaLotjuId);
                if(nearestWs == null && !isPermanentlyDeletedKeruunTila(kameraFrom.getKeruunTila())) {
                    log.error("Could not set set nearest Weather Station for cameraPresetId={}. Weather station with lotjuId={} not found.",
                              to.getPresetId(), tsaLotjuId);
                }
                to.setNearestWeatherStation(nearestWs);
            }
        } else {
            to.setNearestWeatherStation(null);
        }

        // Update RoadStation
        try {
            final RoadStation rs = to.getRoadStation();
            final boolean updated = updateRoadStationAttributes(kameraFrom, rs);
            return updated || hash != HashCodeBuilder.reflectionHashCode(to);
        } catch (Exception e) {
            log.error("method=updateCameraPresetAtributes : Updating roadstation nimiFi=\"{}\" lotjuId={} naturalId={} keruunTila={} failed",
                kameraFrom.getNimiFi(), kameraFrom.getId(), kameraFrom.getVanhaId(), kameraFrom.getKeruunTila());
            throw e;
        }
    }

    /**
     * Updates Camera Station but not presets
     * @param kamera
     * @return true if camera station was changed
     */
    @Transactional
    public boolean updateCamera(final KameraVO kamera) {
        final boolean updated = roadStationUpdateService.updateRoadStation(kamera);

        final RoadStation rs = roadStationService.findByTypeAndLotjuId(RoadStationType.CAMERA_STATION, kamera.getId());
        // Update history every time in case JMS message handling has failed
        cameraPresetHistoryUpdateService.updatePresetHistoryPublicityForCamera(rs);
        return updated;
    }

    /**
     * Updates one preset from kamera and esiasento
     * @param esiasento
     * @param kamera
     * @return true if preset was changed
     */
    @Transactional
    public boolean updatePreset(final EsiasentoVO esiasento, final KameraVO kamera) {
        final CameraPreset preset = cameraPresetService.findCameraPresetByLotjuId(esiasento.getId());
        // Update history every time in case JMS message handling has failed
        boolean updated = updateCameraPresetAtributes(kamera, esiasento, preset);
        cameraPresetHistoryUpdateService.updatePresetHistoryPublicityForCamera(preset.getRoadStation());
        return updated;
    }
}
