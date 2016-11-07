package fi.livi.digitraffic.tie.metadata.quartz;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionUpdater;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@DisallowConcurrentExecution
public class ForecastSectionCoordinatesUpdateJob extends AbstractUpdateJob {

    private static final Logger log = LoggerFactory.getLogger(ForecastSectionCoordinatesUpdateJob.class);

    @Autowired
    private ForecastSectionUpdater forecastSectionUpdater;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Road section coordinates update job start");
        final long startTime = System.currentTimeMillis();

        boolean updated = forecastSectionUpdater.updateForecastSectionCoordinates();

        if (updated) {
            staticDataStatusService.updateMetadataUpdated(MetadataType.FORECAST_SECTION);
        }

        final long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;

        String updateStatus = updated ? "Data updates took place." : "No updates took place.";
        log.info("Road section coordinates update job ended. Update took " + duration + " seconds. " + updateStatus);
    }
}