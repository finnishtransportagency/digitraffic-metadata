package fi.livi.digitraffic.tie.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionV1MetadataUpdater;

@DisallowConcurrentExecution
public class ForecastSectionV1MetadataUpdateJob extends SimpleUpdateJob {

    @Autowired
    private ForecastSectionV1MetadataUpdater forecastSectionMetadataUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) {
        forecastSectionMetadataUpdater.updateForecastSectionV1Metadata();
    }
}