package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.ArrayList;
import java.util.Collection;
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
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVakioArvoVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVakioVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2017._05._02.LamLaskennallinenAnturiVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12.LamAsemaVO;

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
        final CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        final StopWatch start = StopWatch.createStarted();
        for (final Long tmsLotjuId : tmsLotjuIds) {
            completionService.submit(new LaskennallinenanturiFetcher(tmsLotjuId, lamAnturisMappedByTmsLotjuId));
        }

        final MutableInt countAnturis = new MutableInt();
        // Tämä laskenta on välttämätön, jotta executor suorittaa loppuun jokaisen submitatun taskin.
        tmsLotjuIds.forEach(id -> {
            try {
                final Future<Integer> f = completionService.take();
                countAnturis.addAndGet(f.get());
                log.debug("Got {} anturis", f.get());
            } catch (final InterruptedException | ExecutionException e) {
                log.error("Error while fetching LamLaskennallinenAnturis", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();

        log.info("lamFetchedCount={} LamLaskennallinenAnturis for lamStationCount={} LAMAsemas, tookMs={}",
                 countAnturis.getValue(), lamAnturisMappedByTmsLotjuId.size(), start.getTime());
        return lamAnturisMappedByTmsLotjuId;
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 150000, maxErroExcecutionTime = 200000)
    public List<LamAnturiVakioVO> getAllLamAnturiVakios(final Collection<Long> tmsLotjuIds) {

        final List<LamAnturiVakioVO> allAnturiVakios = new ArrayList<>();

        final ExecutorService executor = Executors.newFixedThreadPool(5);
        final CompletionService<List<LamAnturiVakioVO>> completionService = new ExecutorCompletionService<>(executor);

        final StopWatch start = StopWatch.createStarted();
        for (final Long tmsLotjuId : tmsLotjuIds) {
            completionService.submit(new AnturiVakioFetcher(tmsLotjuId));
        }

        // It's necessary to wait all executors to complete.
        tmsLotjuIds.forEach(id -> {
            try {
                final List<LamAnturiVakioVO> values = completionService.take().get();
                allAnturiVakios.addAll(values);
                log.debug("Got {} AnturiVakios", values.size());
            } catch (final InterruptedException | ExecutionException e) {
                log.error("Error while fetching LamAnturiVakios", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        });
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
            completionService.submit(new AnturiVakioArvoFetcher(monthCounter, 1));
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

    private class LaskennallinenanturiFetcher implements Callable<Integer> {

        private final Long tmsLotjuId;
        private final ConcurrentMap<Long, List<LamLaskennallinenAnturiVO>> currentLamAnturisMappedByTmsLotjuId;

        public LaskennallinenanturiFetcher(final Long tmsLotjuId, final ConcurrentMap<Long, List<LamLaskennallinenAnturiVO>> currentLamAnturisMappedByTmsLotjuId) {
            this.tmsLotjuId = tmsLotjuId;
            this.currentLamAnturisMappedByTmsLotjuId = currentLamAnturisMappedByTmsLotjuId;
        }

        @Override
        public Integer call() throws Exception {
            final List<LamLaskennallinenAnturiVO> anturis = lotjuTmsStationMetadataClient.getTiesaaLaskennallinenAnturis(tmsLotjuId);
            currentLamAnturisMappedByTmsLotjuId.put(tmsLotjuId, anturis);
            return anturis.size();
        }
    }

    private class AnturiVakioFetcher implements Callable<List<LamAnturiVakioVO>> {

        private final Long tmsLotjuId;

        public AnturiVakioFetcher(final Long tmsLotjuId) {
            this.tmsLotjuId = tmsLotjuId;
        }

        @Override
        public List<LamAnturiVakioVO> call() throws Exception {
            return lotjuTmsStationMetadataClient.getAsemanAnturiVakios(tmsLotjuId);
        }
    }

    private class AnturiVakioArvoFetcher implements Callable<List<LamAnturiVakioArvoVO>> {

        private final int month;
        private final int dayOfMonth;

        public AnturiVakioArvoFetcher(final int month, final int dayOfMonth) {
            this.month = month;
            this.dayOfMonth = dayOfMonth;
        }

        @Override
        public List<LamAnturiVakioArvoVO> call() throws Exception {
            return lotjuTmsStationMetadataClient.getAllAnturiVakioArvos(month, dayOfMonth);
        }
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
