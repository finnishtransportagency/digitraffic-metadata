package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.WEATHER_STATIONS_AVAILABLE_SENSORS_PATH;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration;

public class RoadStationSensorMetadataControllerRestWebTest extends AbstractRestWebTest {

    @Test
    public void testRoadStationSensorMetadataApi() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_METADATA_PART_PATH + WEATHER_STATIONS_AVAILABLE_SENSORS_PATH))
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(CONTENT_TYPE)) //
                .andExpect(jsonPath("$", notNullValue())) //
                .andExpect(jsonPath("$.roadStationSensors[0].id", isA(Integer.class))) //
                .andExpect(jsonPath("$.roadStationSensors[0].nameOld", isA(String.class))) //
                .andExpect(jsonPath("$.roadStationSensors[0].unit", isA(String.class))) //
        ;
    }
}
