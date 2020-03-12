package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.beta.BetaController.MAINTENANCE_REALIZATIONS_PATH;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_X;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.RANGE_Y;
import static fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper.TASKS;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Locale;

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
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationServiceTestHelper;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationUpdateService;

@Import(value = { V2MaintenanceRealizationServiceTestHelper.class })
public class MaintenanceRealizationsControllerTest extends AbstractRestWebTest {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceRealizationsControllerTest.class);

    @Autowired
    private V2MaintenanceRealizationServiceTestHelper testHelper;

    @Autowired
    private V2MaintenanceRealizationUpdateService maintenanceRealizationUpdateService;

    private ResultActions getJson(final Instant from, final Instant to, final double xMin, final double yMin, final double xMax, final double yMax) throws Exception {
        final String url = API_BETA_BASE_PATH + MAINTENANCE_REALIZATIONS_PATH +
            String.format(Locale.US, "?from=%s&to=%s&xMin=%f&yMin=%f&xMax=%f&yMax=%f", from.toString(), to.toString(), xMin, yMin, xMax, yMax);
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
    public void findMaintenanceRealizationsWithinTime() throws Exception {
        int count1 = getRandomId(0, 10);
        int coun2 = getRandomId(0, 10);
        final ZonedDateTime startNow = getNowWithZeroNanos();
        final ZonedDateTime startInPast = startNow.minusHours(1);
        generateSingleRealisationsWithTasksAndSingleRoute(count1, startNow);
        generateSingleRealisationWithTasksAndMultipleRoutes(coun2, startInPast);

        getJson(
            startNow.toInstant(), startNow.toInstant(),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(count1)));
    }

    @Test
    public void findMaintenanceRealizationsWithinTimeInPast() throws Exception {
        int count1 = getRandomId(0, 10);
        int count2 = getRandomId(0, 10);
        final ZonedDateTime startNow = getNowWithZeroNanos();
        final ZonedDateTime startInPast = startNow.minusHours(1);
        generateSingleRealisationsWithTasksAndSingleRoute(count1, startNow);
        generateSingleRealisationWithTasksAndMultipleRoutes(count2, startInPast);

        final ResultActions result = getJson(
            startInPast.toInstant(), startInPast.toInstant(),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight());
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(count2)));
    }

    @Test
    public void findMaintenanceMultipleRealizationsWithinTime() throws Exception {
        int count1 = getRandomId(0, 10);
        int count2 = getRandomId(0, 10);
        final ZonedDateTime startNow = getNowWithZeroNanos();
        final ZonedDateTime startInPast = startNow.minusHours(1);
        generateSingleRealisationsWithTasksAndSingleRoute(count1, startNow);
        generateSingleRealisationWithTasksAndMultipleRoutes(count2, startInPast);

        final ResultActions result = getJson(
            startInPast.toInstant(), startNow.toInstant(),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight());
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(count1 + count2)));
    }

    @Test
    public void findMaintenanceRealizationsNotWithinTime() throws Exception {
        int count = getRandomId(0, 10);
        final ZonedDateTime startNow = getNowWithZeroNanos();
        generateSingleRealisationsWithTasksAndSingleRoute(count, startNow);

        getJson(
            startNow.plusSeconds(1).toInstant(), startNow.plusHours(24).toInstant(),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(0)));

        getJson(
            startNow.minusHours(24).toInstant(), startNow.minusSeconds(1).toInstant(),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(0)));
    }

    @Test
    public void findMaintenanceRealizationsOnePointWithinBoundingBox() throws Exception {
        final ZonedDateTime startNow = getNowWithZeroNanos();
        generateSingleRealisationsWithTasksAndSingleRoute(1, startNow);

        // generated coordinates are x = 19.0 to 28.99... and y = 59.0 to 68.99...
        final double xMin = 28.0;
        final double xMax = 29.0;
        final double yMin = 68.0;
        final double yMax = 69.0;

        getJson(
            startNow.toInstant(), startNow.toInstant(),
            xMin, yMin, xMax, yMax)
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(1)))
            .andExpect(jsonPath("features[0].properties.tasks[*]", containsInAnyOrder(TASKS[0].getKey(), TASKS[1].getKey())))
            .andExpect(jsonPath("features[0].properties.tasks", hasSize(2)));
    }

    @Test
    public void findMaintenanceRealizationsOutsideBoundingBox() throws Exception {
        final ZonedDateTime startNow = getNowWithZeroNanos();
        generateSingleRealisationsWithTasksAndSingleRoute(1, startNow);

        // generated coordinates are x = 19.0 to 28.99... and y = 59.0 to 68.99...
        final double xMin = 29.0;
        final double xMax = 32;
        final double yMin = 69;
        final double yMax = 72;

        getJson(
            startNow.toInstant(), startNow.toInstant(),
            xMin, yMin, xMax, yMax)
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(0)));
    }

    private void generateSingleRealisationsWithTasksAndSingleRoute(final int countOfDifferentRealizations, final ZonedDateTime startTime)
        throws JsonProcessingException {
        testHelper.generateSingleRealisationsWithTasksAndSingleRoute(countOfDifferentRealizations, startTime);
        maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        testHelper.flushAndClearSession();
    }

    private void generateSingleRealisationWithTasksAndMultipleRoutes(int countOfDifferentRealizations, ZonedDateTime startTime)
        throws JsonProcessingException {
        testHelper.generateSingleRealisationWithTasksAndMultipleRoutes(countOfDifferentRealizations, startTime);
        maintenanceRealizationUpdateService.handleUnhandledRealizations(100);
        testHelper.flushAndClearSession();
    }

    private ZonedDateTime getNowWithZeroNanos() {
        return ZonedDateTime.now().withNano(0);
    }
}
