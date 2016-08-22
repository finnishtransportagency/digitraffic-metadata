package fi.livi.digitraffic.tie.metadata.controller;

import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;

import fi.livi.digitraffic.tie.MetadataRestTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;

public class RoadStationSensorMetadataControllerRestTest extends MetadataRestTest {

    @Test
    public void testRoadStationSensorMetadataApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_METADATA_PART_PATH +
                            MetadataController.WEATHER_STATIONS_AVAILABLE_SENSORS_PATH))
                .andExpect(status().isOk()) //
                .andExpect(content().contentType(CONTENT_TYPE)) //
                .andExpect(jsonPath("$", notNullValue())) //
                .andExpect(jsonPath("$.roadStationSensors[0].id", isA(Integer.class))) //
                .andExpect(jsonPath("$.roadStationSensors[0].nameOld", isA(String.class))) //
                .andExpect(jsonPath("$.roadStationSensors[0].unit", isA(String.class))) //
        ;
    }
}
