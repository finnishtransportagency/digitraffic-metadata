package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.REALIZATIONS_8_TASKS_2_SENDING_TIME;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_X;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_X_AROUND_TASK;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_X_OUTSIDE_TASK;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_Y;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_Y_AROUND_TASK;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_Y_OUTSIDE_TASK;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.SINGLE_REALISATIONS_3_TASKS_SENDING_TIME;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.TASK_IDS_INSIDE_BOX;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

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
    public void findMaintenanceRealizationsWithinTime1() throws IOException {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            SINGLE_REALISATIONS_3_TASKS_SENDING_TIME, SINGLE_REALISATIONS_3_TASKS_SENDING_TIME,
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight(), Collections.emptyList());
        Assert.assertEquals(3, result.features.size());
    }

    @Test
    public void findMaintenanceRealizationsWithinTime2() throws IOException {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            REALIZATIONS_8_TASKS_2_SENDING_TIME, REALIZATIONS_8_TASKS_2_SENDING_TIME,
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight(), Collections.emptyList());
        Assert.assertEquals(8, result.features.size());
    }

    @Test
    public void findMaintenanceRealizationsWithinTimeBoth() throws IOException {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            REALIZATIONS_8_TASKS_2_SENDING_TIME, SINGLE_REALISATIONS_3_TASKS_SENDING_TIME,
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight(), Collections.emptyList());
        Assert.assertEquals(11, result.features.size());
    }

    @Test
    public void findMaintenanceRealizationsNotWithinTime() throws IOException {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            SINGLE_REALISATIONS_3_TASKS_SENDING_TIME.plusMillis(1), SINGLE_REALISATIONS_3_TASKS_SENDING_TIME.plusSeconds(1),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight(), Collections.emptyList());
        Assert.assertEquals(0, result.features.size());
    }

    @Test
    public void findMaintenanceRealizationsOnePointWithinBoundingBox() throws IOException {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            SINGLE_REALISATIONS_3_TASKS_SENDING_TIME, SINGLE_REALISATIONS_3_TASKS_SENDING_TIME.plusSeconds(1),
            RANGE_X_AROUND_TASK.getLeft(), RANGE_Y_AROUND_TASK.getLeft(), RANGE_X_AROUND_TASK.getRight(), RANGE_Y_AROUND_TASK.getRight(),
            Collections.emptyList());
        Assert.assertEquals(1, result.features.size());
        final Set<Long> taskIds = result.features.get(0).getProperties().tasks.stream().collect(Collectors.toSet());
        Assert.assertEquals(TASK_IDS_INSIDE_BOX, taskIds);
    }

    @Test
    public void findMaintenanceRealizationsOutsideBoundingBox() throws IOException {
        final MaintenanceRealizationFeatureCollection result = maintenanceRealizationDataService.findMaintenanceRealizations(
            SINGLE_REALISATIONS_3_TASKS_SENDING_TIME, SINGLE_REALISATIONS_3_TASKS_SENDING_TIME.plusSeconds(1),
            RANGE_X_OUTSIDE_TASK.getLeft(), RANGE_Y_OUTSIDE_TASK.getLeft(), RANGE_X_OUTSIDE_TASK.getRight(), RANGE_Y_OUTSIDE_TASK.getRight(),
            Collections.emptyList());
        Assert.assertEquals(0, result.features.size());
    }
}
