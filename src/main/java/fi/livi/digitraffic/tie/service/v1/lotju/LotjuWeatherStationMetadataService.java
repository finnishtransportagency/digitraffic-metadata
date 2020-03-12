package fi.livi.digitraffic.tie.service.v1.lotju;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaLaskennallinenAnturiVO;

@ConditionalOnNotWebApplication
@Service
public class LotjuWeatherStationMetadataService {

    private static final Logger log = LoggerFactory.getLogger(LotjuWeatherStationMetadataService.class);
    private final LotjuWeatherStationMetadataClient lotjuWeatherStationClient;

    @Autowired
    public LotjuWeatherStationMetadataService(final LotjuWeatherStationMetadataClient lotjuWeatherStationClient) {
        this.lotjuWeatherStationClient = lotjuWeatherStationClient;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 20000)
    public List<TiesaaAsemaVO> getTiesaaAsemas() {
        return lotjuWeatherStationClient.getTiesaaAsemas();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 100000)
    public Map<Long, List<TiesaaLaskennallinenAnturiVO>> getTiesaaLaskennallinenAnturisMappedByAsemaLotjuId(final Set<Long> tiesaaAsemaLotjuIds) {
        log.info("Fetching TiesaaLaskennallinenAnturis for roadWeatherStationCount={} TiesaaAsemas", tiesaaAsemaLotjuIds.size());

        final ConcurrentMap<Long, List<TiesaaLaskennallinenAnturiVO>> tiesaaAnturisMappedByRwsLotjuId = new ConcurrentHashMap<>();

        final StopWatch start = StopWatch.createStarted();
        final ExecutorService executor = Executors.newFixedThreadPool(1 );
        final CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        tiesaaAsemaLotjuIds.forEach(id -> completionService.submit(new LaskennallisetAnturitFetcher(id, tiesaaAnturisMappedByRwsLotjuId)));

        final AtomicInteger countAnturis = new AtomicInteger();
        // Tämä laskenta on välttämätön, jotta executor suorittaa loppuun jokaisen submitatun taskin.
        tiesaaAsemaLotjuIds.forEach(c -> {
            try {
                final Future<Integer> f = completionService.take();
                countAnturis.addAndGet(f.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error while fetching Anturits", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();

        log.info("Fetched sensorsCount={} Anturits, tookMs={}", countAnturis, start.getTime());
        return tiesaaAnturisMappedByRwsLotjuId;
    }

    public List<TiesaaLaskennallinenAnturiVO> getAllTiesaaLaskennallinenAnturis() {
        return lotjuWeatherStationClient.getAllTiesaaLaskennallinenAnturis();
    }

    private class LaskennallisetAnturitFetcher implements Callable<Integer> {

        private final Long tiesaaAsemaLotjuId;
        private final Map<Long, List<TiesaaLaskennallinenAnturiVO>> tiesaaAnturisMappedByRwsLotjuId;

        public LaskennallisetAnturitFetcher(final Long tiesaaAsemaLotjuId, final Map<Long, List<TiesaaLaskennallinenAnturiVO>> tiesaaAnturisMappedByRwsLotjuId) {
            this.tiesaaAsemaLotjuId = tiesaaAsemaLotjuId;
            this.tiesaaAnturisMappedByRwsLotjuId = tiesaaAnturisMappedByRwsLotjuId;
        }

        @Override
        public Integer call() throws Exception {
            final List<TiesaaLaskennallinenAnturiVO> anturis = lotjuWeatherStationClient.getTiesaaAsemanLaskennallisetAnturit(tiesaaAsemaLotjuId);
            tiesaaAnturisMappedByRwsLotjuId.put(tiesaaAsemaLotjuId, anturis);
            return anturis.size();
        }
    }

}
