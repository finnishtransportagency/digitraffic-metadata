package fi.livi.digitraffic.tie.service.v1.tms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.TmsStation;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuTmsStationMetadataService;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamLaskennallinenAnturiVO;

@ConditionalOnNotWebApplication
@Service
public class TmsStationsSensorsUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsStationsSensorsUpdater.class);

    private final RoadStationSensorService roadStationSensorService;
    private final TmsStationService tmsStationService;
    private final DataStatusService dataStatusService;
    private final LotjuTmsStationMetadataService lotjuTmsStationMetadataService;

    @Autowired
    public TmsStationsSensorsUpdater(final RoadStationSensorService roadStationSensorService,
                                     final TmsStationService tmsStationService,
                                     final DataStatusService dataStatusService,
                                     final LotjuTmsStationMetadataService lotjuTmsStationMetadataService) {
        this.roadStationSensorService = roadStationSensorService;
        this.tmsStationService = tmsStationService;
        this.dataStatusService = dataStatusService;
        this.lotjuTmsStationMetadataService = lotjuTmsStationMetadataService;
    }

    /**
     * Updates all available sensors of tms road stations
     */
    public boolean updateTmsStationsSensors() {
        log.info("Update TMS Stations Sensors start");

        // Update sensors of road stations
        // Get current TmsStations
        final Map<Long, TmsStation> currentTmsStationMappedByByLotjuId =
                tmsStationService.findAllTmsStationsMappedByByLotjuId();

        final Set<Long> tmsLotjuIds = currentTmsStationMappedByByLotjuId.keySet();

        log.info("Fetching LamLaskennallinenAnturis for tmsCount={} LamAsemas", tmsLotjuIds.size());

        final Map<Long, List<LamLaskennallinenAnturiVO>> anturisMappedByAsemaLotjuId =
                lotjuTmsStationMetadataService.getLamLaskennallinenAnturisMappedByAsemaLotjuId(tmsLotjuIds);

        final List<Pair<TmsStation,  List<LamLaskennallinenAnturiVO>>> stationAnturisPairs = new ArrayList<>();
        currentTmsStationMappedByByLotjuId.values().forEach(tmsStation -> {
            final List<LamLaskennallinenAnturiVO> anturis = anturisMappedByAsemaLotjuId.remove(tmsStation.getLotjuId());
            if (anturis != null) {
                stationAnturisPairs.add(Pair.of(tmsStation, anturis));
            } else {
                log.info("No anturis for {}", tmsStation);
                stationAnturisPairs.add(Pair.of(tmsStation, Collections.emptyList()));
            }
        });

        // Update sensors of road stations
        final boolean updateStaticDataStatus = updateSensorsOfTmsStations(stationAnturisPairs);

        log.info("Update TMS Stations Sensors end");
        return updateStaticDataStatus;
    }

    private boolean updateSensorsOfTmsStations(final List<Pair<TmsStation,  List<LamLaskennallinenAnturiVO>>> stationAnturisPairs) {

        int countAdded = 0;
        int countRemoved = 0;

        for (final Pair<TmsStation, List<LamLaskennallinenAnturiVO>> pair : stationAnturisPairs) {
            final TmsStation tms = pair.getKey();
            final List<LamLaskennallinenAnturiVO> anturis = pair.getRight();
            try {
                final List<Long> sensorslotjuIds = anturis.stream().map(LamLaskennallinenAnturiVO::getId).collect(Collectors.toList());
                final Pair<Integer, Integer> deletedInserted = roadStationSensorService.updateSensorsOfWeatherStations(tms.getRoadStationId(),
                    RoadStationType.TMS_STATION,
                    sensorslotjuIds);
                countRemoved += deletedInserted.getLeft();
                countAdded += deletedInserted.getRight();
            } catch (final Exception e) {
                log.info("Anturis count={}", anturis);
                throw e;
            }
        }

        log.info("Sensor removed from road stations countRemoved={}", countRemoved);
        log.info("Sensor added to road stations countAdded={}", countAdded);

        return countRemoved > 0 || countAdded > 0;
    }
}
