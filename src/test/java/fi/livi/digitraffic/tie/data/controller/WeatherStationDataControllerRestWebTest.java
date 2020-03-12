package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.WEATHER_DATA_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.v1.SensorValueRepository;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.DataStatusService;

public class WeatherStationDataControllerRestWebTest extends AbstractRestWebTest {

    @Autowired
    private DataStatusService dataStatusService;

    @Autowired
    private SensorValueRepository sensorValueRepository;

    @Before
    public void updateData() {
        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(RoadStationType.WEATHER_STATION));
        sensorValueRepository.findAll().stream()
            .filter(sv -> sv.getRoadStation().getType().equals(RoadStationType.WEATHER_STATION))
            .forEach(sv -> sv.setSensorValueMeasured(ZonedDateTime.now()));
    }

    @Test
    public void testWeatherDataRestApi() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_DATA_PART_PATH + WEATHER_DATA_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0]", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].measuredTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].oldName", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].id", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].sensorValue", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].sensorUnit", Matchers.isA(String.class)))
        ;
    }

    @Test
    public void testWeatherDataRestApiById() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_DATA_PART_PATH + WEATHER_DATA_PATH + "/1034"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0]", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].measuredTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].oldName", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].id", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].sensorValue", Matchers.notNullValue()))
                .andExpect(jsonPath("$.weatherStations[0].sensorValues[0].sensorUnit", Matchers.isA(String.class)))
        ;
    }
}