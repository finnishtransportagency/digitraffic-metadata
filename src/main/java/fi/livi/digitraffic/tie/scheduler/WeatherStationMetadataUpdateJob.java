package fi.livi.digitraffic.tie.scheduler;

import org.apache.commons.lang3.time.StopWatch;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationSensorUpdater;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationUpdater;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationsSensorsUpdater;

@DisallowConcurrentExecution
public class WeatherStationMetadataUpdateJob extends SimpleUpdateJob {

    @Autowired
    public WeatherStationSensorUpdater weatherStationSensorUpdater;

    @Autowired
    public WeatherStationUpdater weatherStationUpdater;

    @Autowired
    public WeatherStationsSensorsUpdater weatherStationsSensorsUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        final StopWatch sensorsWatch = StopWatch.createStarted();
        final boolean sensorsUpdated = weatherStationSensorUpdater.updateRoadStationSensors();
        if (sensorsUpdated) {
            dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_SENSOR_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_SENSOR_METADATA_CHECK);
        sensorsWatch.stop();

        final StopWatch stationsWatch = StopWatch.createStarted();
        boolean stationsUpdated = weatherStationUpdater.updateWeatherStations();
        stationsWatch.stop();

        final StopWatch stationsSensors = StopWatch.createStarted();
        stationsUpdated = weatherStationsSensorsUpdater.updateWeatherStationsSensors() || stationsUpdated;
        stationsSensors.stop();

        if (stationsUpdated) {
            dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_METADATA_CHECK);

        log.info("UpdateRoadStationSensors took: sensorsTimeMs={} ms, updateWeatherStations took: stationsTimeMs={} ms, updateWeatherStationsSensors took: stationsSensorsTimeMs={} ms",
                sensorsWatch.getTime(), stationsWatch.getTime(), stationsSensors.getTime());
    }
}
