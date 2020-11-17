package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.CAMERA_STATIONS_PATH;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraPresetDto;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.model.v1.camera.CameraType;
import fi.livi.digitraffic.tie.service.RoadStationUpdateService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetService;

@Import(RoadStationUpdateService.class)
public class CameraMetadataControllerRestWebTest extends AbstractRestWebTest {

    @Autowired
    private CameraPresetService cameraPresetService;

    @Before
    public void initData() {
        // Obsolete all existing stations
        entityManager.createNativeQuery(
            "UPDATE road_station rs " +
                     "SET obsolete_date = now() " +
                     "WHERE rs.obsolete_date is null " +
                     "  AND rs.road_station_type = '" + RoadStationType.CAMERA_STATION + "'").executeUpdate();

        CameraPreset cp = generateDummyPreset();

        cameraPresetService.save(cp);
        // Persist to db and clear context to force saved data re-read from db
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void testCameraPresetMetadataRestApi() throws Exception {

        final ArrayList<String> cameraTypes = new ArrayList<>();
        for (final CameraType cameraType : CameraType.values()) {
            cameraTypes.add(cameraType.name());
        }

        final ArrayList<String> directions = new ArrayList<>();
        for (final CameraPresetDto.Direction direction : CameraPresetDto.Direction.values()) {
            directions.add(direction.name());
        }

        mockMvc.perform(get(API_V1_BASE_PATH + API_METADATA_PART_PATH + CAMERA_STATIONS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.features[0].id", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].geometry.type", is("Point")))
                .andExpect(jsonPath("$.features[0].geometry.coordinates", Matchers.hasSize(3)))
//                .andExpect(jsonPath("$.features[0].properties.cameraType", Matchers.instanceOf(String.class)))
                .andExpect(jsonPath("$.features[0].properties.roadStationId", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.id", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.id", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.features[0].properties.cameraType", is(in(cameraTypes))))
                .andExpect(jsonPath("$.features[0].properties.collectionStatus", is(in(new String[] {"GATHERING", "REMOVED_TEMPORARILY"}))))
                .andExpect(jsonPath("$.features[0].properties.municipalityCode", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.municipality", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.provinceCode", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.province", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.fi", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.sv", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.en", isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.roadAddress.roadSection", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.roadAddress.roadNumber", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.roadAddress.distanceFromRoadSectionStart", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.presets[0].presetId", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.features[0].properties.presets[0].cameraId", Matchers.startsWith("C")))
                .andExpect(jsonPath("$.features[0].properties.presets[0].inCollection", Matchers.isA(Boolean.class)))
                .andExpect(jsonPath("$.features[0].properties.presets[0].resolution", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.presets[0].directionCode", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.presets[0].direction", is(in(directions))))
                .andExpect(jsonPath("$.features[0].properties.purpose", Matchers.isA(String.class)))
                .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_FORMAT_RESULT_MATCHER);

    }
}
