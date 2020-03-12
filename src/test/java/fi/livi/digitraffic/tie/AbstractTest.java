package fi.livi.digitraffic.tie;

import static fi.livi.digitraffic.tie.external.lotju.metadata.kamera.JulkisuusTaso.JULKINEN;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.Julkisuus;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.JulkisuusTaso;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.JulkisuusVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KeruunTILA;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.TieosoiteVO;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadAddress;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.model.v1.camera.CameraType;

@TestPropertySource(properties = {
    "logging.level.org.springframework.test.context.transaction.TransactionContext=WARN"
})
public abstract class AbstractTest {

    @Autowired
    protected ResourceLoader resourceLoader;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @PersistenceContext
    protected EntityManager entityManager;

    protected static final int MIN_LOTJU_ID = 10000;
    protected static final int MAX_LOTJU_ID = 99999;
    protected static final String PRESET_PRESENTATION_NAME = "PresentationName";

    protected Path getPath(final String filename) {
        return new File(getClass().getResource(filename).getFile()).toPath();
    }

    protected List<Resource> loadResources(final String pattern) throws IOException {
        return Arrays.asList(ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern));
    }

    protected Resource loadResource(final String pattern) throws IOException {
        return resourceLoader.getResource(pattern);
    }

    protected ArrayList<String> readResourceContents(final String resourcePattern) throws IOException {
        final List<Resource> datex2Resources = loadResources(resourcePattern);
        final ArrayList<String> contents = new ArrayList<>();

        for (final Resource datex2Resource : datex2Resources) {
            contents.add(FileUtils.readFileToString(datex2Resource.getFile(), StandardCharsets.UTF_8));
        }
        return contents;
    }

    protected String readResourceContent(final String resourcePattern) throws IOException {
        final Resource datex2Resource = loadResource(resourcePattern);

        return FileUtils.readFileToString(datex2Resource.getFile(), StandardCharsets.UTF_8);
    }

    public static CameraPreset generateDummyPreset() {
        final RoadStation rs = generateDummyRoadStation(RoadStationType.CAMERA_STATION);

        final CameraPreset cp = new CameraPreset();
        cp.setRoadStation(rs);

        final String cameraId = "C" + rs.getNaturalId();
        final String direction = String.valueOf(RandomUtils.nextLong(10, 100));
        cp.setPresetId(cameraId + direction);
        cp.setCameraId(cameraId);
        cp.setPublic(true);
        cp.setLotjuId(RandomUtils.nextLong(100000000, 1000000000) * -1);
        cp.setInCollection(true);
        cp.setCameraType(CameraType.VAPIX);
        cp.setCompression(50);
        cp.setDefaultDirection(true);
        cp.setResolution("1920x1080");
        cp.setDirection("1");
        cp.setLotjuId(rs.getLotjuId());
        cp.setCameraLotjuId(cp.getLotjuId());

        return cp;
    }

    public static RoadStation generateDummyRoadStation(final RoadStationType roadStationType) {

        final RoadStation rs = RoadStation.createRoadStation(roadStationType);
        rs.setNaturalId(80000  + RandomUtils.nextLong(1000, 10000));
        rs.setName(roadStationType.name());
        rs.setLotjuId(rs.getNaturalId());
        rs.updatePublicity(true);
        rs.setCollectionStatus(CollectionStatus.GATHERING);
        rs.setPurpose("Maisema");
        rs.setLatitude(BigDecimal.valueOf(6687086));
        rs.setLongitude(BigDecimal.valueOf(351822));
        rs.setAltitude(BigDecimal.TEN);
        rs.setMunicipality("Vihti");
        rs.setMunicipalityCode("927");
        rs.setProvince("Uusimaa");
        rs.setProvinceCode("1");
        rs.setName("vt1_Vihti_Palojärvi_1");
        rs.setNameFi("Tie 1 Vihti, Palojärvi");
        rs.setNameEn("Road 1 Vihti, Palojärvi");
        rs.setNameSv("Väg 1 Vihtis, Palojärvi");

        RoadAddress ra = generateDummyRoadAddres();
        rs.setRoadAddress(ra);

        return rs;
    }

    public static RoadAddress generateDummyRoadAddres() {
        final RoadAddress ra = new RoadAddress();
        ra.setCarriagewayCode(1);
        ra.setSideCode(1);
        ra.setContractArea("Uusimaa");
        ra.setContractAreaCode(1);
        ra.setDistanceFromRoadSectionStart(1000);
        ra.setRoadMaintenanceClass("1");
        ra.setRoadNumber(1);
        ra.setRoadSection(1);
        return ra;
    }

    protected static Integer getRandomId(final int min, final int max) {
        Assert.assertTrue(max > min);
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    protected static List<EsiasentoVO> createEsiasentos(final long kameraId, final int count) {
        final List<EsiasentoVO> eas = new ArrayList<>();
        IntStream.range(0, count).forEach(i -> {
            final EsiasentoVO ea = new EsiasentoVO();
            ea.setId(getRandomLotjuId().longValue());
            ea.setKameraId(kameraId);
            ea.setKeruussa(true);
            ea.setJulkisuus(Julkisuus.JULKINEN);
            ea.setSuunta("0");
            ea.setNimiEsitys(PRESET_PRESENTATION_NAME + ea.getId());
            eas.add(ea);
        });
        return eas;
    }

    protected static KameraVO createKamera(final Instant publicFrom) {
        final KameraVO k = new KameraVO();
        k.setVanhaId(getRandomLotjuId());
        k.setId(Long.valueOf(k.getVanhaId()));
        k.setNimi("Kamera-asema");
        k.setJulkisuus(createKameraJulkisuus(publicFrom, JULKINEN));
        k.setKeruunTila(KeruunTILA.KERUUSSA);
        final TieosoiteVO to = new TieosoiteVO();
        k.setTieosoite(to);

        return k;
    }

    protected static Instant getInstant(int secondsToAdd) {
        return Instant.now().plusSeconds(secondsToAdd).truncatedTo(ChronoUnit.SECONDS);
    }

    protected static JulkisuusVO createKameraJulkisuus(final Instant from, final JulkisuusTaso julkisuusTaso) {
        final JulkisuusVO julkisuus = new JulkisuusVO();
        julkisuus.setJulkisuusTaso(julkisuusTaso);
        julkisuus.setAlkaen(DateHelper.toXMLGregorianCalendarAtUtc(from));
        return julkisuus;
    }

    private static Integer getRandomLotjuId() {
        return getRandomId(MIN_LOTJU_ID, MAX_LOTJU_ID);
    }
}
