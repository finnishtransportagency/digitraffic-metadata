package fi.livi.digitraffic.tie.service.v1.tms;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuTmsStationMetadataService;

@ConditionalOnNotWebApplication
@Service
public class TmsStationSensorConstantUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsStationSensorConstantUpdater.class);

    private final TmsStationSensorConstantService tmsStationSensorConstantService;
    private final TmsStationService tmsStationService;
    private final DataStatusService dataStatusService;
    private final LotjuTmsStationMetadataService lotjuTmsStationMetadataService;

    @Autowired
    public TmsStationSensorConstantUpdater(final TmsStationSensorConstantService tmsStationSensorConstantService,
                                           final TmsStationService tmsStationService,
                                           final DataStatusService dataStatusService,
                                           final LotjuTmsStationMetadataService lotjuTmsStationMetadataService) {
        this.tmsStationSensorConstantService = tmsStationSensorConstantService;
        this.tmsStationService = tmsStationService;
        this.dataStatusService = dataStatusService;
        this.lotjuTmsStationMetadataService = lotjuTmsStationMetadataService;
    }

    /**
     * Updates all available sensorConstants of tms road stations
     */
    public boolean updateTmsStationsSensorConstants() {
        log.info("Update TMS Stations SensorConstants start");

        // Get current TmsStations
        final List<Long> tmsLotjuIds =
            tmsStationService.findAllTmsStationsLotjuIds();

        log.info("Fetching LamAnturiVakios for tmsCount={} LamAsemas", tmsLotjuIds.size());

        List<LamAnturiVakioVO> allLamAnturiVakios = lotjuTmsStationMetadataService.getAllLamAnturiVakios(tmsLotjuIds);

        final boolean updated = tmsStationSensorConstantService.updateSensorConstants(allLamAnturiVakios);

        if (updated) {
            dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_METADATA_CHECK);

        log.info("Update TMS Stations SensorConstants end");
        return updated;
    }

    /**
     * Updates all available sensorConstants of tms road stations
     */
    public boolean updateTmsStationsSensorConstantsValues() {
        log.info("Update TMS Stations SensorConstantValues start");

        final List<LamAnturiVakioArvoVO> allLamAnturiVakioArvos = lotjuTmsStationMetadataService.getAllLamAnturiVakioArvos();

        boolean updated = tmsStationSensorConstantService.updateSensorConstantValues(allLamAnturiVakioArvos);

        int countFreeFlowSpeeds = tmsStationSensorConstantService.updateFreeFlowSpeedsOfTmsStations();
        log.info("Updated FreeFlowSpeeds for {} TmsStations", countFreeFlowSpeeds);
        if (countFreeFlowSpeeds > 0) {
            dataStatusService.updateDataUpdated(DataType.TMS_FREE_FLOW_SPEEDS_DATA);
        }

        if (updated) {
            dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA);
        }
        dataStatusService.updateDataUpdated(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA_CHECK);

        log.info("Update TMS Stations SensorConstantValues end");
        return updated;
    }
}
