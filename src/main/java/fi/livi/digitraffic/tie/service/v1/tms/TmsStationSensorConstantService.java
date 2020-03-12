package fi.livi.digitraffic.tie.service.v1.tms;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.TmsSensorConstantDao;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;

@Service
public class TmsStationSensorConstantService {
    private static final Logger log = LoggerFactory.getLogger(TmsStationSensorConstantService.class);
    private final TmsSensorConstantDao tmsSensorConstantDao;
    private final DataStatusService dataStatusService;

    @Autowired
    public TmsStationSensorConstantService(final TmsSensorConstantDao tmsSensorConstantDao,
                                           final DataStatusService dataStatusService) {

        this.tmsSensorConstantDao = tmsSensorConstantDao;
        this.dataStatusService = dataStatusService;
    }

    @Transactional
    public boolean updateSensorConstants(List<LamAnturiVakioVO> allLamAnturiVakios) {
        List<Long> ids = allLamAnturiVakios.stream().map(LamAnturiVakioVO::getId).collect(Collectors.toList());
        final int obsoleted = tmsSensorConstantDao.obsoleteSensorConstants(ids);
        final int upsert = tmsSensorConstantDao.updateSensorConstants(allLamAnturiVakios);
        log.info("updateSensorConstants upsert={}, obsoleted={}", upsert, obsoleted);
        return obsoleted > 0 || upsert > 0;
    }

    @Transactional
    public boolean updateSensorConstantValues(List<LamAnturiVakioArvoVO> allLamAnturiVakioArvos) {
        final List<Long> ids = allLamAnturiVakioArvos.stream().map(LamAnturiVakioArvoVO::getId).collect(Collectors.toList());
        final int obsoleted = tmsSensorConstantDao.obsoleteSensorConstantValues(ids);
        final int upsert = tmsSensorConstantDao.updateSensorConstantValues(allLamAnturiVakioArvos);
        log.info("updateSensorConstantValues upsert={}, obsoleted={}", upsert, obsoleted);
        return obsoleted > 0 || upsert > 0;
    }

    @Transactional
    public int updateFreeFlowSpeedsOfTmsStations() {
        return tmsSensorConstantDao.updateFreeFlowSpeedsOfTmsStations();
    }

    @Transactional(readOnly = true)
    public ZonedDateTime getLatestMeasurementTime() {
        final ZonedDateTime dataUpdated = dataStatusService.findDataUpdatedTime(DataType.TMS_SENSOR_CONSTANT_VALUE_DATA);
        final ZonedDateTime metadataUpdated = dataStatusService.findDataUpdatedTime(DataType.TMS_SENSOR_CONSTANT_METADATA);
        return DateHelper.getNewest(dataUpdated, metadataUpdated);
    }
}
