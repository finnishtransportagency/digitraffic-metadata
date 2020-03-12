package fi.livi.digitraffic.tie.scheduler;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionApiVersion;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionDataUpdater;

public class ForecastSectionV2DataUpdateJob extends SimpleUpdateJob {

    @Autowired
    private ForecastSectionDataUpdater forecastSectionDataUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) {
        forecastSectionDataUpdater.updateForecastSectionWeatherData(ForecastSectionApiVersion.V2);
    }
}
