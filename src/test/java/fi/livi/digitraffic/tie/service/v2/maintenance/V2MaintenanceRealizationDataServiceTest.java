package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_X;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_X_AROUND_TASK;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_X_OUTSIDE_TASK;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_Y;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_Y_AROUND_TASK;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_Y_OUTSIDE_TASK;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.REALIZATIONS_8_TASKS_2_END_TIME;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.SINGLE_REALISATIONS_3_TASKS_END_TIME;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.TASK_IDS_INSIDE_BOX;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.JsonNode;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationFeatureCollection;

@Import({ V2MaintenanceRealizationDataService.class, V2MaintenanceRealizationUpdateService.class,
          JacksonAutoConfiguration.class, V2MaintenanceRealizationServiceTestHelper.class  })
public class V2MaintenanceRealizationDataServiceTest extends AbstractServiceTest {

    @Autowired
    private V2MaintenanceRealizationUpdateService maintenanceRealizationUpdateService;

    @Autowired
    private V2MaintenanceRealizationDataService maintenanceRealizationDataService;

    @Autowired
    private V2MaintenanceRealizationServiceTestHelper testHelper;


    @Before
    public void initData() throws IOException {
        testHelper.clearDb();
        testHelper.initializeSingleRealisations3Tasks();
        testHelper.initialize8Realisations2Tasks();
        maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        testHelper.flushAndClearSession();
    }

    @Test
    public void findMaintenanceRealizationsWithinTime1() {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            SINGLE_REALISATIONS_3_TASKS_END_TIME.minus(1, ChronoUnit.HOURS), SINGLE_REALISATIONS_3_TASKS_END_TIME,
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight(), Collections.emptyList());
        Assert.assertEquals(3, result.getFeatures().size());
    }

    @Test
    public void findMaintenanceRealizationsWithinTime2() {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            REALIZATIONS_8_TASKS_2_END_TIME.minus(1, ChronoUnit.HOURS), REALIZATIONS_8_TASKS_2_END_TIME,
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight(), Collections.emptyList());
        Assert.assertEquals(8, result.getFeatures().size());
    }

    @Test
    public void findMaintenanceRealizationsWithinTimeBoth() {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            SINGLE_REALISATIONS_3_TASKS_END_TIME.minus(1, ChronoUnit.HOURS), REALIZATIONS_8_TASKS_2_END_TIME,
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight(), Collections.emptyList());
        Assert.assertEquals(11, result.getFeatures().size());
    }

    @Test
    public void findMaintenanceRealizationsNotWithinTime() {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            SINGLE_REALISATIONS_3_TASKS_END_TIME.plusMillis(1), SINGLE_REALISATIONS_3_TASKS_END_TIME.plusSeconds(1),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight(), Collections.emptyList());
        Assert.assertEquals(0, result.getFeatures().size());
    }

    @Test
    public void findMaintenanceRealizationsOnePointWithinBoundingBox() {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            SINGLE_REALISATIONS_3_TASKS_END_TIME.minus(1, ChronoUnit.HOURS), SINGLE_REALISATIONS_3_TASKS_END_TIME,
            RANGE_X_AROUND_TASK.getLeft(), RANGE_Y_AROUND_TASK.getLeft(), RANGE_X_AROUND_TASK.getRight(), RANGE_Y_AROUND_TASK.getRight(),
            Collections.emptyList());
        Assert.assertEquals(1, result.getFeatures().size());
        final Set<Long> taskIds = new HashSet<>(result.getFeatures().get(0).getProperties().tasks);
        Assert.assertEquals(TASK_IDS_INSIDE_BOX, taskIds);
    }

    @Test
    public void findMaintenanceRealizationsOutsideBoundingBox() {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            SINGLE_REALISATIONS_3_TASKS_END_TIME.minus(1, ChronoUnit.HOURS), SINGLE_REALISATIONS_3_TASKS_END_TIME.plusSeconds(1),
            RANGE_X_OUTSIDE_TASK.getLeft(), RANGE_Y_OUTSIDE_TASK.getLeft(), RANGE_X_OUTSIDE_TASK.getRight(), RANGE_Y_OUTSIDE_TASK.getRight(),
            Collections.emptyList());
        Assert.assertEquals(0, result.getFeatures().size());
    }

    @Test
    public void findRealizationDataJsonByRealizationId() {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            SINGLE_REALISATIONS_3_TASKS_END_TIME.minus(1, ChronoUnit.HOURS), SINGLE_REALISATIONS_3_TASKS_END_TIME.plusSeconds(1),
            RANGE_X_AROUND_TASK.getLeft(), RANGE_Y_AROUND_TASK.getLeft(), RANGE_X_AROUND_TASK.getRight(), RANGE_Y_AROUND_TASK.getRight(),
            Collections.emptyList());

        final JsonNode json = maintenanceRealizationDataService.findRealizationDataJsonByRealizationId(result.getFeatures().get(0).getProperties().id);
        Assert.assertNotNull(json);
    }
}
