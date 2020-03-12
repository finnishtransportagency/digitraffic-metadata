package fi.livi.digitraffic.tie.scheduler;

import org.apache.commons.lang3.time.StopWatch;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SimpleUpdateJob extends AbstractUpdateJob {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public final void execute(final JobExecutionContext context) {
        final String jobName = context.getJobDetail().getKey().getName();

        log.info("Quartz jobName={} start", jobName);

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Exception lastError = null;
        try {
            doExecute(context);
        } catch(final Exception e) {
            lastError = e;
            log.error("Exception executing jobName=" + jobName, e);
        }

        stopWatch.stop();

        log.info("Quartz jobName={} end jobEndStatus={} jobTimeMs={} last error: {} {}",
                 jobName, lastError == null ? "SUCCESS" : "FAIL", stopWatch.getTime(),
                 lastError != null ? lastError.getClass() : null,  lastError != null ? lastError.getMessage() : "");
    }

    protected abstract void doExecute(final JobExecutionContext context) throws Exception;
}
