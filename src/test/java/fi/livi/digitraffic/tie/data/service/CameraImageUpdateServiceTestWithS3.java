package fi.livi.digitraffic.tie.data.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.amazonaws.services.s3.model.S3Object;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.data.s3.AbstractCameraTestWithS3;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPresetHistory;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageReader;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageS3Writer;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageUpdateService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryDataService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryUpdateService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetService;

public class CameraImageUpdateServiceTestWithS3 extends AbstractCameraTestWithS3 {

    private static final Logger log = LoggerFactory.getLogger(CameraImageUpdateServiceTestWithS3.class);

    @MockBean
    private CameraImageReader cameraImageReader;

    @Autowired
    private CameraPresetService cameraPresetService;

    @Autowired
    private CameraImageUpdateService service;

    @Autowired
    private CameraPresetHistoryUpdateService cameraPresetHistoryUpdateService;

    @Autowired
    private CameraPresetHistoryDataService cameraPresetHistoryDataService;

    @Test
    public void versionHistoryAndPresetPublicityForTwoPresets() throws IOException {

        /*
         * Create 5 images for 2 presets.
         * Use loopIndex i with values 1-5 to generate publicity (i mod 2), image data array and lastModified offset.
         */
        final ZonedDateTime initialLastModified = DateHelper.toZonedDateTimeAtUtc(Instant.ofEpochSecond(Instant.now().getEpochSecond()));
        final Set<String> presetIds = new HashSet<>();

        cameraPresetService.findAllPublishableCameraPresets().stream().limit(2).forEach(cp -> {
            // Set station to public
            cp.getRoadStation().updatePublicity(true);
            cameraPresetService.save(cp);
            presetIds.add(cp.getPresetId());

            cameraPresetHistoryUpdateService.deleteAllWithPresetId(cp.getPresetId());
            Assert.assertTrue(cameraPresetHistoryDataService.findAllByPresetIdInclSecretAscInternal(cp.getPresetId()).isEmpty());

            // Init image data for all loop indexes
            try {
                when(cameraImageReader.readImage(eq(cp.getLotjuId()), any()))
                    .thenReturn(getAsByteArray(1),
                                getAsByteArray(2),
                                getAsByteArray(3),
                                getAsByteArray(4),
                                getAsByteArray(5));
            } catch (IOException e) {
                throw new RuntimeException();
            }

            IntStream.range(1, 6).forEach(loopIndex -> {
                // All even kuvaIds are public. Generate kuva and handle it to save history and image.
                final boolean isPublicPreset = isPublicPreset(loopIndex);
                final ZonedDateTime lastModified = initialLastModified.plusMinutes(loopIndex);
                final KuvaProtos.Kuva kuva = createKuva(lastModified, cp.getPresetId(), cp.getLotjuId(), isPublicPreset);
                service.handleKuva(kuva);

                // Now we have new latest image and also new history item. Check them.
                checkLatestS3ObjectAndHistory(cp.getPresetId(), lastModified, true, isPublicPreset, loopIndex);

                // Check version history
                final CameraPresetHistory latestHistory = cameraPresetHistoryDataService.findLatestWithPresetIdIncSecretInternal(cp.getPresetId());
                checkVersionedS3ObjectAndHistory(cp.getPresetId(), lastModified, true, isPublicPreset, loopIndex, latestHistory);
            });
        });

        // Now check that full history haven't changed
        presetIds.forEach(presetId -> {
            final AtomicInteger loopIndex = new AtomicInteger(1);
            final List<CameraPresetHistory> history = cameraPresetHistoryDataService.findAllByPresetIdInclSecretAscInternal(presetId);

            history.forEach(h -> {

                // All even versions are public
                final boolean shouldBePublic = isPublicPreset(loopIndex.get());

                checkVersionedS3ObjectAndHistory(h.getPresetId(), initialLastModified.plusMinutes(loopIndex.get()), true, shouldBePublic, loopIndex.get(), h);

                // update loop index
                loopIndex.getAndSet(loopIndex.get() + 1);
            });
        });

        verify(cameraImageReader, times(10)).readImage(anyLong(), any());
    }

    @Test
    public void versionHistoryAndCameraPublicity() {

        final ZonedDateTime initialLastModified =
            DateHelper.toZonedDateTimeAtUtc(Instant.ofEpochSecond(Instant.now().getEpochSecond()));

        final CameraPreset cp = cameraPresetService.findAllPublishableCameraPresets().stream().findFirst().get();

        cameraPresetHistoryUpdateService.deleteAllWithPresetId(cp.getPresetId());
        Assert.assertTrue(cameraPresetHistoryDataService.findAllByPresetIdInclSecretAscInternal(cp.getPresetId()).isEmpty());

        try {
            when(cameraImageReader.readImage(eq(cp.getLotjuId()), any()))
                .thenReturn(getAsByteArray(1),
                    getAsByteArray(2),
                    getAsByteArray(3),
                    getAsByteArray(4));
        } catch (IOException e) {
            throw new RuntimeException();
        }

        final String presetId = cp.getPresetId();

        /* Create image history with following publicity matrix
           Camera, Image, result
              T      T      T
              T      F      F
              F      F      F
              F      T      F
         */
        handleKuvaAndCheckLatestS3ObjectAndHistory(cp, initialLastModified.plusMinutes(1), true, true, 1);
        handleKuvaAndCheckLatestS3ObjectAndHistory(cp, initialLastModified.plusMinutes(2), true, false, 2);
        handleKuvaAndCheckLatestS3ObjectAndHistory(cp, initialLastModified.plusMinutes(3), false, true, 3);
        handleKuvaAndCheckLatestS3ObjectAndHistory(cp, initialLastModified.plusMinutes(4), false, false, 4);

        // Get history and check it is still correct
        final List<CameraPresetHistory> history = cameraPresetHistoryDataService.findAllByPresetIdInclSecretAscInternal(presetId);
        Assert.assertEquals(4, history.size());

        // Check version history to match matrix
        checkVersionedS3ObjectAndHistory(presetId, initialLastModified.plusMinutes(1), true, true, 1, history.get(0));
        checkVersionedS3ObjectAndHistory(presetId, initialLastModified.plusMinutes(2), true, false, 2, history.get(1));
        checkVersionedS3ObjectAndHistory(presetId, initialLastModified.plusMinutes(3), false, false, 3, history.get(2));
        checkVersionedS3ObjectAndHistory(presetId, initialLastModified.plusMinutes(4), false, true, 4, history.get(3));
    }

    private void handleKuvaAndCheckLatestS3ObjectAndHistory(CameraPreset cp, ZonedDateTime lastModified, boolean cameraPublicity, boolean presetPublicity, int imageDataIndex) {
        handleKuva(cp, lastModified, cameraPublicity, presetPublicity, imageDataIndex);
        checkLatestS3ObjectAndHistory(cp.getPresetId(), lastModified, cameraPublicity, presetPublicity, imageDataIndex);

    }

    private void checkVersionedS3ObjectAndHistory(final String presetId, final ZonedDateTime lastModified,
                                                  final boolean cameraPublicity, final boolean presetPublicity, int imageDataIndex,
                                                  CameraPresetHistory history) {
        // Check history data
        final boolean shouldBePublic = cameraPublicity && presetPublicity;
        Assert.assertEquals(shouldBePublic, history.getPublishable());
        // Last modified should equal
        final ZonedDateTime historyLastModified = history.getLastModified();
        Assert.assertEquals(lastModified, historyLastModified);

        // Check S3 image versioned data
        final byte[] image = readWeathercamS3DataVersion( CameraImageS3Writer.getVersionedKey(presetId), history.getVersionId());
        Assert.assertEquals(image.length, history.getSize().intValue());
        // S3 image data should be equal with written dat. Hidden images also has real data, no noise image.
        assertBytes(getAsByteArray(imageDataIndex), image);

        // S3 Object last modified should be equals with history
        final S3Object imageObject = readWeathercamS3ObjectVersion(history.getPresetId() + ".jpg", history.getVersionId());
        assertLastModified(historyLastModified, imageObject);
    }

    private void handleKuva(final CameraPreset cp, final ZonedDateTime lastModified, final boolean cameraPublicity, final boolean presetPublicity, int imageDataIndex) {
        final KuvaProtos.Kuva kuva = createKuva(lastModified, cp.getPresetId(), cp.getLotjuId(), presetPublicity);
        cp.getRoadStation().updatePublicity(cameraPublicity);
        cameraPresetService.save(cp);
        service.handleKuva(kuva);
    }

    private void checkLatestS3ObjectAndHistory(final String presetId, ZonedDateTime lastModified, final boolean cameraPublicity,
                                               final boolean presetPublicity, int imageDataIndex) {
        final boolean shouldBePublic = cameraPublicity && presetPublicity;
        final String key = presetId + ".jpg";
        // Check that latest image data is correct after update
        // Latest image's bytes equals with actual (public) or noise image (hidden)
        final byte[] image = readWeathercamS3Data(key);
        assertBytes(shouldBePublic ? getAsByteArray(imageDataIndex) : service.getNoiseImage(), image);
        // S3 Object lastmodified is correct
        final S3Object imageS3Object = readWeathercamS3Object(key);
        assertLastModified(lastModified, imageS3Object);

        // Check latest history data
        final CameraPresetHistory latestHistory = cameraPresetHistoryDataService.findLatestWithPresetIdIncSecretInternal(key.substring(0, key.length()-4));
        Assert.assertEquals(shouldBePublic, latestHistory.getPublishable());
        Assert.assertEquals(lastModified, latestHistory.getLastModified());
    }

    private void assertLastModified(final ZonedDateTime expected, final S3Object imageObject) {
        final String s3LastMofidiedHeader = imageObject.getObjectMetadata().getUserMetaDataOf(CameraImageS3Writer.LAST_MODIFIED_USER_METADATA_HEADER);
        final String historyLastModifiedHeader = CameraImageS3Writer.getInLastModifiedHeaderFormat(expected.toInstant());
        Assert.assertEquals(historyLastModifiedHeader, s3LastMofidiedHeader);
    }

    /**
     * @return true if parameter is even
     */
    private boolean isPublicPreset(final int i) {
        return i % 2 == 0;
    }

    private void assertBytes(byte[] bytes1, byte[] bytes2) {
        Assert.assertArrayEquals(bytes1 , bytes2);
    }

    private byte[] getAsByteArray(int i) {
        byte[] arr = new byte[i];
        Arrays.fill(arr, (byte)i);
        return arr;
    }

    private KuvaProtos.Kuva createKuva(final ZonedDateTime lastModified, final String presetId, final long esiasentoId, final boolean isPublic) {
        return KuvaProtos.Kuva.newBuilder()
            .mergeFrom(KuvaProtos.Kuva.getDefaultInstance())
            .setAikaleima(lastModified.toInstant().toEpochMilli())
            .setEsiasentoId(esiasentoId)
            .setNimi(presetId + RandomStringUtils.randomAlphanumeric(10))
            .setJulkinen(isPublic)
            .setKuvaId(esiasentoId)
            .setKameraId(1)
            .setEsiasennonNimi("Esiasento " + presetId)
            .setAsemanNimi("Asema " + presetId)
            .build();
    }
}
