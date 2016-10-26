package fi.livi.digitraffic.tie.data.jms;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;
import fi.livi.digitraffic.tie.data.service.CameraDataUpdateService;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraStationUpdater;

@Transactional
public class CameraJmsMessageListenerTest extends MetadataIntegrationTest {
    
    private static final Logger log = LoggerFactory.getLogger(CameraJmsMessageListenerTest.class);

    private static final String REQUEST_PATH = "/Kamerakuva/";
    private static final String IMAGE_SUFFIX = "image.jpg";
    private static final String IMAGE_DIR = "lotju/kuva/";

    @Autowired
    private CameraPresetService cameraPresetService;

    @Autowired
    private CameraDataUpdateService cameraDataUpdateService;

    @Autowired
    LockingService lockingService;

    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @Autowired
    ResourceLoader resourceLoader;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    private Map<String, byte[]> imageFilesMap = new HashMap<>();

    private static TemporaryFolder testFolder;

    @BeforeClass
    public static void beforeClass() throws IOException {
        testFolder = new TemporaryFolder();
        testFolder.create();
        String path = testFolder.getRoot().getPath();
        log.info("Created temporarry weathercam importDir: " + path);
    }

    @AfterClass
    public static void afterClass() {
        if (testFolder.getRoot().exists()) {
            testFolder.delete();
        }
        Assert.assertFalse(testFolder.getRoot().exists());
    }

    @Before
    public void setUpTestData() throws IOException {

        cameraDataUpdateService.setWeathercamImportDir(getImportDir());

        int i = 5;
        while (i > 0) {
            String imageName = i + "image.jpg";
            Resource resource = resourceLoader.getResource("classpath:" + IMAGE_DIR + imageName);
            final File imageFile = resource.getFile();
            byte[] bytes = FileUtils.readFileToByteArray(imageFile);
            imageFilesMap.put(imageName, bytes);
            i--;
        }

        cameraStationUpdater.fixCameraPresetsWithMissingRoadStations();

        List<CameraPreset> nonObsoleteCameraPresets = cameraPresetService.findAllNonObsoleteCameraPresets();
        log.info("Non obsolete CameraPresets before " + nonObsoleteCameraPresets.size());
        Map<String, CameraPreset> cameraPresets = cameraPresetService.finAllCameraPresetsMappedByPresetId();

        int missingMin = 1000 - nonObsoleteCameraPresets.size();
        Iterator<CameraPreset> iter = cameraPresets.values().iterator();
        while (missingMin > 0 && iter.hasNext()) {
            CameraPreset cp = iter.next();
            RoadStation rs = cp.getRoadStation();
            if (rs.isObsolete() || cp.isObsolete()) {
                missingMin--;
            }
            if (rs.isObsolete()) {
                rs.setObsolete(false);
            }
            if (!rs.isPublic()) {
                rs.setPublic(true);
            }
            if (cp.isObsolete()) {
                cp.setObsolete(false);
            }
        }

        nonObsoleteCameraPresets = cameraPresetService.findAllNonObsoleteCameraPresets();
        log.info("Non obsolete CameraPresets for testing " + nonObsoleteCameraPresets.size());
    }

    @Test
    public void testPerformanceForReceivedMessages() throws IOException, JAXBException, DatatypeConfigurationException {

        log.info("Using weathercam.importDir: " + testFolder.getRoot().getPath());

        log.info("Init mock http-server for images");
        int port = wireMockRule.port();
        log.info("Mock server port: " + port);
        createHttpResponseStubFor(1 + IMAGE_SUFFIX);
        createHttpResponseStubFor(2 + IMAGE_SUFFIX);
        createHttpResponseStubFor(3 + IMAGE_SUFFIX);
        createHttpResponseStubFor(4 + IMAGE_SUFFIX);
        createHttpResponseStubFor(5 + IMAGE_SUFFIX);

        JmsMessageListener<Kuva> cameraJmsMessageListener =
                new JmsMessageListener<Kuva>(Kuva.class, "cameraJmsMessageListener", lockingService, UUID.randomUUID().toString()) {
            @Override
            protected void handleData(List<Kuva> data) {
                long start = System.currentTimeMillis();
                if (TestTransaction.isActive()) {
                    TestTransaction.flagForCommit();
                    TestTransaction.end();
                }
                TestTransaction.start();
                try {
                    cameraDataUpdateService.updateCameraData(data);
                } catch (SQLException e) {
                    Assert.fail("Data updating failed");
                }
                TestTransaction.flagForCommit();
                TestTransaction.end();
                long end = System.currentTimeMillis();
                log.info("handleData took " + (end-start) + " ms");
            }
        };

        DatatypeFactory df = DatatypeFactory.newInstance();
        GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
        XMLGregorianCalendar xgcal = df.newXMLGregorianCalendar(gcal);

        // Generate update-data
        List<CameraPreset> presets = cameraPresetService.findAllNonObsoleteCameraPresets();
        Iterator<CameraPreset> presetIterator = presets.iterator();



        int testBurstsLeft = 10;
        long handleDataTotalTime = 0;
        long maxHandleTime = testBurstsLeft * 2000;
        final List<Kuva> data = new ArrayList<>(presets.size());

        StopWatch sw = new StopWatch();
        while(testBurstsLeft > 0) {
            testBurstsLeft--;
            sw.reset();
            sw.start();

            data.clear();
            while ( true && presetIterator.hasNext() ) {
                CameraPreset preset = presetIterator.next();

                // Kuva: {"asemanNimi":"Vaalimaa_testi","nimi":"C0364302201610110000.jpg","esiasennonNimi":"esiasento2","esiasentoId":3324,"kameraId":1703,"aika":2016-10-10T21:00:40Z,"tienumero":7,"tieosa":42,"tieosa":false,"url":"https://testioag.liikennevirasto.fi/LOTJU/KameraKuvavarasto/6845284"}
                int kuvaIndex = RandomUtils.nextInt(1, 6);
                Kuva kuva = new Kuva();
                kuva.setAika(xgcal);
                kuva.setAsemanNimi("Suomenmaa " + RandomUtils.nextLong(1000,9999));
                kuva.setEsiasennonNimi("Esiasento" + RandomUtils.nextLong(1000,9999));
                kuva.setEsiasentoId(RandomUtils.nextLong(1000,9999));
                kuva.setEtaisyysTieosanAlusta(BigInteger.valueOf(RandomUtils.nextLong(0,99999)));
                kuva.setJulkinen(true);
                kuva.setKameraId(Long.parseLong(preset.getCameraId().substring(1)));
                kuva.setLiviId("" + kuvaIndex);
                kuva.setNimi(preset.getPresetId() + "1234.jpg");
                if (preset.getRoadStation().getRoadAddress() != null ) {
                    kuva.setTienumero(BigInteger.valueOf(preset.getRoadStation().getRoadAddress().getRoadNumber()));
                    kuva.setTieosa(BigInteger.valueOf(preset.getRoadStation().getRoadAddress().getRoadSection()));
                }
                kuva.setUrl("http://localhost:" + port + REQUEST_PATH + kuvaIndex + IMAGE_SUFFIX);
                kuva.setXKoordinaatti("12345.67");
                kuva.setYKoordinaatti("23456.78");

                data.add(kuva);
                xgcal.add(df.newDuration(1000));
                if (data.size() >= 25) {
                    break;
                }
            }
            sw.stop();
            long generation = sw.getTime();
            log.info("Data generation took " + generation + " ms");

            sw.reset();
            sw.start();
            Assert.assertTrue(data.size() >= 25);
            cameraJmsMessageListener.handleData(data);
            sw.stop();
            log.info("Data handle took " + sw.getTime() + " ms");
            handleDataTotalTime += sw.getTime();

            try {

                // send data with 1 s intervall
                long sleep = 1000 - generation;
                if (sleep < 0) {
                    log.error("Data generation took too long");
                } else {
                    Thread.sleep(sleep);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("Handle kuva data total took " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms " + (handleDataTotalTime <= maxHandleTime ? "(OK)" : "(FAIL)"));

        log.info("Check data validy");

        Map<String, CameraPreset> updatedPresets = cameraPresetService.finAllCameraPresetsMappedByPresetId();

        for (Kuva kuva : data) {
            String presetId = CameraHelper.resolvePresetId(kuva);
            // Check written image against source image
            byte[] dst = readCameraDataFromDisk(presetId);
            byte[] src = imageFilesMap.get(kuva.getLiviId() + IMAGE_SUFFIX);
            Assert.assertArrayEquals("Written image is invalid for " + presetId, src, dst);

            // Check preset updated to db against kuva
            CameraPreset preset = updatedPresets.get(presetId);
            LocalDateTime kuvaTaken = DateHelper.toLocalDateTimeAtDefaultZone(kuva.getAika());
            LocalDateTime presetPictureLastModified = preset.getPictureLastModified();
            Assert.assertEquals("Preset not updated with kuva's timestamp", kuvaTaken, presetPictureLastModified);
        }

        Assert.assertTrue("Handle data took too much time " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms", handleDataTotalTime <= maxHandleTime);
    }

    String getImportDir() {
        return testFolder.getRoot().getPath();
    }

    private byte[] readCameraDataFromDisk(String presetId) throws IOException {
        final File imageFile = new File(getImportDir() + "/" + presetId + ".jpg");
        return FileUtils.readFileToByteArray(imageFile);
    }

    private void createHttpResponseStubFor(String kuva) throws IOException {
        log.info("Create mock with url: " + REQUEST_PATH + kuva);
        stubFor(get(urlEqualTo(REQUEST_PATH + kuva))
                .willReturn(aResponse().withBody(imageFilesMap.get(kuva))
                .withHeader("Content-Type", "image/jpeg")
                .withStatus(200)));
    }
}