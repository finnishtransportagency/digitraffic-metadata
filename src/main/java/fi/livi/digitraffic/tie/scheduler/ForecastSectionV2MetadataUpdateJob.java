package fi.livi.digitraffic.tie.scheduler;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.service.v2.forecastsection.V2ForecastSectionMetadataUpdater;

public class ForecastSectionV2MetadataUpdateJob extends SimpleUpdateJob {

    @Autowired
    private V2ForecastSectionMetadataUpdater v2ForecastSectionMetadataUpdater;

    @Override
    protected void doExecute(JobExecutionContext context) {
        v2ForecastSectionMetadataUpdater.updateForecastSectionsV2Metadata();
    }
}
