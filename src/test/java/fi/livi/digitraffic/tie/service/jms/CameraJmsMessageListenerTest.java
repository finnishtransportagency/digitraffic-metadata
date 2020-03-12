package fi.livi.digitraffic.tie.service.jms;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.service.jms.marshaller.KuvaMessageMarshaller;
import fi.livi.digitraffic.tie.data.s3.AbstractCameraTestWithS3;
import fi.livi.digitraffic.tie.service.v1.camera.CameraDataUpdateService;
import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetService;

@TestPropertySource(properties = { "camera-image-uploader.imageUpdateTimeout=500",
                                   "road.datasource.hikari.maximum-pool-size=6",
                                   "logging.level.fi.livi.digitraffic.tie.service.v1.camera.CameraImageUpdateService=WARN"})
public class CameraJmsMessageListenerTest extends AbstractCameraTestWithS3 {
    private static final Logger log = LoggerFactory.getLogger(CameraJmsMessageListenerTest.class);

    private static final String IMAGE_SUFFIX = "image.jpg";
    private static final String IMAGE_DIR = "lotju/kuva/";

    @Autowired
    private CameraPresetService cameraPresetService;

    @Autowired
    private CameraDataUpdateService cameraDataUpdateService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AmazonS3 amazonS3Client;

    @Value("${dt.amazon.s3.weathercam.bucketName}")
    private String bucketName;

    private Map<String, byte[]> imageFilesMap = new HashMap<>();
    @Before
    public void initData() throws IOException {

        int i = 5;
        while (i > 0) {
            final String imageName = i + IMAGE_SUFFIX;
            final Resource resource = resourceLoader.getResource("classpath:" + IMAGE_DIR + imageName);
            final File imageFile = resource.getFile();
            final byte[] bytes = FileUtils.readFileToByteArray(imageFile);

            imageFilesMap.put(imageName, bytes);
            i--;
        }

        final List<CameraPreset> nonObsoleteCameraPresets = cameraPresetService.findAllPublishableCameraPresets();
        log.info("Non obsolete CameraPresets before:{}", nonObsoleteCameraPresets.size());

        final Map<Long, CameraPreset> cameraPresets = cameraPresetService.findAllCameraPresetsMappedByLotjuId();
        log.info("All camera presets size cameraPresetsCount={}", cameraPresets.size());

        int missingMin = 1000 - nonObsoleteCameraPresets.size();
        final Iterator<CameraPreset> iter = cameraPresets.values().iterator();

        while (missingMin > 0 && iter.hasNext()) {
            final CameraPreset cp = iter.next();
            final RoadStation rs = cp.getRoadStation();

            if (!rs.isPublishable() || !cp.isPublishable()) {
                missingMin--;
            }
            rs.setCollectionStatus(CollectionStatus.GATHERING);
            rs.unobsolete();
            rs.updatePublicity(true);
            cp.unobsolete();
            cp.setPublic(true);
        }
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Send some data bursts to jms handler and test performance of database updates.
     */
    @Test
    public void testPerformanceForReceivedMessages() throws IOException, JMSException {
        log.info("HTTP mock server for images port={}", TEST_PORT);

        createHttpResponseStubFor(1);// + IMAGE_SUFFIX);
        createHttpResponseStubFor(2);// + IMAGE_SUFFIX);
        createHttpResponseStubFor(3);// + IMAGE_SUFFIX);
        createHttpResponseStubFor(4);// + IMAGE_SUFFIX);
        createHttpResponseStubFor(5);// + IMAGE_SUFFIX);

        final JMSMessageListener.JMSDataUpdater<KuvaProtos.Kuva> dataUpdater = (data) -> {
            final StopWatch start = StopWatch.createStarted();
            if (TestTransaction.isActive()) {
                TestTransaction.flagForCommit();
                TestTransaction.end();
            }
            TestTransaction.start();
            int updated = 0;
            try {
                updated = cameraDataUpdateService.updateCameraData(data);
            } catch (Exception e) {
                Assert.fail("Data updating failed");
            }
            TestTransaction.flagForCommit();
            TestTransaction.end();
            log.info("handleData tookMs={}", start.getTime());
            return updated;
        };

        final JMSMessageListener<KuvaProtos.Kuva> cameraJmsMessageListener =
            new JMSMessageListener<>(new KuvaMessageMarshaller(), dataUpdater, true, log);

        Instant time = Instant.now();

        // Generate update-data
        final List<CameraPreset> presets = cameraPresetService.findAllPublishableCameraPresets();
        final Iterator<CameraPreset> presetIterator = presets.iterator();

        int testBurstsLeft = 10;
        long handleDataTotalTime = 0;
        long maxHandleTime = testBurstsLeft * 2200;
        final List<KuvaProtos.Kuva> data = new ArrayList<>(presets.size());

        final StopWatch sw = new StopWatch();
        while (testBurstsLeft > 0) {
            testBurstsLeft--;
            sw.reset();
            sw.start();

            data.clear();
            while (presetIterator.hasNext()) {
                CameraPreset preset = presetIterator.next();

                // Kuva: {"asemanNimi":"Vaalimaa_testi","nimi":"C0364302201610110000.jpg","esiasennonNimi":"esiasento2","esiasentoId":3324,"kameraId":1703,"aika":2016-10-10T21:00:40Z,"tienumero":7,"tieosa":42,"tieosa":false,"url":"https://testioag.liikennevirasto.fi/LOTJU/KameraKuvavarasto/6845284"}
                int kuvaIndex = RandomUtils.nextInt(1, 6);

                KuvaProtos.Kuva.Builder kuvaBuilder = KuvaProtos.Kuva.newBuilder();
                kuvaBuilder.setEsiasentoId(preset.getLotjuId());
                kuvaBuilder.setKameraId(preset.getCameraLotjuId());
                kuvaBuilder.setNimi(preset.getPresetId() + "1234.jpg");
                kuvaBuilder.setAikaleima(time.toEpochMilli());
                kuvaBuilder.setAsemanNimi("Suomenmaa " + RandomUtils.nextLong(1000, 9999));
                kuvaBuilder.setEsiasennonNimi("Esiasento" + RandomUtils.nextLong(1000, 9999));
                kuvaBuilder.setEtaisyysTieosanAlusta(RandomUtils.nextInt(0, 99999));
                kuvaBuilder.setJulkinen(true);
                kuvaBuilder.setLiviId("" + kuvaIndex);
                kuvaBuilder.setKuvaId(kuvaIndex);

                if (preset.getRoadStation().getRoadAddress() != null) {
                    kuvaBuilder.setTienumero(preset.getRoadStation().getRoadAddress().getRoadNumber());
                    kuvaBuilder.setTieosa(preset.getRoadStation().getRoadAddress().getRoadSection());
                }
                //kuvaBuilder.setUrl("http://localhost:" + httpPort + REQUEST_PATH + kuvaIndex + IMAGE_SUFFIX);
                kuvaBuilder.setXKoordinaatti("12345.67");
                kuvaBuilder.setYKoordinaatti("23456.78");

                KuvaProtos.Kuva kuva = kuvaBuilder.build();
                data.add(kuva);

                time = time.plusMillis(1000);

                cameraJmsMessageListener.onMessage(createBytesMessage(kuva));

                if (data.size() >= 25) {
                    break;
                }
            }

            final long generation = sw.getTime();

            sw.reset();
            sw.start();
            Assert.assertTrue("Data size too small: " + data.size(), data.size() >= 25);
            cameraJmsMessageListener.drainQueueScheduled();
            log.info("Data handle took " + sw.getTime() + " ms");
            handleDataTotalTime += sw.getTime();

            try {
                // send data with 1 s intervall
                long sleep = 1000 - generation;
                if (sleep > 0) {
                    Thread.sleep(sleep);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("Handle kuva data total took {} ms and max was {} ms success={}",
            handleDataTotalTime, maxHandleTime, (handleDataTotalTime <= maxHandleTime ? "OK" : "FAIL"));

        log.info("Check data validy");

        final Map<Long, CameraPreset> updatedPresets = cameraPresetService.findAllCameraPresetsMappedByLotjuId();

        for (KuvaProtos.Kuva kuva : data) {
            String presetId = CameraHelper.resolvePresetId(kuva);
            // Check written image against source image
            byte[] dst = readCameraImageFromS3(presetId);
            byte[] src = imageFilesMap.get(kuva.getKuvaId() + IMAGE_SUFFIX);
            Assert.assertArrayEquals("Written image is invalid for " + presetId, src, dst);

            // Check preset updated to db against kuva
            CameraPreset preset = updatedPresets.get(kuva.getEsiasentoId());

            Instant kuvaTaken = Instant.ofEpochMilli(kuva.getAikaleima());
            Instant presetPictureLastModified = preset.getPictureLastModified().toInstant();

            Assert.assertEquals("Preset not updated with kuva's timestamp " + preset.getPresetId(), kuvaTaken, presetPictureLastModified);
        }
        log.info("Data is valid");
        Assert.assertTrue("Handle data took too much time " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms",
                handleDataTotalTime <= maxHandleTime);
    }

    private byte[] readCameraImageFromS3(final String presetId) throws IOException {
        final String key = presetId + ".jpg";
        final S3Object o = amazonS3Client.getObject(bucketName, key);
        final byte[] imageData = o.getObjectContent().readAllBytes();
        o.getObjectContent().close();
        return imageData;
    }

    private void createHttpResponseStubFor(int kuvaId) {
        log.info("Create mock with url: " + REQUEST_PATH + kuvaId);
        stubFor(get(urlEqualTo(REQUEST_PATH + kuvaId))
                .willReturn(aResponse().withBody(imageFilesMap.get(kuvaId + IMAGE_SUFFIX))
                        .withHeader("Content-Type", "image/jpeg")
                        .withStatus(200)));
    }

    public static BytesMessage createBytesMessage(final KuvaProtos.Kuva kuva) throws JMSException, IOException {
        final org.apache.commons.io.output.ByteArrayOutputStream bous = new org.apache.commons.io.output.ByteArrayOutputStream(0);
        kuva.writeDelimitedTo(bous);
        final byte[] kuvaBytes = bous.toByteArray();

        final BytesMessage bytesMessage = mock(BytesMessage.class);

        when(bytesMessage.getBodyLength()).thenReturn((long)kuvaBytes.length);
        when(bytesMessage.readBytes(any(byte[].class))).then(invocation -> {
            final byte[] bytes = (byte[]) invocation.getArguments()[0];
            System.arraycopy(kuvaBytes, 0, bytes, 0, kuvaBytes.length);

            return kuvaBytes.length;
        });

        return bytesMessage;
    }
}
