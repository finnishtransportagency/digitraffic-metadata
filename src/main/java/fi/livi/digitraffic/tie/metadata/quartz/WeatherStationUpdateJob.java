package fi.livi.digitraffic.tie.metadata.quartz;

import org.apache.commons.lang3.time.StopWatch;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationSensorUpdater;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationUpdater;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationsSensorsUpdater;

@DisallowConcurrentExecution
public class WeatherStationUpdateJob extends SimpleUpdateJob {

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

        log.info("UpdateRoadStationSensors took: {} ms, updateWeatherStations took: {} ms, updateWeatherStationsSensors took: {} ms",
                sensorsWatch.getTime(), stationsWatch.getTime(), stationsSensors.getTime());
    }
}
