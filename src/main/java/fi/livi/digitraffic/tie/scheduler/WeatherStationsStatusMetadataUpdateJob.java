package fi.livi.digitraffic.tie.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationUpdater;

@DisallowConcurrentExecution
public class WeatherStationsStatusMetadataUpdateJob extends SimpleUpdateJob {

    @Autowired
    private WeatherStationUpdater weatherStationUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) {
        final int wsCount = weatherStationUpdater.updateWeatherStationsStatuses();
        if (wsCount > 0) {
            dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.WEATHER_STATION_METADATA_CHECK);
        log.info("weatherStationsStatusUpdatedCount={}", wsCount);
    }
}
