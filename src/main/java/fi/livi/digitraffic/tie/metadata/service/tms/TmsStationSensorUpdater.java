package fi.livi.digitraffic.tie.metadata.service.tms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.DataValidityHelper;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.AbstractRoadStationSensorUpdater;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuTmsStationClient;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamLaskennallinenAnturiVO;

@Service
public class TmsStationSensorUpdater extends AbstractRoadStationSensorUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsStationSensorUpdater.class);

    private final LotjuTmsStationClient lotjuTmsStationClient;

    @Autowired
    public TmsStationSensorUpdater(final RoadStationSensorService roadStationSensorService,
                                   final LotjuTmsStationClient lotjuTmsStationClient) {
        super(roadStationSensorService);
        this.lotjuTmsStationClient = lotjuTmsStationClient;
    }

    /**
     * Updates all available tms road station sensors
     */
    @Transactional
    public boolean updateRoadStationSensors() {
        log.info("Update TMS RoadStationSensors start");

        if (lotjuTmsStationClient == null) {
            log.warn("Not updating TMS stations sensors because lotjuTmsStationClient not defined");
            return false;
        }

        // Update available RoadStationSensors types to db
        List<LamLaskennallinenAnturiVO> allLamLaskennallinenAnturis =
                lotjuTmsStationClient.getAllLamLaskennallinenAnturis();

        boolean fixedLotjuIds = fixRoadStationSensorsWithoutLotjuId(allLamLaskennallinenAnturis);

        boolean updated = updateAllRoadStationSensors(allLamLaskennallinenAnturis);
        log.info("Update TMS RoadStationSensors end");
        return fixedLotjuIds || updated;
    }

    private boolean fixRoadStationSensorsWithoutLotjuId(final List<LamLaskennallinenAnturiVO> allLamLaskennallinenAnturis) {
        final Map<Long, RoadStationSensor> currentNaturalIdToSensorMap =
                roadStationSensorService.findAllRoadStationSensorsWithOutLotjuIdMappedByNaturalId(RoadStationType.TMS_STATION);

        final List<Pair<LamLaskennallinenAnturiVO, RoadStationSensor>> update = new ArrayList<>(); // Sensors to update
        final List<LamLaskennallinenAnturiVO> insert = new ArrayList<>(); // New sensors

        final AtomicInteger updated = new AtomicInteger(0);
        allLamLaskennallinenAnturis.stream().filter(anturi -> validate(anturi) ).forEach(anturi -> {
            final RoadStationSensor currentSaved = currentNaturalIdToSensorMap.remove(Long.valueOf(anturi.getVanhaId()));
            if ( currentSaved != null ) {
                currentSaved.setLotjuId(anturi.getId());
            }
        });

        // Obsolete not found stations
        final AtomicInteger obsoleted = new AtomicInteger(0);
        currentNaturalIdToSensorMap.values().stream().forEach(sensor -> {
            if (sensor.obsolete()) {
                obsoleted.addAndGet(1);
            }
        });

        log.info("Obsoleted {} RoadStationSensor", obsoleted);
        log.info("Fixed {} RoadStationSensor without lotjuId", updated);
        return obsoleted.get() > 0 || updated.get() > 0;
    }

    private boolean updateAllRoadStationSensors(final List<LamLaskennallinenAnturiVO> allLamLaskennallinenAnturis) {
        final Map<Long, RoadStationSensor> sensorsMappedByLotjuId =
                roadStationSensorService.findAllRoadStationSensorsMappedByLotjuId(RoadStationType.TMS_STATION);

        final List<Pair<LamLaskennallinenAnturiVO, RoadStationSensor>> update = new ArrayList<>(); // Sensors to update
        final List<LamLaskennallinenAnturiVO> insert = new ArrayList<>(); // New sensors

        final AtomicInteger invalid = new AtomicInteger(0);
        allLamLaskennallinenAnturis.stream().forEach(anturi -> {
            if (validate(anturi)) {
                final RoadStationSensor currentSaved = sensorsMappedByLotjuId.remove(anturi.getId());
                if ( currentSaved != null ) {
                    update.add(Pair.of(anturi, currentSaved));
                } else {
                    insert.add(anturi);
                }
            } else {
                invalid.addAndGet(1);
            }
        });

        if (invalid.get() > 0) {
            log.warn("Found {} LamLaskennallinenAnturi from LOTJU", invalid);
        }

        final int obsoleted = obsoleteRoadStationSensors(sensorsMappedByLotjuId.values());
        final int updated = updateRoadStationSensors(update);
        final int inserted = insertRoadStationSensors(insert);

        log.info("Obsoleted {} RoadStationSensors", obsoleted);
        log.info("Updated {} RoadStationSensors", update);
        log.info("Inserted {} RoadStationSensors", insert);

        if (insert.size() > inserted) {
            log.warn("Insert failed for {} RoadStationSensors", (insert.size()-inserted));
        }

        return obsoleted > 0 || inserted > 0 || updated > 0;
    }

    private static boolean validate(LamLaskennallinenAnturiVO anturi) {
        return anturi.getId() != null && anturi.getVanhaId() != null;
    }

    private int insertRoadStationSensors(final List<LamLaskennallinenAnturiVO> insert) {

        int counter = 0;
        for (final LamLaskennallinenAnturiVO anturi : insert) {
            RoadStationSensor sensor = new RoadStationSensor();
            updateRoadStationSensorAttributes(anturi, sensor);
            sensor = roadStationSensorService.saveRoadStationSensor(sensor);
            log.info("Created new " + sensor);
            counter++;
        }
        return counter;
    }

    private static int updateRoadStationSensors(final List<Pair<LamLaskennallinenAnturiVO, RoadStationSensor>> update) {

        int counter = 0;
        for (final Pair<LamLaskennallinenAnturiVO, RoadStationSensor> pair : update) {

            final LamLaskennallinenAnturiVO anturi = pair.getLeft();
            final RoadStationSensor sensor = pair.getRight();
            log.debug("Updating " + sensor);

            final String before = ReflectionToStringBuilder.toString(sensor);
            if ( updateRoadStationSensorAttributes(anturi, sensor) ) {
                counter++;
                log.info("Updated RoadStationSensor:\n" + before + " -> \n" + ReflectionToStringBuilder.toString(sensor));
            }
        }
        return counter;
    }

    private static boolean updateRoadStationSensorAttributes(final LamLaskennallinenAnturiVO from, final RoadStationSensor to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        to.setRoadStationType(RoadStationType.TMS_STATION);
        to.setObsolete(false);
        to.setObsoleteDate(null);

        to.setLotjuId(from.getId());
        to.setNaturalId(from.getVanhaId());
        if (to.getName() == null) {
            to.setName(from.getNimi());
        }
        to.setNameFi(from.getNimi());
        to.setShortNameFi(from.getLyhytNimi());
        to.setDescription(from.getKuvaus());
        to.setAccuracy(from.getTarkkuus());
        to.setUnit(DataValidityHelper.nullifyUnknownValue(from.getYksikko()));

        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }
}
