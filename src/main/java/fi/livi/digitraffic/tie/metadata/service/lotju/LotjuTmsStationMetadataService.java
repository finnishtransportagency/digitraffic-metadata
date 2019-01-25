package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

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

//    TODO @PerformanceMonitor(maxErroExcecutionTime = , maxWarnExcecutionTime = )
    public Map<Long, List<LamLaskennallinenAnturiVO>> getLamLaskennallinenAnturisMappedByAsemaLotjuId(final Set<Long> tmsLotjuIds) {
        final ConcurrentMap<Long, List<LamLaskennallinenAnturiVO>> lamAnturisMappedByTmsLotjuId = new ConcurrentHashMap<>();

        final ExecutorService executor = Executors.newFixedThreadPool(1);
        final CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        final StopWatch start = StopWatch.createStarted();
        for (final Long tmsLotjuId : tmsLotjuIds) {
            completionService.submit(new LaskennallinenanturiFetcher(tmsLotjuId, lamAnturisMappedByTmsLotjuId));
        }

        final AtomicInteger countAnturis = new AtomicInteger();
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
                countAnturis.get(), lamAnturisMappedByTmsLotjuId.size(), start.getTime());
        return lamAnturisMappedByTmsLotjuId;
    }

    public List<LamAnturiVakioVO> getAllLamAnturiVakios(final Set<Long> tmsLotjuIds) {

        final List<LamAnturiVakioVO> allAnturiVakios = new ArrayList<LamAnturiVakioVO>();
        final List<LamAnturiVakioVO> synchronizedAnturiVakios = Collections.synchronizedList(allAnturiVakios);

        final ExecutorService executor = Executors.newFixedThreadPool(5);
        final CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        final StopWatch start = StopWatch.createStarted();
        for (final Long tmsLotjuId : tmsLotjuIds) {
            completionService.submit(new AnturiVakioFetcher(tmsLotjuId, synchronizedAnturiVakios));
        }

        // It's necessary to wait all executors to complete.
        tmsLotjuIds.forEach(id -> {
            try {
                final Future<Integer> f = completionService.take();
                log.debug("Got {} AnturiVakios", f.get());
            } catch (final InterruptedException | ExecutionException e) {
                log.error("Error while fetching LamAnturiVakios", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();

        log.info("lamFetchedCount={} LamAnturiVakios for lamStationCount={} LAMAsemas, tookMs={}",
                 allAnturiVakios.size(), tmsLotjuIds.size(), start.getTime());
        return allAnturiVakios;
    }

    public List<LamAnturiVakioArvoVO> getAllLamAnturiVakioArvos() {

        final List<LamAnturiVakioArvoVO> lamAnturiVakios = Collections.synchronizedList(new ArrayList<>());

        final ExecutorService executor = Executors.newFixedThreadPool(5);
        final CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        LocalDate date = LocalDate.ofYearDay(LocalDate.now().getYear(),1);
        final int startYear = date.getYear();
        final StopWatch start = StopWatch.createStarted();
        int dayCounter = 0;
        while (startYear == date.getYear()) {
            completionService.submit(new AnturiVakioArvoFetcher(date.getMonthValue(), date.getDayOfMonth(), lamAnturiVakios));
            dayCounter++;
            date = date.plusDays(1);
        }
        log.info("Fetch LamAnturiVakioArvos for {} days", dayCounter);

        final AtomicInteger countLamAnturiVakioArvos = new AtomicInteger();
        // It's necessary to wait all executors to complete.
        for (int i = 0; i < dayCounter; i++) {
            try {
                final Future<Integer> f = completionService.take();
                countLamAnturiVakioArvos.addAndGet(f.get());
                log.debug("Got {} LamAnturiVakioArvos", f.get());
            } catch (final InterruptedException | ExecutionException e) {
                log.error("Error while fetching LamAnturiVakioArvos", e);
                executor.shutdownNow();
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();

        final StopWatch distinctTime = StopWatch.createStarted();
        List<LamAnturiVakioArvoVO> distincLamAnturiVakios = lamAnturiVakios.parallelStream()
            .map(LamAnturiVakioArvoWrapper::new)
            .distinct()
            .map(LamAnturiVakioArvoWrapper::unWrap)
            .collect(Collectors.toList());
        distinctTime.stop();

        log.info("LamAnturiVakios from {} to distinct {}", lamAnturiVakios.size(), distincLamAnturiVakios.size());
        log.info("lamFetchedCount={} LamAnturiVakios, for dateCount={} days, tookMs={}, distinctCount {}",
                 countLamAnturiVakioArvos.get(), dayCounter, start.getTime(), distincLamAnturiVakios.size());
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

    private class AnturiVakioFetcher implements Callable<Integer> {

        private final Long tmsLotjuId;
        private final List<LamAnturiVakioVO> synchronizedAnturiVakios;

        public AnturiVakioFetcher(final Long tmsLotjuId, final List<LamAnturiVakioVO> synchronizedAnturiVakios) {
            this.tmsLotjuId = tmsLotjuId;
            this.synchronizedAnturiVakios = synchronizedAnturiVakios;
        }

        @Override
        public Integer call() throws Exception {
            final List<LamAnturiVakioVO> anturis = lotjuTmsStationMetadataClient.getAsemanAnturiVakios(tmsLotjuId);
            for (LamAnturiVakioVO lamAnturiVakioVO : anturis) {
                log.debug("Anturi {}", ToStringHelper.toStringFull(lamAnturiVakioVO));
            }
            synchronizedAnturiVakios.addAll(anturis);
            return anturis.size();
        }
    }

    private class AnturiVakioArvoFetcher implements Callable<Integer> {

        private final int month;
        private final int dayOfMonth;
        private final List<LamAnturiVakioArvoVO> anturiVakioArvos;

        public AnturiVakioArvoFetcher(final int month, final int dayOfMonth, final List<LamAnturiVakioArvoVO> anturiVakioArvos) {
            this.month = month;
            this.dayOfMonth = dayOfMonth;
            this.anturiVakioArvos = anturiVakioArvos;
        }

        @Override
        public Integer call() throws Exception {
            final List<LamAnturiVakioArvoVO> arvos = lotjuTmsStationMetadataClient.getAllAnturiVakioArvos(month, dayOfMonth);
            anturiVakioArvos.addAll(arvos);
            return arvos.size();
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
                log.error("Lotju returns unequal values for same AnturiVakioArvo {} vs {}", ToStringHelper.toStringFull(vakio), ToStringHelper.toStringFull(other));
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
