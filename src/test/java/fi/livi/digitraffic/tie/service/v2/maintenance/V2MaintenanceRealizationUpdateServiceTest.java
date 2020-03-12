package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData.Status.ERROR;
import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData.Status.HANDLED;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.REALIZATIONS_8_TASKS_2_PATH;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.SINGLE_REALISATIONS_3_TASKS_PATH;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceRealizationDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceRealizationRepository;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealization;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData;

@Import({ V2MaintenanceRealizationUpdateService.class, JacksonAutoConfiguration.class, V2MaintenanceRealizationServiceTestHelper.class })
public class V2MaintenanceRealizationUpdateServiceTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceRealizationUpdateServiceTest.class);
    @Autowired
    private V2MaintenanceRealizationUpdateService maintenanceRealizationUpdateService;

    @Autowired
    private V2MaintenanceRealizationRepository realizationRepository;

    @Autowired
    private V2MaintenanceRealizationDataRepository realizationDataRepository;

    @Autowired
    private V2MaintenanceRealizationServiceTestHelper testHelper;

    @Before
    public void init() {
        testHelper.clearDb();
        realizationRepository.deleteAllInBatch();
        realizationDataRepository.deleteAllInBatch();
    }

    @Test
    public void saveNewWorkMachineRealizationSingleRealizationEqualsOriginal() throws IOException {
        testHelper.initializeSingleRealisations3Tasks();
        final String formattedRealisationJSon = testHelper.getFormatedRealizationJson(SINGLE_REALISATIONS_3_TASKS_PATH);
        final List<MaintenanceRealizationData> data = realizationDataRepository.findAll();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals(formattedRealisationJSon, data.get(0).getJson());
    }

    @Test
    public void saveNewWorkMachineRealizationMultipleRealization() throws IOException {
        testHelper.initialize8Realisations2Tasks();

        final String formattedRealisationJSon = testHelper.getFormatedRealizationJson(REALIZATIONS_8_TASKS_2_PATH);
        final List<MaintenanceRealizationData> data = realizationDataRepository.findAll();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals(formattedRealisationJSon, data.get(0).getJson());
    }

    @Test
    public void handleUnhandledWorkMachineRealizations() throws IOException {
        testHelper.initialize8Realisations2Tasks();
        testHelper.initializeSingleRealisations3TasksWithIllegalJson();
        testHelper.initialize8Realisations2Tasks();

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
        testHelper.initializeSingleRealisations3Tasks();

        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(1, count);
        testHelper.flushAndClearSession();

        // Check the handled data
        final List<MaintenanceRealization> all = realizationRepository.findAll(Sort.by("id"));
        Assert.assertEquals(3, all.size());
        final MaintenanceRealization first = all.get(0);
        final MaintenanceRealization second = all.get(1);
        final MaintenanceRealization third = all.get(2);

        testHelper.checkContainsOnlyTasksWithIds(first, 12911, 1368);
        testHelper.checkContainsOnlyTasksWithIds(second, 1368);
        testHelper.checkContainsOnlyTasksWithIds(third, 12911);

        testHelper.checkCoordinateCount(first, 3);
        testHelper.checkCoordinateCount(second, 4);
        testHelper.checkCoordinateCount(third, 2);
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsResultsWithMultipleRealization() throws IOException {
        // 1. Realization: 4 points - Tasks: 2864
        // 2. Realization: 12 points - Tasks: 1370
        testHelper.initialize8Realisations2Tasks();

        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(1, count);
        testHelper.flushAndClearSession();

        // Check the handled data
        final List<MaintenanceRealization> all = realizationRepository.findAll(Sort.by("id"));
        Assert.assertEquals(8, all.size());
        IntStream.range(0,2).forEach(i ->
        {
            final MaintenanceRealization r = all.get(i);
            testHelper.checkContainsOnlyTasksWithIds(r, 2864);
            testHelper.checkCoordinateCount(r, 2);
        });
        IntStream.range(2,8).forEach(i -> {
            final MaintenanceRealization r = all.get(i);
            testHelper.checkContainsOnlyTasksWithIds(r, 1370);
            testHelper.checkCoordinateCount(r, 2);
        });
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsResultsWithTransitAndSinglePoint() throws IOException {
        // 1. Realization: 2 points - Tasks: 12911, 1368
        // 2. Realization: 3 points - Tasks: 1368
        // 3. Realization: 1points - Tasks: 12911 -> should not be saved
        testHelper.initializeSingleRealisations3TasksWithTransitAndPoint();

        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(1, count);
        testHelper.flushAndClearSession();

        // Check the handled data
        final List<MaintenanceRealization> all = realizationRepository.findAll(Sort.by("id"));
        Assert.assertEquals(2, all.size());
        final MaintenanceRealization first = all.get(0);
        final MaintenanceRealization second = all.get(1);

        testHelper.checkContainsOnlyTasksWithIds(first, 12911, 1368);
        testHelper.checkContainsOnlyTasksWithIds(second, 1368);

        testHelper.checkCoordinateCount(first, 2);
        testHelper.checkCoordinateCount(second, 3);
    }

    @Test
    public void handleUnhandledWorkMachineRealizationsWithError() throws IOException {
        testHelper.initializeSingleRealisations3TasksWithIllegalJson();

        // Double check we have right data in db
        Assert.assertEquals(1, realizationDataRepository.findUnhandled(100).count());

        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        Assert.assertEquals(0, count);
        final List<MaintenanceRealizationData> all = realizationDataRepository.findAll();
        Assert.assertEquals(1, all.size());
        Assert.assertEquals(ERROR, all.get(0).getStatus());
    }

    @Ignore("Just for internal testing")
    @Rollback(false)
    @Test
    public void saveNewWorkMachineRealizationWithStrangeMultipleRealization() throws IOException {
        testHelper.initializeForInternalTesting("virheellisiä-linestringeja.json");
        final long count = maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        log.info("Imported {} realizations", count);
    }
}
