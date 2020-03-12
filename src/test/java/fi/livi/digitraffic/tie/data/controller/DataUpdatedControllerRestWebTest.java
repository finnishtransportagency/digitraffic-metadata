package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.util.AssertionErrors;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.controller.v1.DataController;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.DataStatusService;

/**
 * Test that every data-api has working last update query
 */
public class DataUpdatedControllerRestWebTest extends AbstractRestWebTest {
    private static final Logger log = LoggerFactory.getLogger(DataUpdatedControllerRestWebTest.class);

    private static final Set<String> excludedOperations = Sets.newSet(
        "TRAFFIC_DISORDERS_DATEX2_PATH",
        "FORECAST_SECTION_WEATHER_DATA_PATH",
        "WEIGHT_RESTRICTIONS_DATEX2_PATH",
        "ROADWORKS_DATEX2_PATH"
    );

    @Autowired
    private DataController dataController;

    @Autowired
    private DataStatusService dataStatusService;

    @Before
    public void updateDataUpdated() {
        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(RoadStationType.TMS_STATION));
        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(RoadStationType.WEATHER_STATION));
    }

    @Test
    public void testDataUpdatedExists() throws Exception {
       final Field[] fieldArray = FieldUtils.getAllFields(DataController.class);
       final List<Field> fields = Stream.of(fieldArray).filter(this::filter).collect(Collectors.toList());

        for(final Field field : fields) {
            final String url = API_V1_BASE_PATH + API_DATA_PART_PATH +
                         field.get(dataController) +
                         "?" + DataController.LAST_UPDATED_PARAM + "=true";

            log.info("Test url: " + url);
            mockMvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andExpect(mvcResult -> {
                        String contentType = mvcResult.getResponse().getContentType();

                        AssertionErrors.assertTrue("Content type not set", contentType != null);

                        MatcherAssert.assertThat(MediaType.valueOf(contentType), Matchers.anyOf(
                            Matchers.is(MediaType.APPLICATION_JSON),
                            Matchers.is(MediaType.APPLICATION_XML)));

                        if (Matchers.is(MediaType.APPLICATION_JSON).matches(MediaType.valueOf(contentType))) {
                            jsonPath("$.dataUpdatedTime", Matchers.notNullValue()).match(mvcResult);
                        }
                    });
        }

    }

    private boolean filter(final Field f) {
        return f.getName().endsWith("_PATH") && !excludedOperations.contains(f.getName());
    }

}
