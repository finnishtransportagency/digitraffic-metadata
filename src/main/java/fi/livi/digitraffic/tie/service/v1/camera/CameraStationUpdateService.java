package fi.livi.digitraffic.tie.service.v1.camera;

import static fi.livi.digitraffic.tie.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.model.v1.camera.CameraType;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.WeatherStation;
import fi.livi.digitraffic.tie.service.RoadStationService;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationService;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.KameraVO;

@Service
public class CameraStationUpdateService extends AbstractCameraStationAttributeUpdater {

    private static final Logger log = LoggerFactory.getLogger(AbstractCameraStationAttributeUpdater.class);

    private final CameraPresetService cameraPresetService;
    private final RoadStationService roadStationService;
    private final WeatherStationService weatherStationService;
    private final EntityManager entityManager;
    private final CameraPresetHistoryService cameraPresetHistoryService;

    @Autowired
    public CameraStationUpdateService(final CameraPresetService cameraPresetService,
                                      final RoadStationService roadStationService,
                                      final WeatherStationService weatherStationService,
                                      final EntityManager entityManager,
                                      final CameraPresetHistoryService cameraPresetHistoryService) {
        this.cameraPresetService = cameraPresetService;
        this.roadStationService = roadStationService;
        this.weatherStationService = weatherStationService;
        this.entityManager = entityManager;
        this.cameraPresetHistoryService = cameraPresetHistoryService;
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
            log.debug("Old preset: {}", ToStringBuilder.reflectionToString(to));
            log.debug("New kamera: {}", ToStringBuilder.reflectionToString(kameraFrom));
            log.debug("New esiasento: {}", ToStringBuilder.reflectionToString(esiasentoFrom));
        }

        // Preset properties
        if ( to.getPresetId() != null && !to.getPresetId().equals(presetId) ) {
            log.info("Old preset: {}", ToStringBuilder.reflectionToString(to));
            log.info("New kamera: {}", ToStringBuilder.reflectionToString(kameraFrom));
            log.info("New esiasento: {}", ToStringBuilder.reflectionToString(esiasentoFrom));
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
            // Update history every time in case JMS message handling has failed
            cameraPresetHistoryService.updatePresetHistoryPublicityForCamera(rs);

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
        return roadStationService.updateRoadStation(kamera);
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
        return updateCameraPresetAtributes(kamera, esiasento, preset);
    }
}