package fi.livi.digitraffic.tie.metadata.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.isIn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.Test;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration;

public class WeatherStationMetadataControllerRestWebTest extends AbstractRestWebTest {


    @Test
    public void testWeatherStationMetadataRestApi() throws Exception {
        mockMvc.perform(get(RoadApplicationConfiguration.API_V1_BASE_PATH +
                            RoadApplicationConfiguration.API_METADATA_PART_PATH +
                            MetadataController.WEATHER_STATIONS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.features[0].id", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].geometry.type", is("Point")))
                .andExpect(jsonPath("$.features[0].geometry.coordinates", Matchers.hasSize(3)))
                .andExpect(jsonPath("$.features[0].properties.weatherStationType", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.roadStationId", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.collectionStatus", isIn(new String[] {"GATHERING", "REMOVED_TEMPORARILY"})))
                .andExpect(jsonPath("$.features[0].properties.municipalityCode", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.municipality", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.provinceCode", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.municipalityCode", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.municipality", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.provinceCode", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.province", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.fi", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.sv", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.en", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.roadAddress.roadSection", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.roadAddress.roadNumber", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.stationSensors[0]", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.purpose").doesNotHaveJsonPath());
    }

    @Test
    public void testWeatherStationSensorsMetadataRestApi() throws Exception {
        mockMvc.perform(get(RoadApplicationConfiguration.API_V1_BASE_PATH +
            RoadApplicationConfiguration.API_METADATA_PART_PATH +
            MetadataController.WEATHER_STATIONS_AVAILABLE_SENSORS_PATH))
            .andExpect(status().isOk())
            .andExpect(content().contentType(CONTENT_TYPE))
            .andExpect(jsonPath("$.roadStationSensors[0].id", isA(Integer.class)))
            .andExpect(jsonPath("$.roadStationSensors[0].name", isA(String.class)))
            .andExpect(jsonPath("$.roadStationSensors[0].description", isA(String.class)))
            .andExpect(jsonPath("$.roadStationSensors[0].descriptionFi", isA(String.class)))
            .andExpect(jsonPath("$.roadStationSensors[0].vehicleClass").doesNotHaveJsonPath())
            .andExpect(jsonPath("$.roadStationSensors[0].lane").doesNotHaveJsonPath())
            .andExpect(jsonPath("$.roadStationSensors[0].direction").doesNotHaveJsonPath());
    }
}
