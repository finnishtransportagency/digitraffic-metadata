package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData.Status.ERROR;
import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData.Status.HANDLED;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceRealizationDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceRealizationRepository;
import fi.livi.digitraffic.tie.external.harja.ReittitoteumanKirjausRequestSchema;
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
    public final static String MULTIPLE_REALISATIONS_2_TASKS_PATH =
        "classpath:harja/controller/toteumakirjaus-monta-reittitoteumaa-3-tehtavaa.json";
    public final static String SINGLE_REALISATIONS_3_TASKS_WITH_TRANSIT_AND_POINT_PATH =
        "classpath:harja/controller/toteumakirjaus-yksi-reittitoteuma-3-tehtavaa-siirtymalla-ja-yhdella-pisteella.json";

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

    @Test
    public void saveNewWorkMachineRealizationSingleRealizationEqualsOriginal() throws IOException {
        initializeSingleRealisations3Tasks();

        final String formattedRealisationJSon = getFormatedRealizationJson(SINGLE_REALISATIONS_3_TASKS_PATH);
        final List<MaintenanceRealizationData> data = realizationDataRepository.findAll();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals(formattedRealisationJSon, data.get(0).getJson());
    }

    @Test
    public void saveNewWorkMachineRealizationMultipleRealization() throws IOException {
        initializeMultipleRealisations2Tasks();

        final String formattedRealisationJSon = getFormatedRealizationJson(MULTIPLE_REALISATIONS_2_TASKS_PATH);
        final List<MaintenanceRealizationData> data = realizationDataRepository.findAll();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals(formattedRealisationJSon, data.get(0).getJson());
    }

    @Test
    public void handleUnhandledWorkMachineRealizations() throws IOException {
        initializeMultipleRealisations2Tasks();
        initializeSingleRealisations3TasksWithIllegalJson();
        initializeMultipleRealisations2Tasks();

        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(2, count);

        final List<MaintenanceRealizationData> data = realizationDataRepository.findAll(Sort.by("id"));
        Assert.assertEquals(3, data.size());
        Assert.assertEquals(HANDLED, data.get(0).getStatus());
        Assert.assertEquals(ERROR, data.get(1).getStatus());
        Assert.assertEquals(HANDLED, data.get(2).getStatus());
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsResultsWithSingleRealization() throws IOException {
        // 1. Realization: 3 points - Tasks: 12911, 1368
        // 2. Realization: 4 points - Tasks: 1368
        // 3. Realization: 2 points - Tasks: 12911
        initializeSingleRealisations3Tasks();

        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(1, count);
        flushAndClearSession();

        // Check the handled data
        final List<MaintenanceRealization> all = realizationRepository.findAll(Sort.by("id"));
        Assert.assertEquals(3, all.size());
        final MaintenanceRealization first = all.get(0);
        final MaintenanceRealization second = all.get(1);
        final MaintenanceRealization third = all.get(2);

        checkContainsOnlyTasksWithIds(first, 12911, 1368);
        checkContainsOnlyTasksWithIds(second, 1368);
        checkContainsOnlyTasksWithIds(third, 12911);

        checkCoordinateCount(first, 3);
        checkCoordinateCount(second, 4);
        checkCoordinateCount(third, 2);
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsResultsWithMultipleRealization() throws IOException {
        // 1. Realization: 4 points - Tasks: 2864
        // 2. Realization: 12 points - Tasks: 1370
        initializeMultipleRealisations2Tasks();

        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(1, count);
        flushAndClearSession();

        // Check the handled data
        final List<MaintenanceRealization> all = realizationRepository.findAll(Sort.by("id"));
        Assert.assertEquals(2, all.size());
        final MaintenanceRealization first = all.get(0);
        final MaintenanceRealization second = all.get(1);

        checkContainsOnlyTasksWithIds(first, 2864);
        checkContainsOnlyTasksWithIds(second, 1370);

        checkCoordinateCount(first, 4);
        checkCoordinateCount(second, 12);
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsResultsWithTransitAndSinglePoint() throws IOException {
        // 1. Realization: 2 points - Tasks: 12911, 1368
        // 2. Realization: 3 points - Tasks: 1368
        // 3. Realization: 1points - Tasks: 12911 -> should not be saved
        initializeSingleRealisations3TasksWithTransitAndPoint();

        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(1, count);
        flushAndClearSession();

        // Check the handled data
        final List<MaintenanceRealization> all = realizationRepository.findAll(Sort.by("id"));
        Assert.assertEquals(2, all.size());
        final MaintenanceRealization first = all.get(0);
        final MaintenanceRealization second = all.get(1);

        checkContainsOnlyTasksWithIds(first, 12911, 1368);
        checkContainsOnlyTasksWithIds(second, 1368);

        checkCoordinateCount(first, 2);
        checkCoordinateCount(second, 3);
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsWithError() throws IOException {
        initializeSingleRealisations3TasksWithIllegalJson();

        // Double check we have right data in db
        Assert.assertEquals(1, realizationDataRepository.findUnhandled(100).count());

        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(0, count);
        final List<MaintenanceRealizationData> all = realizationDataRepository.findAll();
        Assert.assertEquals(1, all.size());
        Assert.assertEquals(ERROR, all.get(0).getStatus());
    }



    public void checkCoordinateCount(final MaintenanceRealization realization, final int count) {
        Assert.assertEquals(count, realization.getLineString().getCoordinates().length);
        Assert.assertEquals(count, realization.getRealizationPoints().size());
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

    public void initializeMultipleRealisations2Tasks() throws IOException {
        final String jsonSingleRealisations3Tasks =
            readResourceContent(MULTIPLE_REALISATIONS_2_TASKS_PATH);
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

    public String getFormatedRealizationJson(final String realizationJsonPath) throws IOException {
        return writer.writeValueAsString(reader.readValue(readResourceContent(realizationJsonPath)));
    }

    private void saveRealizationAsJson(final String realisationJSon) throws JsonProcessingException {
        final ReittitoteumanKirjausRequestSchema realization = reader.readValue(realisationJSon);
        maintenanceRealizationUpdateService.saveNewWorkMachineRealization(123L, realization);
    }

    public void flushAndClearSession() {
        entityManager.flush();
        entityManager.clear();
    }

    private String readResourceContent(final String resourcePattern) throws IOException {
        return FileUtils.readFileToString(resourceLoader.getResource(resourcePattern).getFile(), UTF_8);
    }
}
