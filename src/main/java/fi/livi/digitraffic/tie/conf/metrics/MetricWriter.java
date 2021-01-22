package fi.livi.digitraffic.tie.conf.metrics;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

@Configuration
public class MetricWriter {
    private final MeterRegistry meterRegistry;

    private static final Logger LOG = LoggerFactory.getLogger(MetricWriter.class);

    private final List<String> meterNames = Arrays.asList(
        "hikaricp.connections.max",
        "hikaricp.connections.active");

    public MetricWriter(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedRate = 1000*60)
    void printMetrics() {
        meterNames.forEach(name -> {
            logMeasurement(name, Statistic.VALUE);
        });
    }

    private void logMeasurement(final String meterName, final Statistic statistic) {
        final Meter meter = meterRegistry.get(meterName).meter();

        if(meter == null) {
            LOG.error("Could not find meter {}", meterName);
            return;
        }
        
        final Measurement measurement = StreamSupport.stream(meter.measure().spliterator(), false)
            .filter(m -> m.getStatistic() == statistic)
            .findFirst().orElse(null);

        if(measurement == null) {
            LOG.error("Could not find statistic {} for {}", statistic, meterName);
            return;
        }

        LOG.info("meterName={}.{} statisticValue={}", meterName, statistic, measurement.getValue());
    }
}
