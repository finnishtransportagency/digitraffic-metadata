package fi.livi.digitraffic.tie.metadata.quartz;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.LongStream;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpServerErrorException;

import fi.livi.digitraffic.tie.data.service.traveltime.TravelTimeUpdater;
import fi.livi.digitraffic.tie.metadata.dao.MetadataUpdatedRepository;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.model.DataUpdated;

@DisallowConcurrentExecution
public class TravelTimeMeasurementsUpdateJob extends SimpleUpdateJob {

    @Autowired
    private TravelTimeUpdater travelTimeUpdater;

    @Autowired
    private MetadataUpdatedRepository metadataUpdatedRepository;

    @Override
    protected void doExecute(final JobExecutionContext context) throws Exception {

        final DataUpdated updated = metadataUpdatedRepository.findByMetadataType(DataType.TRAVEL_TIME_MEASUREMENTS.name());

        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime from = TravelTimeMediansUpdateJob.getStartTime(updated, now);

        final long between = ChronoUnit.MINUTES.between(from, now.minusMinutes(1));  // Measurement period duration is 5 minutes

        LongStream.range(1, between).forEachOrdered(minute -> {
            try {
                travelTimeUpdater.updateIndividualMeasurements(from.plusMinutes(minute));
            } catch (HttpServerErrorException e) {
                // Request failed after retries. Skip this minute.
                log.debug("HttpServerErrorException", e);
            }
        });
    }
}
