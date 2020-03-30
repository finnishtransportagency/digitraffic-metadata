package fi.livi.digitraffic.tie.service.v2.maintenance;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceRealizationDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceRealizationRepository;
import fi.livi.digitraffic.tie.external.harja.ReittitoteumanKirjausRequestSchema;
import fi.livi.digitraffic.tie.external.harja.entities.KoordinaattisijaintiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.Lahettaja;
import fi.livi.digitraffic.tie.external.harja.entities.MaaraSchema;
import fi.livi.digitraffic.tie.external.harja.entities.OrganisaatioSchema;
import fi.livi.digitraffic.tie.external.harja.entities.OtsikkoSchema;
import fi.livi.digitraffic.tie.external.harja.entities.ReittiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.Reittipiste;
import fi.livi.digitraffic.tie.external.harja.entities.ReittitoteumaSchema;
import fi.livi.digitraffic.tie.external.harja.entities.ReittitoteumatSchema;
import fi.livi.digitraffic.tie.external.harja.entities.Suorittaja;
import fi.livi.digitraffic.tie.external.harja.entities.TehtavaSchema;
import fi.livi.digitraffic.tie.external.harja.entities.TehtavatSchema;
import fi.livi.digitraffic.tie.external.harja.entities.ToteumaSchema;
import fi.livi.digitraffic.tie.external.harja.entities.TunnisteSchema;
import fi.livi.digitraffic.tie.external.harja.entities.TyokoneSchema;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealization;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTask;

@Service
public class V2MaintenanceRealizationServiceTestHelper {

    private final V2MaintenanceRealizationUpdateService maintenanceRealizationUpdateService;
    private final V2MaintenanceRealizationRepository realizationRepository;
    private final V2MaintenanceRealizationDataRepository realizationDataRepository;
    private final ObjectReader reader;
    private final ObjectWriter writer;
    private final EntityManager entityManager;
    private final ResourceLoader resourceLoader;

    public final static String SINGLE_REALISATIONS_3_TASKS_PATH =
        "classpath:harja/controller/toteumakirjaus-yksi-reittitoteuma-3-tehtavaa.json";
    public final static String REALIZATIONS_8_TASKS_2_PATH =
        "classpath:harja/controller/toteumakirjaus-8-reittitoteumaa-2-tehtavaa.json";
    public final static String SINGLE_REALISATIONS_3_TASKS_WITH_TRANSIT_AND_POINT_PATH =
        "classpath:harja/controller/toteumakirjaus-yksi-reittitoteuma-3-tehtavaa-siirtymalla-ja-yhdella-pisteella.json";

    public final static Pair<Double, Double> RANGE_X = Pair.of(19.0, 32.0);
    public final static Pair<Double, Double> RANGE_Y = Pair.of(59.0, 72.0);

    // select 'Pair.of(' ||  id || ', "' || fi || '"),' from maintenance_task;
    public static final Pair<Integer, String>[] TASKS = new Pair[] {
        Pair.of(1357, "Harjaus"),
        Pair.of(6883, "Harjaus ja roskien poisto"),
        Pair.of(1438, "Huonokuntoisten viittojen ja opastetaulujen uusiminen"),
        Pair.of(1437, "Hylättyjen ajoneuvojen siirto"),
        Pair.of(1436, "Katupölynsidonta"),
        Pair.of(1435, "Kolmannen osapuolen vahinkojen korjaukset"),
        Pair.of(6956, "Kolmansien osapuolien vahinkojen korjaukset"),
        Pair.of(1356, "Koneellinen niitto"),
        Pair.of(1355, "Koneellinen vesakonraivaus"),
        Pair.of(6893, "Liikennemerkkien, opasteiden ja liikenteenohjauslaitteiden hoito sekä reunapaalujen kp"),
        Pair.of(5677, "Liikenneympäristön hoito - Ei yksilöity")
    };

    public final static Instant SINGLE_REALISATIONS_3_TASKS_END_TIME = ZonedDateTime.parse("2020-01-13T10:48:47Z").toInstant();
    public final static Instant REALIZATIONS_8_TASKS_2_END_TIME = ZonedDateTime.parse("2020-01-13T12:06:55Z").toInstant();

    /*  SINGLE_REALISATIONS_3_TASKS should have following points for realization with task 12911L, 1368L
           WGS84                 ETRS-TM35FIN
        1. x=25.87174 y=64.26403 P: 7126921 m I: 445338 m - Pukkila
        2. x=25.95947 y=64.17967 P: 7117449 m I: 449434 m - Piippola
        3. x=26.33006 y=64.10373 P: 7108745 m I: 467354 m - Pyhäntä

        Bounding box min x=26.3 y=64.1 max x=27.0 y=65.0 should contain Pyhäntä point
        Bounding box min x=26.34 y=64.1 max x=27.0 y=65.0 should not contain any points */
    public final static Pair<Double, Double> RANGE_X_AROUND_TASK = Pair.of(26.3, 27.0);
    public final static Pair<Double, Double> RANGE_Y_AROUND_TASK = Pair.of(64.1, 65.0);
    public final static Set<Long> TASK_IDS_INSIDE_BOX = new HashSet<>(Arrays.asList(12911L, 1368L));
    public final static Set<Integer> TASK_IDS_INSIDE_BOX_INTS = new HashSet<>(Arrays.asList(12911, 1368));
    public final static Pair<Double, Double> RANGE_X_OUTSIDE_TASK = Pair.of(26.34, 27.0);
    public final static Pair<Double, Double> RANGE_Y_OUTSIDE_TASK = Pair.of(64.1, 65.0);


    @Autowired
    public V2MaintenanceRealizationServiceTestHelper(final ObjectMapper objectMapper,
                                                     final V2MaintenanceRealizationUpdateService maintenanceRealizationUpdateService,
                                                     final V2MaintenanceRealizationRepository realizationRepository,
                                                     final V2MaintenanceRealizationDataRepository realizationDataRepository,
                                                     final EntityManager entityManager,
                                                     final ResourceLoader resourceLoader) {

        this.maintenanceRealizationUpdateService = maintenanceRealizationUpdateService;
        this.realizationRepository = realizationRepository;
        this.realizationDataRepository = realizationDataRepository;
        reader = objectMapper.readerFor(ReittitoteumanKirjausRequestSchema.class);
        writer = objectMapper.writerFor(ReittitoteumanKirjausRequestSchema.class);
        this.entityManager = entityManager;
        this.resourceLoader = resourceLoader;
    }

    public void clearDb() {
        realizationRepository.deleteAllInBatch();
        realizationDataRepository.deleteAllInBatch();
    }

    public void checkCoordinateCount(final MaintenanceRealization realization, final int count) {
        Assert.assertEquals(count, realization.getLineString().getCoordinates().length);
    }

    public void checkContainsOnlyTasksWithIds(final MaintenanceRealization realization, final long... taskids) {
        final Set<Long> actualIds = realization.getTasks().stream().map(MaintenanceTask::getId).collect(Collectors.toSet());
        final Set<Long> expectedIds = Arrays.stream(taskids).boxed().collect(Collectors.toSet());
        Assert.assertEquals(expectedIds, actualIds);
    }

    private void saveRealizationAsPlainText(final String realizationJson) {
        final MaintenanceRealizationData realization = new MaintenanceRealizationData(123L, realizationJson);
        realizationDataRepository.save(realization);
    }

    public void initializeSingleRealisations3Tasks() throws IOException {
        final String jsonSingleRealisations3Tasks =
            readResourceContent(SINGLE_REALISATIONS_3_TASKS_PATH);
        saveRealizationAsJson(jsonSingleRealisations3Tasks);
    }

    public void initialize8Realisations2Tasks() throws IOException {
        final String jsonSingleRealisations3Tasks =
            readResourceContent(REALIZATIONS_8_TASKS_2_PATH);
        saveRealizationAsJson(jsonSingleRealisations3Tasks);
    }

    public void initializeForInternalTesting(final String fileName) throws IOException {
        final String jsonSingleRealisations3Tasks =
            readResourceContent("classpath:harja/internal-testing/" + fileName);
        saveRealizationAsJson(jsonSingleRealisations3Tasks);
    }

    public void initializeSingleRealisations3TasksWithIllegalJson() throws IOException {
        final String jsonSingleRealisations3Tasks =
            readResourceContent(SINGLE_REALISATIONS_3_TASKS_PATH);
        saveRealizationAsPlainText("[" + jsonSingleRealisations3Tasks);
    }

    public void initializeSingleRealisations3TasksWithTransitAndPoint() throws IOException {
        final String jsonSingleRealisationWith3TasksTransitAndPoint =
            readResourceContent(SINGLE_REALISATIONS_3_TASKS_WITH_TRANSIT_AND_POINT_PATH);
        saveRealizationAsPlainText(jsonSingleRealisationWith3TasksTransitAndPoint);
    }

    /**
     * Generate single realizations with data in single realization.
     * (Data in JSON reittitoteuma-property not in reittitoteumat)
     *
     * @param countOfDifferentRealizations how many realizations with different tasks
     * @param startTime
     * @throws IOException
     */
    public void generateSingleRealisationsWithTasksAndSingleRoute(final int countOfDifferentRealizations, final ZonedDateTime startTime)
        throws JsonProcessingException {
        final ReittitoteumanKirjausRequestSchema toteuma = createReittitoteumanKirjaus(countOfDifferentRealizations, startTime);
        maintenanceRealizationUpdateService.saveRealizationData(123L, toteuma);
    }

    /**
     * Generate single realization with data in multiple realizations.
     * (Data in JSON reittitoteumat-property not in reittitoteuma)
     *
     * @param countOfDifferentRealizations how many realizations with different tasks
     * @param endTime
     * @throws JsonProcessingException
     */
    public void generateSingleRealisationWithTasksAndMultipleRoutes(final int countOfDifferentRealizations, final ZonedDateTime endTime)
        throws JsonProcessingException {
        final ReittitoteumanKirjausRequestSchema toteuma = createReittitoteumatKirjaus(countOfDifferentRealizations, endTime);
        maintenanceRealizationUpdateService.saveRealizationData(123L, toteuma);
    }

    private ReittitoteumanKirjausRequestSchema createReittitoteumanKirjaus(final int countOfDifferentRealizations, final ZonedDateTime startTime) {
        return new ReittitoteumanKirjausRequestSchema(
            createOtsikko(startTime),
            createReittitoteuma(countOfDifferentRealizations, startTime), null);

    }

    private ReittitoteumanKirjausRequestSchema createReittitoteumatKirjaus(final int countOfDifferentRealizations, final ZonedDateTime endTime) {
        ReittitoteumaSchema reittitoteuma = createReittitoteuma(countOfDifferentRealizations, endTime);
        final List<ReittiSchema> reitti = reittitoteuma.getReitti();

        List<ReittitoteumatSchema> reittitoteumat =
            IntStream.range(0, countOfDifferentRealizations*2)
                .filter(n -> n % 2 == 0)
                .mapToObj(i -> {

                    ReittiSchema reitti1 = reitti.get(i);
                    ReittiSchema reitti2 = reitti.get(i+1);

                return new ReittitoteumatSchema(
                    new ReittitoteumaSchema(
                        createToteuma(countOfDifferentRealizations, endTime),
                        Arrays.asList(reitti1, reitti2),
                        reittitoteuma.getTyokone()));
            }).collect(Collectors.toList());

        return new ReittitoteumanKirjausRequestSchema(
            createOtsikko(endTime),
            null, reittitoteumat);


    }

    private ReittitoteumaSchema createReittitoteuma(final int countOfDifferentRealizations, final ZonedDateTime endTime) {
        return new ReittitoteumaSchema(
            createToteuma(countOfDifferentRealizations, endTime), createReitti(countOfDifferentRealizations, endTime), createTyokone());
    }

    private TyokoneSchema createTyokone() {
        return new TyokoneSchema(TyokoneSchema.Tyyppi.KUORMA_AUTO, "KA-123", "Scania");
    }

    private List<ReittiSchema> createReitti(final int countOfDifferentRealizations, final ZonedDateTime startTime) {
        return IntStream.range(0, countOfDifferentRealizations)
            .mapToObj(i -> createReittiWithTaskIdx(i, startTime))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<ReittiSchema> createReittiWithTaskIdx(final int idx, final ZonedDateTime startTime) {
        return IntStream.range(0, 100)
            .map(i -> 99 - i) // reverse order
            .mapToObj(i -> {
            // x = 19.0 to 28.99... and y = 59.0 to 68.99...
            final double xLongitude = 19.0 + idx + (i + 1) * 0.1;
            final double yLatitude = 59.0 + idx + (i + 1) * 0.1;
            final double z = 0.0;
            // Source data has koordinates in ETRS89 format
            final Point etrs89 = CoordinateConverter.convertFromWGS84ToETRS89(new Point(xLongitude, yLatitude, z));
            final KoordinaattisijaintiSchema koordinaattisjainti =
                new KoordinaattisijaintiSchema(etrs89.getLongitude(), etrs89.getLatitude(), etrs89.getAltitude());

            return new ReittiSchema(
                new Reittipiste(
                    startTime.minusSeconds(i * 10),
                    koordinaattisjainti,
                    Arrays.asList(createTehtava(idx), createTehtava(idx + 1)),
                    Collections.emptyList(), Collections.emptyList(), null));
        })
        .collect(Collectors.toList());
    }

    public static ZonedDateTime getGeneratedNewestEndTimeWithEndTime(final ZonedDateTime end) {
        return end.minusSeconds(99 * 10);
    }

    public static final String COMPANY = "Tie huolto Oy";
    public static final String COMPANY_ID = "8561566-0";
    private ToteumaSchema createToteuma(final int countOfDifferentRealizations, final ZonedDateTime endTime) {
        return new ToteumaSchema(
            new TunnisteSchema(321), 1, getGeneratedNewestEndTimeWithEndTime(endTime), endTime,
            new Suorittaja(COMPANY, COMPANY_ID), ToteumaSchema.Toteumatyyppi.KOKONAISHINTAINEN,
            createTehtavat(countOfDifferentRealizations), Collections.emptyList());
    }

    private List<TehtavatSchema> createTehtavat(final int countOfDifferentTasks) {
        return IntStream.range(0, countOfDifferentTasks)
            .mapToObj(i -> createTehtava(i)).collect(Collectors.toList());
    }

    private TehtavatSchema createTehtava(final int taskIdx) {
        return new TehtavatSchema(new TehtavaSchema(TASKS[taskIdx].getKey(), new MaaraSchema("km", 100.0), TASKS[taskIdx].getValue()));
    }

    private OtsikkoSchema createOtsikko(final ZonedDateTime startTime) {
        return new OtsikkoSchema(
            new Lahettaja("Tievoima",
            new OrganisaatioSchema("Tie huolto Oy", "8561566-0")),
            new TunnisteSchema(123),
            startTime);
    }

    public String getFormatedRealizationJson(final String realizationJsonPath) throws IOException {
        return writer.writeValueAsString(reader.readValue(readResourceContent(realizationJsonPath)));
    }

    public void checkValidJson(final String json) {
        // Test reading as object and then back to json
        try {
            writer.writeValueAsString(reader.readValue(json));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }

    }

    private void saveRealizationAsJson(final String realisationJSon) throws JsonProcessingException {
        final ReittitoteumanKirjausRequestSchema realization = reader.readValue(realisationJSon);
        maintenanceRealizationUpdateService.saveRealizationData(123L, realization);
    }

    public void flushAndClearSession() {
        entityManager.flush();
        entityManager.clear();
    }

    private String readResourceContent(final String resourcePattern) throws IOException {
        return FileUtils.readFileToString(resourceLoader.getResource(resourcePattern).getFile(), UTF_8);
    }
}
