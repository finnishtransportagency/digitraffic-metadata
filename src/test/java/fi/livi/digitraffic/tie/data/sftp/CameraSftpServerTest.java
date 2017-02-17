package fi.livi.digitraffic.tie.data.sftp;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.messaging.MessagingException;
import org.springframework.util.ReflectionUtils;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;
import fi.livi.digitraffic.tie.data.service.CameraDataUpdateService;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraStationUpdater;

@Transactional
public class CameraSftpServerTest extends AbstractSftpTest {
    private static final Logger log = LoggerFactory.getLogger(MetadataIntegrationTest.class);

    private static final String RESOURCE_IMAGE_SUFFIX = "image.jpg";
    private static final String IMAGE_DIR = "lotju/kuva/";
    private static final int TEST_UPLOADS = 10;

    @Autowired
    private SessionFactory sftpSessionFactory;

    @Autowired
    private CameraDataUpdateService cameraDataUpdateService;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    CameraStationUpdater cameraStationUpdater;

    @Autowired
    CameraPresetService cameraPresetService;

    @Value("${camera-image-uploader.sftp.uploadFolder}")
    private String sftpUploadFolder;

    @Value("${camera-image-uploader.sftp.poolSize}")
    Integer poolSize;

    @Value("${camera-image-uploader.sftp.sessionWaitTimeout}")
    Long sessionWaitTimeout;

    private Map<String, byte[]> imageFilesMap = new HashMap<>();


    private static final String CAMERA_ID = "C01502";

    private ArrayList<Kuva> kuvas = new ArrayList<>();

    @Before
    public void setUpTestData() throws IOException {

        log.info("Init test data");
        // Creates also new road stations so run before generating lotjuIds
        cameraStationUpdater.fixCameraPresetsWithMissingRoadStations();
        entityManager.flush();
        entityManager.clear();
        generateMissingLotjuIdsWithJdbc();
        fixDataWithJdbc();
        entityManager.flush();
        entityManager.clear();

        // Init minimum TEST_UPLOADS non obsolete presets
        List<CameraPreset> nonObsoleteCameraPresets = cameraPresetService.findAllPublishableCameraPresets();
        log.info("Non obsolete CameraPresets before " + nonObsoleteCameraPresets.size());
        Map<Long, CameraPreset> cameraPresets = cameraPresetService.findAllCameraPresetsMappedByLotjuId();

        int missingCount = TEST_UPLOADS - nonObsoleteCameraPresets.size();
        Iterator<CameraPreset> iter = cameraPresets.values().iterator();
        while (missingCount > 0 && iter.hasNext()) {
            CameraPreset cp = iter.next();
            RoadStation rs = cp.getRoadStation();
            if (rs.isObsolete() || cp.isObsolete() || !rs.isPublic() || !cp.isPublic() || !cp.isPublicExternal()) {
                missingCount--;
            }
            rs.setObsolete(false);
            rs.setObsolete(false);
            rs.setPublic(true);
            cp.setObsolete(false);
            cp.setPublicInternal(true);
            cp.setPublicExternal(true);
        }

        // Active presets
        List<CameraPreset> activePresets = cameraPresetService.findAllPublishableCameraPresets();
        nonObsoleteCameraPresets = activePresets.subList(0, Math.min(TEST_UPLOADS, activePresets.size()));
        log.info("Non obsolete CameraPresets for testing " + nonObsoleteCameraPresets.size());

        // Missing presets in db, images should get deleted
        int i = 0;
        final ArrayList<CameraPreset> missingCameraPresets = new ArrayList<>();
        while (i < 5) {
            i++;
            missingCameraPresets.add(generateMissingDummyPreset());
        }

        Session session = this.sftpSessionFactory.getSession();
        if (!session.exists(sftpUploadFolder)) {
            session.mkdir(sftpUploadFolder);
        }

        List<CameraPreset> cps = Stream.concat(nonObsoleteCameraPresets.stream(), missingCameraPresets.stream()).collect(Collectors.toList());
        int count = 0;
        for (CameraPreset cp : cps) {
            count++;
            final int imageNumber = (count % 5)+1;

            log.info("Load image resource {}{} for preset {}", imageNumber, RESOURCE_IMAGE_SUFFIX, cp.getPresetId());
            final Resource resource = resourceLoader.getResource("classpath:" + IMAGE_DIR + imageNumber + RESOURCE_IMAGE_SUFFIX);
            final File imageFile = resource.getFile();
            final byte[] bytes = FileUtils.readFileToByteArray(imageFile);
            final Kuva kuva = createKuvaDataAndHttpStub(cp, bytes);
            kuvas.add(kuva);

            // Upload missing presets images to server
            if (cp.getPresetId().startsWith("X")) {
                log.info("Write image to sftp that should be deleted by update {}", getSftpPath(kuva));
                session.write(new ByteArrayInputStream(bytes), getSftpPath(kuva));
                Session otherSession = this.sftpSessionFactory.getSession();
                assertTrue("Image not found on sftp server", otherSession.exists(getSftpPath(kuva)));
                otherSession.close();
            }
        }
        session.close();
    }

    @After
    public void restoreData() {
        restoreGeneratedLotjuIdsWithJdbc();
    }

    @Test
    public void testUpdateCameraDataToSftp() throws Exception {

        cameraDataUpdateService.updateCameraData(kuvas);

        try (final Session session = this.sftpSessionFactory.getSession()) {

            kuvas.stream().forEach(kuva -> {
                String filePath = getSftpPath(kuva);
                if (kuva.getNimi().startsWith("X")) {
                    try {
                        Assert.assertFalse("Image should have been deleted from sftp server", session.exists(getSftpPath(kuva)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    log.info("Read preset {} image back from server from path {}", kuva.getEsiasentoId(), filePath);
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        session.read(filePath, out);
                        byte[] initialData = imageFilesMap.get(kuva.getNimi());
                        byte[] readData = out.toByteArray();
                        Assert.assertArrayEquals("Preset " + kuva.getNimi() + " image data read from sever is not equal with initial content",
                                initialData, readData);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    @Test
    public void testSessionCachingPoolLimit() {

        HashSet<Session> sessions = new HashSet<>();
        while(sessions.size() < poolSize+1) {
            log.info("Get session {}", sessions.size()+1);
            if (sessions.size() < poolSize) {
                sessions.add(this.sftpSessionFactory.getSession());
            } else {
                // getting session out of pool fails
                log.info("Getting session of full should fail after timeout");
                StopWatch time = StopWatch.createStarted();
                boolean fail = false;
                try {
                    Session session = this.sftpSessionFactory.getSession();
                    session.close();
                } catch (MessagingException e) {
                    fail = true;
                    time.stop();
                    log.info("Timeout took {} ms", time.getTime());
                    assertTrue(time.getTime() >= sessionWaitTimeout);
                    assertTrue(time.getTime() <= sessionWaitTimeout+100);
                }
                assertTrue("Get session should have failed after timeout " + sessionWaitTimeout, fail);
                break;
            }
        }
        sessions.stream().forEach(Session::close);
    }

    @Test
    public void testSessionCaching() {

        HashSet<Session> newSessions = new HashSet<>();
        while(newSessions.size() < poolSize) {
            log.info("Get new session {}", newSessions.size()+1);
            newSessions.add(this.sftpSessionFactory.getSession());
        }
        // relase sessions to pool
        newSessions.stream().forEach(Session::close);

        Field sessionField = ReflectionUtils.findField(CachingSessionFactory.CachedSession.class, "targetSession");
        sessionField.setAccessible(true);
        Set<Session> newRealSessions = new HashSet<>();
        newSessions.stream().forEach(s -> {
            newRealSessions.add((Session) ReflectionUtils.getField(sessionField, s));
        });

        HashSet<Session> cachedSessions = new HashSet<>();
        while(cachedSessions.size() < poolSize) {
            log.info("Get cached session {}", cachedSessions.size()+1);
            cachedSessions.add(this.sftpSessionFactory.getSession());
        }
        Set<Session> cachedRealSessions = new HashSet<>();
        cachedSessions.stream().forEach(s -> {
            cachedRealSessions.add((Session) ReflectionUtils.getField(sessionField, s));
        });

        assertTrue("All sessions should be found from cachedSessions", cachedRealSessions.containsAll(newRealSessions));
        cachedSessions.stream().forEach(Session::close);
    }


    private CameraPreset generateMissingDummyPreset() {
        CameraPreset cp = new CameraPreset();
        String cameraId = "X" + RandomUtils.nextLong(10000, 100000);
        String direction = String.valueOf(RandomUtils.nextLong(10, 100));
        cp.setPresetId(cameraId + direction);
        cp.setCameraId(cameraId);
        cp.setRoadStation(new RoadStation(RoadStationType.CAMERA_STATION));
        return cp;
    }

    private Kuva createKuvaDataAndHttpStub(final CameraPreset cp, final byte[] data) {
        try {
            DatatypeFactory df = DatatypeFactory.newInstance();
            GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
            XMLGregorianCalendar xgcal = df.newXMLGregorianCalendar(gcal);

            Kuva kuva = new Kuva();
            kuva.setNimi(cp.getPresetId());
            kuva.setAika((XMLGregorianCalendar) xgcal.clone());
            kuva.setAsemanNimi("Suomenmaa " + RandomUtils.nextLong(1000, 10000));
            kuva.setEsiasennonNimi("Esiasento" + RandomUtils.nextLong(1000, 10000));
            kuva.setEsiasentoId(cp.getLotjuId() != null ? cp.getLotjuId() : RandomUtils.nextLong(10000, 100000));
            kuva.setEtaisyysTieosanAlusta(BigInteger.valueOf(RandomUtils.nextLong(0, 99999)));
            kuva.setJulkinen(true);
            kuva.setKameraId(Long.parseLong(cp.getCameraId().substring(1)));
            kuva.setLiviId("" + RandomUtils.nextLong(0, 99999));
            if (cp.getRoadStation().getRoadAddress() != null) {
                kuva.setTienumero(BigInteger.valueOf(cp.getRoadStation().getRoadAddress().getRoadNumber()));
                kuva.setTieosa(BigInteger.valueOf(cp.getRoadStation().getRoadAddress().getRoadSection()));
            }
            kuva.setUrl(getImageUrl(cp.getPresetId()));
            kuva.setXKoordinaatti("12345.67");
            kuva.setYKoordinaatti("23456.78");

            if (data != null) {
                createHttpResponseStubFor(cp.getPresetId(), data);
            }
            return kuva;
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }

    }

    private void createHttpResponseStubFor(final String presetId, final byte[] data) {
        imageFilesMap.put(presetId, data);
        final String url = getImagePath(presetId);
        log.info("Create mock with url {}", url);
        stubFor(get(urlEqualTo(getImagePath(presetId)))
                .willReturn(aResponse().withBody(data)
                        .withHeader("Content-Type", "image/jpeg")
                        .withStatus(200)));
    }
}
