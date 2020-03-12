package fi.livi.digitraffic.tie.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.service.v1.datex2.Datex2SimpleMessageUpdater;

@DisallowConcurrentExecution
public class Datex2RoadworksMessageUpdateJob extends SimpleUpdateJob {

    @Autowired
    private Datex2SimpleMessageUpdater datex2SimpleMessageUpdater;

    @Override
    protected void doExecute(final JobExecutionContext context) throws Exception {
        datex2SimpleMessageUpdater.updateDatex2RoadworksMessages();
    }
}
