package fi.livi.digitraffic.tie.conf.metrics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;

@Configuration
public class MetricWriterConfig {
    private final MeterRegistry meterRegistry;

    private final List<String> meterNames = Arrays.asList(
        "hikaricp.connections.max",
        "hikaricp.connections.active",
        "hikaricp.connections.usage",
        "hikaricp.connections.acquire");

    public MetricWriterConfig(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedRate = 1000*60)
    void printMetrics() {
        meterNames.stream().forEach(name -> {
            final Meter meter = meterRegistry.get(name).meter();

            System.out.println(name);

            meter.measure().forEach(System.out::println);
        });
    }
}
