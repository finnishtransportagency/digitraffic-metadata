package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetHistoryService;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

@ConditionalOnNotWebApplication
@Service
public class CameraImageUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraImageUpdateService.class);

    private final int retryDelayMs;
    private final CameraPresetService cameraPresetService;
    private final CameraImageReader imageReader;
    private final CameraImageWriter imageWriter;
    private final CameraImageS3Writer cameraImageS3Writer;
    private final byte[] noiseImage;
    private final ResourceLoader resourceLoader;
    private final CameraPresetHistoryService cameraPresetHistoryService;

    static final int RETRY_COUNT = 3;

    private final static Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>() {{
        put(CameraImageReadFailureException.class, true);
        put(CameraImageWriteFailureException.class, true);
    }};
    private final static String NOISE_IMG = "img/noise.jpg";

    @Autowired
    CameraImageUpdateService(
        @Value("${camera-image-uploader.retry.delay.ms}")
        final int retryDelayMs,
        final CameraPresetService cameraPresetService,
        final CameraImageReader imageReader,
        final CameraImageWriter imageWriter,
        final CameraImageS3Writer cameraImageS3Writer,
        final ResourceLoader resourceLoader,
        final CameraPresetHistoryService cameraPresetHistoryService) throws IOException {
        this.retryDelayMs = retryDelayMs;
        this.cameraPresetService = cameraPresetService;
        this.imageReader = imageReader;
        this.imageWriter = imageWriter;
        this.cameraImageS3Writer = cameraImageS3Writer;
        this.resourceLoader = resourceLoader;
        this.cameraPresetHistoryService = cameraPresetHistoryService;
        this.noiseImage = readImageFromResource(NOISE_IMG);
    }

    private byte[] readImageFromResource(final String imagePath) throws IOException {
        log.info("Read image from {}", imagePath);
        final Resource resource = resourceLoader.getResource("classpath:" + imagePath);
        final InputStream imageIs = resource.getInputStream();
        return IOUtils.toByteArray(imageIs);
    }

    // TODO DPO-462 remove when done and S3 in use
    @Transactional
    public long deleteAllImagesForNonPublishablePresets() {
        // return count of succesful deletes
        final List<String> npIds = cameraPresetService.findAllNotPublishableCameraPresetsPresetIds();

        return npIds.stream().map(presetId -> imageWriter.deleteImage(getPresetImageName(presetId)))
            .filter(CameraImageWriter.DeleteInfo::isFileExistsAndDeleteSuccess)
            .count();
    }

    public boolean handleKuva(final KuvaProtos.Kuva kuva) {
        if (log.isDebugEnabled()) {
            log.debug("method=handleKuva Handling {}", ToStringHelper.toString(kuva));
        }

        final CameraPreset cameraPreset = cameraPresetService.findCameraPresetByLotjuId(kuva.getEsiasentoId());

        final String presetId = resolvePresetIdFrom(cameraPreset, kuva);
        final String filename = getPresetImageName(presetId);

        // If preset exists in db, update image
        if (cameraPreset != null) {
            final boolean roadStationPublic = cameraPreset.getRoadStation().isPublicNow();
            final boolean isResultPublic = kuva.getJulkinen() && roadStationPublic;
            final ImageUpdateInfo transferInfo = transferKuva(kuva, presetId, filename, isResultPublic);

            cameraPresetService.updateCameraPresetAndHistory(cameraPreset, isResultPublic, transferInfo);

            if (transferInfo.isSuccess()) {
                log.info("method=handleKuva presetId={} uploadFileName={} readImageStatus={} writeImageStatus={} " +
                        "readTookMs={} writeTooksMs={} s3WriteTooksMs={} tookMs={} " +
                        "downloadImageUrl={} imageSizeBytes={} " +
                        "s3VersionId={} imageTimestamp={} imageTimeInPastSeconds={}",
                    presetId, transferInfo.getFullPath(), transferInfo.getReadStatus(), transferInfo.getWriteStatus(),
                    transferInfo.getReadDurationMs(), transferInfo.getWriteDurationMs(), transferInfo.getS3WriteDurationMs(),
                    transferInfo.getDurationMs(), transferInfo.getDownloadUrl(), transferInfo.getSizeBytes(), transferInfo.getS3VersionId(),
                    transferInfo.getLastUpdated(), transferInfo.getImageTimeInPastSeconds());
            } else {
                log.error("method=handleKuva presetId={} uploadFileName={} readImageStatus={} writeImageStatus={} " +
                        "readTookMs={} readTotalTookMs={} " +
                        "writeTooksMs={} writeTotalTookMs={} tookMs={} " +
                        "downloadImageUrl={} imageSizeBytes={} " +
                        "readErro={} writeError={}",
                    presetId, transferInfo.getFullPath(), transferInfo.getReadStatus(), transferInfo.getWriteStatus(),
                    transferInfo.getReadDurationMs(), transferInfo.getReadTotalDurationMs(),
                    transferInfo.getWriteDurationMs(), transferInfo.getWriteTotalDurationMs(),
                    transferInfo.getDurationMs(),
                    transferInfo.getDownloadUrl(), transferInfo.getSizeBytes(),
                    transferInfo.getReadError(), transferInfo.getWriteError());
            }
            return transferInfo.isSuccess();
        } else {
            // Preset doesn't exist, so we delete image
            final CameraImageWriter.DeleteInfo deleteInfo = imageWriter.deleteImage(filename);
            log.info("method=handleKuva preset does not exists presetId={} deleteFileName={} fileExists={} deleteSuccess={} tookMs={}",
                presetId, deleteInfo.getKey(), deleteInfo.isFileExists(), deleteInfo.isDeleteSuccess(), deleteInfo.getDurationMs());
            final CameraImageS3Writer.DeleteInfo deleteInfoS3 = cameraImageS3Writer.deleteImage(filename);
            log.info("method=handleKuva preset does not exists presetId={} deleteFileS3Key={} fileExists={} deleteSuccess={} tookMs={}",
                presetId, deleteInfo.getKey(), deleteInfoS3.isFileExists(), deleteInfoS3.isDeleteSuccess(), deleteInfoS3.getDurationMs());
            return deleteInfoS3.isSuccess();
        }
    }

    private ImageUpdateInfo transferKuva(final KuvaProtos.Kuva kuva, final String presetId, final String filename, final boolean isPublic) {
        final ImageUpdateInfo info = new ImageUpdateInfo(presetId, imageWriter.getImageFullPath(filename),
                                                         DateHelper.toZonedDateTimeAtUtc(kuva.getAikaleima()));
        try {
            byte[] image = readKuva(kuva.getKuvaId(), info);
            try {
                image = ImageManipulationService.removeJpgExifMetadata(image);
            } catch (Exception e) {
                // Let's use original
                log.warn("Failed to remove Exif metadata from image, using original image", e);
            }
            writeKuva(image, kuva.getAikaleima(), filename, info, isPublic);
        } catch (final CameraImageReadFailureException e) {
            // read attempts exhausted
        } catch (final CameraImageWriteFailureException e) {
            // write attempts exhausted
        }
        return info;
    }

    private byte[] readKuva(final long kuvaId, final ImageUpdateInfo info) {
        final RetryTemplate retryTemplate = getRetryTemplate();
        return retryTemplate.execute(retryContext -> {
            final StopWatch start = StopWatch.createStarted();
            // Read the image
            byte[] image;
            try {
                image = imageReader.readImage(kuvaId, info);
                info.setSizeBytes(image.length);
                info.updateReadStatusSuccess();
                final long readEnd = start.getTime();
                info.updateReadTotalDurationMs(readEnd);
                info.setReadDurationMs(readEnd);
            } catch (final Exception e) {
                info.updateReadStatusFailed(e);
                throw new CameraImageReadFailureException(e);
            }
            if (image.length <= 0) {
                final CameraImageReadFailureException e = new CameraImageReadFailureException("Image was 0 bytes");
                info.updateReadStatusFailed(e);
                throw e;
            }
            return image;
        });
    }

    private void writeKuva(final byte[] realImage, final long timestampEpochMillis, final String filename, final ImageUpdateInfo info, final boolean isPublic) {
        final RetryTemplate retryTemplate = getRetryTemplate();
        retryTemplate.execute(retryContext -> {
            final StopWatch writeStart = StopWatch.createStarted();
            try {
                final byte[] currentImageToWrite = isPublic ? realImage : noiseImage;

                imageWriter.writeImage(currentImageToWrite, filename, timestampEpochMillis);
                final long writeDuration = writeStart.getTime();
                final String versionId = cameraImageS3Writer.writeImage(currentImageToWrite, realImage,
                                                                        filename, timestampEpochMillis);
                info.setS3VersionId(versionId);
                info.updateWriteStatusSuccess();
                final long s3WriteDuration = writeStart.getTime()-writeDuration;
                info.updateWriteTotalDurationMs(writeDuration + s3WriteDuration);
                info.setWriteDurationMs(0);
                info.setS3WriteDurationMs(s3WriteDuration);

            } catch (final Exception e) {
                info.updateWriteStatusFailed(e);
                throw new CameraImageWriteFailureException(e);
            }
            return null;
        });
    }

    private RetryTemplate getRetryTemplate() {
        final RetryTemplate retryTemplate = new RetryTemplate();
        final FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(retryDelayMs);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(RETRY_COUNT, retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);
        return retryTemplate;
    }

    static String resolvePresetIdFrom(final CameraPreset cameraPreset, final KuvaProtos.Kuva kuva) {
        return cameraPreset != null ? cameraPreset.getPresetId() : kuva.getNimi().substring(0, 8);
    }

    private static String getPresetImageName(final String presetId) {
        return presetId + ".jpg";
    }

    static class CameraImageReadFailureException extends RuntimeException {

        CameraImageReadFailureException(String message) {
            super(message);
        }

        CameraImageReadFailureException(Throwable cause) {
            super(cause);
        }
    }

    static class CameraImageWriteFailureException extends RuntimeException {

        CameraImageWriteFailureException(Throwable cause) {
            super(cause);
        }

    }

    byte[] getNoiseImage() {
        return noiseImage;
    }
}