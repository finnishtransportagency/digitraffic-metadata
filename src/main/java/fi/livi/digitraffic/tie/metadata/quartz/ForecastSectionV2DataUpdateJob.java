package fi.livi.digitraffic.tie.metadata.quartz;

import java.time.Instant;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionApiVersion;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionDataUpdater;

public class ForecastSectionV2DataUpdateJob extends SimpleUpdateJob {

    @Autowired
    private ForecastSectionDataUpdater forecastSectionDataUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) {
        final Instant messageTimestamp = forecastSectionDataUpdater.updateForecastSectionWeatherData(ForecastSectionApiVersion.V2);

        dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_V2_WEATHER_DATA, DateHelper.toZonedDateTimeAtUtc(messageTimestamp));
    }
}
