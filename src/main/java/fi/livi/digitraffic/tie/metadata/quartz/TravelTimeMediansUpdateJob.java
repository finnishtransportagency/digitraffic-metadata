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
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;

@DisallowConcurrentExecution
public class TravelTimeMediansUpdateJob extends SimpleUpdateJob {

    @Autowired
    private TravelTimeUpdater travelTimeUpdater;

    @Autowired
    private MetadataUpdatedRepository metadataUpdatedRepository;

    @Override
    protected void doExecute(final JobExecutionContext context) throws Exception {

        final MetadataUpdated updated = metadataUpdatedRepository.findByMetadataType(MetadataType.TRAVEL_TIME_MEDIANS.name());

        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime from = getStartTime(updated, now);

        final long between = ChronoUnit.MINUTES.between(from, now.minusMinutes(5)); // Median period duration is 5 minutes

        LongStream.range(1, between).forEachOrdered(minute -> {
            try {
                travelTimeUpdater.updateMedians(from.plusMinutes(minute));
            } catch (HttpServerErrorException e) {
                // Request failed after retries. Skip this minute.
            }
        });
    }

    public static ZonedDateTime getStartTime(final MetadataUpdated updated, final ZonedDateTime now) {

        if (updated == null || updated.getUpdatedTime().isBefore(now.minusDays(1))) {
            // Data source stores data from last 24h hours
            return now.minusDays(1);
        } else {
            return updated.getUpdatedTime();
        }
    }
}
