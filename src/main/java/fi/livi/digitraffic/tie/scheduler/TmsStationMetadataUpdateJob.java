package fi.livi.digitraffic.tie.scheduler;

import org.apache.commons.lang3.time.StopWatch;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationSensorUpdater;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationUpdater;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationsSensorsUpdater;

@DisallowConcurrentExecution
public class TmsStationMetadataUpdateJob extends SimpleUpdateJob {

    @Autowired
    private TmsStationSensorUpdater tmsStationSensorUpdater;

    @Autowired
    private TmsStationUpdater tmsStationUpdater;

    @Autowired
    private TmsStationsSensorsUpdater tmsStationsSensorsUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {

        final StopWatch sensorsWatch = StopWatch.createStarted();
        final boolean sensorsUpdated = tmsStationSensorUpdater.updateRoadStationSensors();
        if (sensorsUpdated) {
            dataStatusService.updateDataUpdated(DataType.TMS_STATION_SENSOR_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.TMS_STATION_SENSOR_METADATA_CHECK);
        sensorsWatch.stop();

        final StopWatch stationsWatch = StopWatch.createStarted();
        final boolean stationsUpdated = tmsStationUpdater.updateTmsStations();
        stationsWatch.stop();

        final StopWatch stationsSensorsWatch = StopWatch.createStarted();
        final boolean updatedTmsStationsSensors = tmsStationsSensorsUpdater.updateTmsStationsSensors();
        stationsSensorsWatch.stop();

        if (stationsUpdated || updatedTmsStationsSensors) {
            dataStatusService.updateDataUpdated(DataType.TMS_STATION_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.TMS_STATION_METADATA_CHECK);

        log.info("UpdateRoadStationSensors took: sensorsTimeMs={} ms, updateTmsStations took: stationsTimeMs={} ms, updateTmsStationsSensors took: stationsSensorsTimeMs={} ms",
                sensorsWatch.getTime(), stationsWatch.getTime(), stationsSensorsWatch.getTime());
    }
}
