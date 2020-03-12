package fi.livi.digitraffic.tie.service.v1.camera;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.helper.ToStringHelper;

@ConditionalOnNotWebApplication
@Service
public class CameraDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraDataUpdateService.class);

    private final int imageUpdateTimeout;
    private final CameraImageUpdateService cameraImageUpdateService;

    private static final ExecutorService jobThreadPool = Executors.newFixedThreadPool(5);
    private static final ExecutorService updateTaskThreadPool = Executors.newFixedThreadPool(5);

    @Autowired
    CameraDataUpdateService(@Value("${camera-image-uploader.imageUpdateTimeout}")
                                   final int imageUpdateTimeout,
                                   final CameraImageUpdateService cameraImageUpdateService) {
        this.imageUpdateTimeout = imageUpdateTimeout;
        this.cameraImageUpdateService = cameraImageUpdateService;
    }

    // Log only on warn and error level. Warn when over 10 s execution time as normally its around 6 s.
    @PerformanceMonitor(maxInfoExcecutionTime = 100000, maxWarnExcecutionTime = 10000)
    public int updateCameraData(final List<KuvaProtos.Kuva> data) {
        final Collection<KuvaProtos.Kuva> latestKuvas = filterLatest(data);
        final List<Future<Boolean>> futures = new ArrayList<>();
        final StopWatch start = StopWatch.createStarted();

        latestKuvas.forEach(kuva -> {
            final UpdateJobManager task = new UpdateJobManager(kuva, cameraImageUpdateService, imageUpdateTimeout);
            futures.add(jobThreadPool.submit(task));
        });

        while ( futures.stream().anyMatch(f -> !f.isDone()) ) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                log.debug("InterruptedException", e);
            }
        }
        final long updateCount = futures.parallelStream().filter(p -> {
                try {
                    return p.get();
                } catch (Exception e) {
                    log.error("method=updateCameraData UpdateJobManager task failed with error" , e);
                    return false;
                }
            }).count();

        log.info("Updating success for weather camera images updateCount={} of futuresCount={} failedCount={} tookMs={}", updateCount, futures.size(), futures.size()-updateCount, start.getTime());
        return (int) updateCount;
    }

    private Collection<KuvaProtos.Kuva> filterLatest(final List<KuvaProtos.Kuva> data) {
        // Collect newest kuva per preset
        final HashMap<Long, KuvaProtos.Kuva> kuvaMappedByPresetLotjuId = new HashMap<>();
        data.forEach(kuva -> {
            if (kuva.hasEsiasentoId()) {
                KuvaProtos.Kuva currentKamera = kuvaMappedByPresetLotjuId.get(kuva.getEsiasentoId());

                if ( currentKamera == null || currentKamera.getAikaleima() < kuva.getAikaleima()) {
                    if (currentKamera != null) {
                        log.info("Replace {} with {}", currentKamera.getAikaleima(), kuva.getAikaleima());
                    }
                    kuvaMappedByPresetLotjuId.put(kuva.getEsiasentoId(), kuva);
                }
            } else {
                log.warn("Kuva esiasentoId is not set: {}", ToStringHelper.toString(kuva));
            }
        });
        return kuvaMappedByPresetLotjuId.values();
    }


    private class UpdateJobManager implements Callable<Boolean> {

        private final long timeout;
        private final ImageUpdateTask task;

        private UpdateJobManager(final KuvaProtos.Kuva kuva, final CameraImageUpdateService cameraImageUpdateService, final long timeout) {
            this.timeout = timeout;
            this.task = new ImageUpdateTask(kuva, cameraImageUpdateService);
        }

        @Override
        public Boolean call() {
            Future<Boolean> future = null;
            String presetId = null;
            try {
                presetId = CameraImageUpdateService.resolvePresetIdFrom(null, task.kuva);
                future = updateTaskThreadPool.submit(task);
                return future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                log.error("ImageUpdateTasks failed to complete for presetId={} before timeoutMs={} ms", presetId, timeout);
            } catch (Exception e) {
                log.error(String.format("ImageUpdateTasks failed to complete for presetId=%s with exception", presetId), e);
            } finally {
                // This is safe even if task is already finished
                if (future != null) {
                    future.cancel(true);
                }
            }
            return false;
        }
    }

    private static class ImageUpdateTask implements Callable<Boolean> {

        private final KuvaProtos.Kuva kuva;
        private final CameraImageUpdateService cameraImageUpdateService;

        private ImageUpdateTask(final KuvaProtos.Kuva kuva, CameraImageUpdateService cameraImageUpdateService) {
            this.kuva = kuva;
            this.cameraImageUpdateService = cameraImageUpdateService;
        }

        @Override
        public Boolean call() {
            try {
                return cameraImageUpdateService.handleKuva(kuva);
            } catch (Exception e) {
                log.error(String.format("Error while calling cameraImageUpdateService.handleKuva with %s", ToStringHelper.toString(kuva)), e);
                throw e;
            }
        }
    }
}
