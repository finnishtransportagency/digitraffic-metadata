package fi.livi.digitraffic.tie.metadata.quartz;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionDataUpdater;

@DisallowConcurrentExecution
public class ForecastSectionWeatherUpdateJob extends SimpleUpdateJob {

    @Autowired
    private ForecastSectionDataUpdater forecastSectionDataUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) throws Exception {
        Timestamp messageTimestamp = forecastSectionDataUpdater.updateForecastSectionWeatherData();

        dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_WEATHER_DATA,
                                            ZonedDateTime.ofInstant(messageTimestamp.toInstant(), ZoneId.systemDefault()));
    }
}
