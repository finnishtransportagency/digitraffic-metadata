package fi.livi.digitraffic.tie.controller.integrations;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_MAINTENANCE_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.integrations.V1RoadMaintenanceController.WORK_MACHINE_TRACKING_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.v1.workmachine.WorkMachineObservationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.v1.maintenance.WorkMachineObservation;
import fi.livi.digitraffic.tie.model.v1.maintenance.WorkMachineObservationCoordinate;
import fi.livi.digitraffic.tie.model.v1.maintenance.WorkMachineTask;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.WorkMachineTracking;
import fi.livi.digitraffic.tie.service.v1.MaintenanceDataService;
import fi.livi.digitraffic.tie.service.v1.WorkMachineObservationService;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class V1RoadMaintenanceControllerTest extends AbstractRestWebTest {
    private static final Logger log = LoggerFactory.getLogger(V1RoadMaintenanceControllerTest.class);

    @Autowired
    private MaintenanceDataService maintenanceDataService;

    @Autowired
    private WorkMachineObservationService workMachineObservationService;

    @Autowired
    private WorkMachineObservationRepository workMachineObservationRepository;

    @Before
    public void cleanDb() {
        workMachineObservationRepository.deleteAll();
        if (TestTransaction.isActive()) {
            TestTransaction.flagForCommit();
            TestTransaction.end();
        }
        TestTransaction.start();
    }

    @Test
    public void postWorkMachineTrackingDataOk() throws Exception {

        final int recordsBefore = maintenanceDataService.findAll().size();

        postTrackingJson("pisteseuranta.json");

        final int recordsAfter = maintenanceDataService.findAll().size();
        Assert.assertEquals(recordsBefore+1, recordsAfter);
    }

    @Test
    public void postWorkMachineTrackingLineStringDataOk() throws Exception {

        final int recordsBefore = maintenanceDataService.findAll().size();

        postTrackingJson("viivageometriaseuranta.json");

        final List<WorkMachineTracking> all = maintenanceDataService.findAll();
        Assert.assertEquals(recordsBefore+1, all.size());
    }


    @Test
    public void postWorkMachineTrackingDataWithNoContentType() throws Exception {

        final int recordsBefore = maintenanceDataService.findAll().size();

        postTracking("pisteseuranta.json", null, status().is5xxServerError());

        final int recordsAfter = maintenanceDataService.findAll().size();
        Assert.assertEquals(recordsBefore, recordsAfter);
    }

    // TODO: could this be divided to multiple smaller tests?
    @Test
    public void postWorkMachineTrackingDataAndHandleAsDistinctObservations() throws Exception {
        final long harjaUrakkaId = 999999;
        final long harjaTyokoneId = 1111111111;
        postTrackingJson("linestring_tracking_1.json");
        postTrackingJson("linestring_tracking_2.json");
        postTrackingJson("linestring_tracking_3.json");

        maintenanceDataService.updateWorkMachineTrackingTypes();
        maintenanceDataService.handleUnhandledWorkMachineTrackings(100);
        entityManager.flush();
        entityManager.clear();

        List<WorkMachineObservation> observations =
            workMachineObservationService.findWorkMachineObservationsByWorkMachineHarjaIdAndHarjaUrakkaId(harjaTyokoneId, harjaUrakkaId);

        Assert.assertEquals("Observations should be divided in two as there is over 30 min gap in observations",
                   2, observations.size());

        final WorkMachineObservation first = observations.get(0);
        final WorkMachineObservation second = observations.get(1);

        Assert.assertEquals("First observation should have coordinates from 2 first messages = 10",
            10, first.getCoordinates().size());

        Assert.assertEquals("First observation should have coordinates from 2 first messages = 10",
            5, second.getCoordinates().size());

        int counter = 0;
        for (WorkMachineObservationCoordinate c : first.getCoordinates()) {
            counter++;
            if (counter < 6) { // first 5 coordinates
                Assert.assertEquals(2, c.getWorkMachineTasks().size());
                c.getWorkMachineTasks().forEach(t -> Arrays.asList(WorkMachineTask.Task.PLOUGHING_AND_SLUSH_REMOVAL, WorkMachineTask.Task.SALTING).contains(t.getTask()));
            }
            else { // last 5 coordinates
                Assert.assertEquals(1, c.getWorkMachineTasks().size());
                c.getWorkMachineTasks().stream().findFirst().get().getTask().equals(WorkMachineTask.Task.PLOUGHING_AND_SLUSH_REMOVAL);
            }
        }

        for (WorkMachineObservationCoordinate c : second.getCoordinates()) {
            Assert.assertEquals(1, c.getWorkMachineTasks().size());
            c.getWorkMachineTasks().stream().findFirst().get().getTask().equals(WorkMachineTask.Task.SALTING);
        }
    }

    private void postTrackingJson(final String fileName) throws Exception {
        postTracking(fileName, MediaType.APPLICATION_JSON, status().isOk());
    }

    private void postTracking(final String fileName, final MediaType mediaType, final ResultMatcher expectResult) throws Exception {
        final String jsonContent = readResourceContent("classpath:harja/controller/" + fileName);
        log.info("POST: {}", API_V1_BASE_PATH + API_MAINTENANCE_PART_PATH + WORK_MACHINE_TRACKING_PATH);
        final MockHttpServletRequestBuilder post = post(API_V1_BASE_PATH + API_MAINTENANCE_PART_PATH + WORK_MACHINE_TRACKING_PATH)
            .content(jsonContent);
        if (mediaType != null) {
            post.contentType(mediaType);
        }

        mockMvc.perform(post).andExpect(expectResult);
    }

    @Ignore("Manual testing")
    @Test
    public void reconvertKoordinatesToETRS89() {
        List<List<Double>> list1 = Arrays.asList(
            Arrays.asList(23.043001740682634, 62.077040093972144),
            Arrays.asList(23.04298031157012, 62.077057456787955),
            Arrays.asList(23.042977977405062, 62.07707536693498),
            Arrays.asList(23.04300057360766, 62.077049049045925),
            Arrays.asList(23.042936286174413, 62.077101137481954));

        List<List<Double>> list2 = Arrays.asList(
            Arrays.asList(23.04283614273957, 62.07713422104009),
            Arrays.asList(23.042776523433194, 62.07715048911074),
            Arrays.asList(23.0424745049959, 62.07711486578356),
            Arrays.asList(23.04269897620993, 62.07715725471351),
            Arrays.asList(23.04262376333742, 62.07714611013338));

        List<List<Double>> list3 = Arrays.asList(
            Arrays.asList(23.04241955454114, 62.077095313427435),
            Arrays.asList(23.042364604157456, 62.07707576104993),
            Arrays.asList(23.042331083335966, 62.07703884594243),
            Arrays.asList(23.04225545265861, 62.07688387267941),
            Arrays.asList(23.04226087078776, 62.07669526886046)
        );

        System.out.println("1:");
        System.out.println(CoordinateConverter.convertLineStringFromWGS84ToETRS89(list1));
        System.out.println("2:");
        System.out.println(CoordinateConverter.convertLineStringFromWGS84ToETRS89(list2));
        System.out.println("3:");
        System.out.println(CoordinateConverter.convertLineStringFromWGS84ToETRS89(list3));
    }

}
