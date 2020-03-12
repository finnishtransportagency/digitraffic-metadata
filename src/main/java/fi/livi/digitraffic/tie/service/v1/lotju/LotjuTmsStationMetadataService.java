package fi.livi.digitraffic.tie.service.v1.lotju;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.helper.ToStringHelper;

@ConditionalOnNotWebApplication
@Service
public class LotjuTmsStationMetadataService {

    private static final Logger log = LoggerFactory.getLogger(LotjuTmsStationMetadataService.class);
    private final LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient;

    @Autowired
    public LotjuTmsStationMetadataService(final LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient) {
        this.lotjuTmsStationMetadataClient = lotjuTmsStationMetadataClient;
    }

    public List<LamAsemaVO> getLamAsemas() {
        return lotjuTmsStationMetadataClient.getLamAsemas();
    }

    public List<LamLaskennallinenAnturiVO> getAllLamLaskennallinenAnturis() {
        return lotjuTmsStationMetadataClient.getAllLamLaskennallinenAnturis();
    }


    @PerformanceMonitor(maxWarnExcecutionTime = 800000, maxErroExcecutionTime = 1000000)
    public Map<Long, List<LamLaskennallinenAnturiVO>> getLamLaskennallinenAnturisMappedByAsemaLotjuId(final Set<Long> tmsLotjuIds) {
        final ConcurrentMap<Long, List<LamLaskennallinenAnturiVO>> lamAnturisMappedByTmsLotjuId = new ConcurrentHashMap<>();

        final ExecutorService executor = Executors.newFixedThreadPool(1);
        final CompletionService<Pair<Long, List<LamLaskennallinenAnturiVO>>> completionService = new ExecutorCompletionService<>(executor);

        final StopWatch start = StopWatch.createStarted();
        for (final Long tmsLotjuId : tmsLotjuIds) {
            completionService.submit(() -> Pair.of(tmsLotjuId, lotjuTmsStationMetadataClient.getTiesaaLaskennallinenAnturis(tmsLotjuId)));
        }

        int countAnturis = 0;
        // It's necessary to wait all executors to complete.
        int handledCount = 0;
        while (handledCount < tmsLotjuIds.size()) {
            handledCount++;
            try {
                final Future<Pair<Long, List<LamLaskennallinenAnturiVO>>> f = completionService.take();
                final Long tmsLotjuId = f.get().getKey();
                final List<LamLaskennallinenAnturiVO> anturis = f.get().getValue();
                lamAnturisMappedByTmsLotjuId.put(tmsLotjuId, anturis);
                countAnturis += anturis.size();
                log.debug("Got {} anturis", anturis.size());
            } catch (final InterruptedException | ExecutionException e) {
                log.error("Error while fetching LamLaskennallinenAnturis", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();

        log.info("lamFetchedCount={} LamLaskennallinenAnturis for lamStationCount={} LAMAsemas, tookMs={}",
                 countAnturis, lamAnturisMappedByTmsLotjuId.size(), start.getTime());
        return lamAnturisMappedByTmsLotjuId;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 150000, maxErroExcecutionTime = 200000)
    public List<LamAnturiVakioVO> getAllLamAnturiVakios(final Collection<Long> tmsLotjuIds) {

        final List<LamAnturiVakioVO> allAnturiVakios = new ArrayList<>();

        final ExecutorService executor = Executors.newFixedThreadPool(5);
        final CompletionService<List<LamAnturiVakioVO>> completionService = new ExecutorCompletionService<>(executor);

        final StopWatch start = StopWatch.createStarted();
        for (final Long tmsLotjuId : tmsLotjuIds) {
            completionService.submit(() -> lotjuTmsStationMetadataClient.getAsemanAnturiVakios(tmsLotjuId));
        }

        // It's necessary to wait all executors to complete.
        int handledCount = 0;
        while (handledCount < tmsLotjuIds.size()) {
            handledCount++;
            try {
                final List<LamAnturiVakioVO> values = completionService.take().get();
                allAnturiVakios.addAll(values);
                log.debug("Got {} AnturiVakios", values.size());
            } catch (final InterruptedException | ExecutionException e) {
                log.error("Error while fetching LamAnturiVakios", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();

        log.info("method=getAllLamAnturiVakios fetchedCount={} for lamStationCount={} tookMs={}",
                 allAnturiVakios.size(), tmsLotjuIds.size(), start.getTime());
        return allAnturiVakios;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 120000, maxErroExcecutionTime = 200000)
    public List<LamAnturiVakioArvoVO> getAllLamAnturiVakioArvos() {

        final List<LamAnturiVakioArvoVO> lamAnturiVakioArvos = new ArrayList<>();

        final ExecutorService executor = Executors.newFixedThreadPool(5);
        final CompletionService<List<LamAnturiVakioArvoVO>> completionService = new ExecutorCompletionService<>(executor);


        final StopWatch start = StopWatch.createStarted();
        int monthCounter = 0;
        while (monthCounter < 12) {
            monthCounter++;
            final int month = monthCounter;
            completionService.submit(() -> lotjuTmsStationMetadataClient.getAllAnturiVakioArvos(month, 1));
        }
        log.info("Fetch LamAnturiVakioArvos for {} months", monthCounter);

        int countLamAnturiVakioArvos = 0;
        // It's necessary to wait all executors to complete.
        for (int i = 0; i < monthCounter; i++) {
            try {
                final List<LamAnturiVakioArvoVO> values = completionService.take().get();
                countLamAnturiVakioArvos += values.size();
                lamAnturiVakioArvos.addAll(values);
                log.debug("Got {} LamAnturiVakioArvos, {}/{}", values.size(), i+1, monthCounter);
            } catch (final InterruptedException | ExecutionException e) {
                log.error("Error while fetching LamAnturiVakioArvos", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();

        List<LamAnturiVakioArvoVO> distincLamAnturiVakios = lamAnturiVakioArvos.parallelStream()
            .map(LamAnturiVakioArvoWrapper::new)
            .distinct()
            .map(LamAnturiVakioArvoWrapper::unWrap)
            .collect(Collectors.toList());

        log.debug("Distinct lamAnturiVakioArvos {} was before {}", distincLamAnturiVakios.size(), lamAnturiVakioArvos.size());
        log.info("method=getAllLamAnturiVakioArvos fetchedCount={} for monthCount={} distincLamAnturiVakiosCount={} tookMs={}",
                 countLamAnturiVakioArvos, monthCounter, distincLamAnturiVakios.size(), start.getTime());
        return distincLamAnturiVakios;
    }

    private class LamAnturiVakioArvoWrapper {

        private final LamAnturiVakioArvoVO vakio;

        public LamAnturiVakioArvoWrapper(LamAnturiVakioArvoVO vakio) {
            this.vakio = vakio;
        }

        public LamAnturiVakioArvoVO unWrap() {
            return vakio;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            LamAnturiVakioArvoVO other = ((LamAnturiVakioArvoWrapper)o).unWrap();

            boolean equals = new EqualsBuilder()
                .append(other.getAnturiVakioId(), vakio.getAnturiVakioId())
                .append(other.getVoimassaAlku(), vakio.getVoimassaAlku())
                .append(other.getVoimassaLoppu(), vakio.getVoimassaLoppu())
                .isEquals();
            if (equals && !new EqualsBuilder().append(other.getArvo(), vakio.getArvo()).isEquals()) {
                log.error("LOTJU returns unequal values for same AnturiVakioArvo {} vs {}", ToStringHelper.toStringFull(vakio), ToStringHelper.toStringFull(other));
            }
            return equals;
        }

        public int hashCode() {
            return new HashCodeBuilder()
                .append(vakio.getAnturiVakioId())
                .append(vakio.getArvo())
                .append(vakio.getVoimassaAlku())
                .append(unWrap().getVoimassaLoppu())
                .toHashCode();
        }
    }
}
