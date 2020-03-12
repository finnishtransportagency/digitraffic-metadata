package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.FREE_FLOW_SPEEDS_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.AbstractRestWebTest;

public class FreeFlowSpeedRestWebTest extends AbstractRestWebTest {

    @Test
    public void testFreeFlowSpeedDataRestApi() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_DATA_PART_PATH + FREE_FLOW_SPEEDS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].tmsNumber", Matchers.notNullValue()));
    }

    @Test
    public void testFreeFlowSpeedDataRestApiByTmsId() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_DATA_PART_PATH + FREE_FLOW_SPEEDS_PATH + "/tms/23801"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.tmsFreeFlowSpeeds[0].tmsNumber", Matchers.notNullValue()));
    }
}
