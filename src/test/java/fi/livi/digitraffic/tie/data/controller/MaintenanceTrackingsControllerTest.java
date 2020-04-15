package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.beta.BetaController.MAINTENANCE_TRACKINGS_PATH;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.ASFALTOINTI;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.PAALLYSTEIDEN_PAIKKAUS;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_X;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_Y;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.createMaintenanceTrackingWithPoints;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.createWorkMachines;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper.getTaskSetWithTasks;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat;
import fi.livi.digitraffic.tie.external.harja.Tyokone;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingServiceTestHelper;

@Import(value = { V2MaintenanceTrackingServiceTestHelper.class })
public class MaintenanceTrackingsControllerTest extends AbstractRestWebTest {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceTrackingsControllerTest.class);

    @Autowired
    private V2MaintenanceTrackingServiceTestHelper testHelper;

    private ResultActions getTrackingsJson(final Instant from, final Instant to, final Set<MaintenanceTrackingTask> tasks, final double xMin, final double yMin, final double xMax, final double yMax) throws Exception {
        final String tasksParams = tasks.stream().map(t -> "&taskId=" + t.toString()).collect(Collectors.joining());
        final String url = API_BETA_BASE_PATH + MAINTENANCE_TRACKINGS_PATH +
            String.format(Locale.US, "?from=%s&to=%s&xMin=%f&yMin=%f&xMax=%f&yMax=%f%s", from.toString(), to.toString(), xMin, yMin, xMax, yMax, tasksParams);
        log.info("Get URL: {}", url);
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(url);
        get.contentType(MediaType.APPLICATION_JSON);
        final ResultActions result = mockMvc.perform(get);
        log.info("Response:\n{}", result.andReturn().getResponse().getContentAsString());
        return result;
    }

    private ResultActions getLatestTrackingsJson(final Instant from, final Instant to, final Set<MaintenanceTrackingTask> tasks, final double xMin, final double yMin, final double xMax, final double yMax) throws Exception {
        final String tasksParams = tasks.stream().map(t -> "&taskId=" + t.toString()).collect(Collectors.joining());
        final String url = API_BETA_BASE_PATH + MAINTENANCE_TRACKINGS_PATH + "/latest";
            String.format(Locale.US, "?from=%s&to=%s&xMin=%f&yMin=%f&xMax=%f&yMax=%f%s", from.toString(), to.toString(), xMin, yMin, xMax, yMax, tasksParams);
        log.info("Get URL: {}", url);
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(url);
        get.contentType(MediaType.APPLICATION_JSON);
        final ResultActions result = mockMvc.perform(get);
        log.info("Response:\n{}", result.andReturn().getResponse().getContentAsString());
        return result;
    }

    @Before
    public void initData() throws IOException {
        testHelper.clearDb();
        testHelper.flushAndClearSession();
    }

    @Test
    public void findMaintenanceTrackingsWithinTime() throws Exception {
        final ZonedDateTime now = DateHelper.getZonedDateTimeNowAtUtcWithoutMillis();
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final List<Tyokone> firstHalfMachines = workMachines.subList(0, machineCount / 2);
        final List<Tyokone> secondHalfMachines = workMachines.subList(machineCount / 2, machineCount);

        testHelper.saveTrackingData( // end time will be start+9 min
            createMaintenanceTrackingWithPoints(now, 10, 1, firstHalfMachines, ASFALTOINTI, PAALLYSTEIDEN_PAIKKAUS));

        testHelper.saveTrackingData( // end time will be start+10+9 min
            createMaintenanceTrackingWithPoints(now.plusMinutes(10), 10, 1, secondHalfMachines, ASFALTOINTI));
        testHelper.handleUnhandledWorkMachineTrackings();

        // First tracking
        getTrackingsJson(
            now.toInstant(), now.plusMinutes(9).toInstant(), new HashSet<>(),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(firstHalfMachines.size())))
            .andExpect(jsonPath("features[*].properties.workMachineId").doesNotExist());

        // Second tracking
        getTrackingsJson(
            now.plusMinutes(10).toInstant(), now.plusMinutes(10+9).toInstant(), new HashSet<>(),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(secondHalfMachines.size())))
            .andExpect(jsonPath("features[*].properties.workMachineId").doesNotExist());

        // Both
        getTrackingsJson(
            now.toInstant(), now.plusMinutes(10+9).toInstant(), new HashSet<>(),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(machineCount)))
            .andExpect(jsonPath("features[*].properties.workMachineId").doesNotExist());;
    }

    @Test
    public void findMaintenanceTrackingsWithTasks() throws Exception {
        final ZonedDateTime now = DateHelper.getZonedDateTimeNowAtUtcWithoutMillis();
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);

        IntStream.range(0, machineCount).forEach(i -> {
            final Tyokone machine = workMachines.get(i);
            try {
                testHelper.saveTrackingData( // end time will be start+9 min
                    createMaintenanceTrackingWithPoints(
                        now, 10, 1, Collections.singletonList(machine),
                        SuoritettavatTehtavat.values()[i], SuoritettavatTehtavat.values()[i+1]));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        testHelper.handleUnhandledWorkMachineTrackings();

        // find with first task should only find the first tracking for machine 1.
        getTrackingsJson(
            now.toInstant(), now.plusMinutes(9).toInstant(), getTaskSetWithTasks(MaintenanceTrackingTask.values()[0]),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(1)))
            .andExpect(jsonPath("features[*].properties.workMachineId").doesNotExist());

        // Search with second task should return trackings for machine 1. and 2.
        getTrackingsJson(
            now.toInstant(), now.plusMinutes(9).toInstant(), getTaskSetWithTasks(MaintenanceTrackingTask.values()[1]),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(2)))
            .andExpect(jsonPath("features[*].properties.workMachineId").doesNotExist());
    }

    @Test
    public void findLatestMaintenanceTrackings() throws Exception {
        final ZonedDateTime now = DateHelper.getZonedDateTimeNowAtUtcWithoutMillis();
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);

        // Generate trackings for 50 minutes changing tasks every 10 minutes
        IntStream.range(0, 5).forEach(i -> {
            try {
                testHelper.saveTrackingData( // end time will be start+9 min
                    createMaintenanceTrackingWithPoints(
                        now.plusMinutes(i * 10), 10, 1, workMachines,
                        SuoritettavatTehtavat.values()[i], SuoritettavatTehtavat.values()[i + 1]));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        testHelper.handleUnhandledWorkMachineTrackings();

        log.info("Machine count {}", machineCount);
        // When getting latest trackings we should get only latest trackings per machine -> result of machineCount
        final ResultActions latestResult = getLatestTrackingsJson(
            now.toInstant(), now.plusMinutes(4 * 10 + 9).toInstant(), getTaskSetWithTasks(MaintenanceTrackingTask.values()[1]),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(machineCount)))
            .andExpect(jsonPath("features[*].properties.workMachineId").doesNotExist());
        IntStream.range(0, machineCount).forEach(i -> {
            try {
                latestResult.andExpect(jsonPath("features[" + i + "].geometry.type", equalTo("Point")));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        // When getting all trackings we should all 5 trackings per machine -> result of machineCount*5
        final ResultActions trackingResult = getTrackingsJson(
            now.toInstant(), now.plusMinutes(4 * 10 + 9).toInstant(), new HashSet<>(),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(machineCount * 5)))
            .andExpect(jsonPath("features[*].properties.workMachineId").doesNotExist());
         IntStream.range(0, machineCount * 5).forEach(i -> {
             try {
                 trackingResult.andExpect(jsonPath("features[" + i + "].geometry.type", equalTo("LineString")));
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         });
    }

    @Test
    public void findWithBoundingBox() throws Exception {
        final ZonedDateTime now = DateHelper.getZonedDateTimeNowAtUtcWithoutMillis();

        final List<Tyokone> workMachines = createWorkMachines(1);
        final TyokoneenseurannanKirjausRequestSchema k =
            createMaintenanceTrackingWithPoints(now, 10, 1, workMachines, ASFALTOINTI);
        testHelper.saveTrackingData(k);
        testHelper.handleUnhandledWorkMachineTrackings();

        final Double maxX = k.getHavainnot().stream().map(h -> h.getHavainto().getSijainti().getKoordinaatit().getX()).max(Double::compareTo).get();
        final Double maxY = k.getHavainnot().stream().map(h -> h.getHavainto().getSijainti().getKoordinaatit().getY()).max(Double::compareTo).get();

        Point pointWGS84 = CoordinateConverter.convertFromETRS89ToWGS84(new Point(maxX, maxY));

        log.info(pointWGS84.toString());
        // The only tracking should be found when it's inside the bounding box
        getTrackingsJson(
            now.toInstant(), now.plusMinutes(4 * 10 + 9).toInstant(), new HashSet<>(),
            pointWGS84.getLongitude()-0.1, pointWGS84.getLatitude()-0.1, RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(1)))
            .andExpect(jsonPath("features[*].properties.workMachineId").doesNotExist());
        // The only tracking should not be found when it's not inside the bounding box
        getTrackingsJson(
            now.toInstant(), now.plusMinutes(4 * 10 + 9).toInstant(), new HashSet<>(),
            pointWGS84.getLongitude()+0.1, pointWGS84.getLatitude()+0.1, RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(0)));
    }
}