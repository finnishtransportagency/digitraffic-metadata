package fi.livi.digitraffic.tie.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.service.v1.location.LocationMetadataUpdater;

@DisallowConcurrentExecution
public class LocationMetadataUpdateJob extends SimpleUpdateJob {
    @Autowired
    private LocationMetadataUpdater locationMetadataUpdater;

    @Override
    @Transactional
    public void doExecute(final JobExecutionContext context) throws Exception {
        locationMetadataUpdater.findAndUpdate();
    }
}
