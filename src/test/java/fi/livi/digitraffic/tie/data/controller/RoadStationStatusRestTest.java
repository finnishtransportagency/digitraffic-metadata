package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.RestTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;

public class RoadStationStatusRestTest  extends RestTest {
    @Test
    public void testRoadStatusRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH + MetadataApplicationConfiguration.API_DATA_PART_PATH + RoadStationStatusController.PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.timestamp", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.roadStationStatusData", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.roadStationStatusData[0].stationId", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.roadStationStatusData[0].updated", Matchers.notNullValue()))
        ;
    }
}