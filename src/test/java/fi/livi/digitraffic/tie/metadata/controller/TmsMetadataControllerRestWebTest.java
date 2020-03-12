package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_STATIONS_AVAILABLE_SENSORS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_STATIONS_PATH;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.v1.tms.TmsStationRepository;
import fi.livi.digitraffic.tie.model.CalculatorDeviceType;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;
import fi.livi.digitraffic.tie.model.v1.TmsStation;
import fi.livi.digitraffic.tie.service.RoadDistrictService;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;

@Import({ RoadDistrictService.class})
public class TmsMetadataControllerRestWebTest extends AbstractRestWebTest {

    @Autowired
    private TmsStationRepository tmsStationRepository;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    protected RoadDistrictService roadDistrictService;

    @Before
    public void initData() {
        entityManager.createNativeQuery(
            "UPDATE road_station rs " +
                "SET obsolete_date = now() " +
                "WHERE rs.obsolete_date is null " +
                "  AND rs.road_station_type = '" + RoadStationType.TMS_STATION + "'").executeUpdate();
        TmsStation ts = generateDummyTmsStation();
        tmsStationRepository.save(ts);

        List<RoadStationSensor> publishable =
            roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.TMS_STATION);

        Assert.assertFalse(publishable.isEmpty());

        roadStationSensorService.updateSensorsOfWeatherStations(ts.getRoadStationId(),
            RoadStationType.TMS_STATION,
            publishable.stream().map(s -> s.getLotjuId()).collect(Collectors.toList()));
    }


    @Test
    public void testTmsMetadataRestApi() throws Exception {

        mockMvc.perform(get(API_V1_BASE_PATH + API_METADATA_PART_PATH + TMS_STATIONS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.features[0].id", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.features[0].geometry.type", is("Point")))
                .andExpect(jsonPath("$.features[0].geometry.coordinates", Matchers.hasSize(3)))
                .andExpect(jsonPath("$.features[0].properties", Matchers.anything()))
                .andExpect(jsonPath("$.features[0].properties.roadAddress.roadSection", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.roadAddress.roadNumber", isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.roadStationId", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.tmsNumber", Matchers.isA(Integer.class)))
                .andExpect(jsonPath("$.features[0].properties.name", Matchers.notNullValue()))
                .andExpect(jsonPath("$.features[0].properties.name", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.fi", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.sv", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.names.en", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.features[0].properties.purpose", Matchers.isA(String.class)));
    }

    @Test
    public void testTmsStationSensorsMetadataRestApi() throws Exception {
        mockMvc.perform(get(API_V1_BASE_PATH + API_METADATA_PART_PATH + TMS_STATIONS_AVAILABLE_SENSORS_PATH))
            .andExpect(status().isOk())
            .andExpect(content().contentType(CONTENT_TYPE))
            .andExpect(jsonPath("$.roadStationSensors[0].id", isA(Integer.class)))
            .andExpect(jsonPath("$.roadStationSensors[0].name", isA(String.class)))
            .andExpect(jsonPath("$.roadStationSensors[0].description", isA(String.class)))
            .andExpect(jsonPath("$.roadStationSensors[0].descriptions.fi", isA(String.class)))
            .andExpect(jsonPath("$.roadStationSensors[0].vehicleClass").hasJsonPath())
            .andExpect(jsonPath("$.roadStationSensors[0].lane").hasJsonPath())
            .andExpect(jsonPath("$.roadStationSensors[0].direction").hasJsonPath());
    }

    private TmsStation generateDummyTmsStation() {
        final RoadStation rs = generateDummyRoadStation(RoadStationType.TMS_STATION);

        final TmsStation ts = new TmsStation();
        ts.setRoadStation(rs);
        ts.setLotjuId(rs.getLotjuId());
        ts.setNaturalId(rs.getLotjuId());
        ts.setRoadDistrict(roadDistrictService.findByNaturalId(1));
        ts.setCalculatorDeviceType(CalculatorDeviceType.DSL_5);
        ts.setName("st120_Pähkinärinne");
        ts.setDirection1Municipality("Vihti");
        ts.setDirection1MunicipalityCode(927);
        ts.setDirection2Municipality("Helsinki");
        ts.setDirection2MunicipalityCode(91);
        ts.setWinterFreeFlowSpeed1(70);
        ts.setWinterFreeFlowSpeed2(70);
        ts.setSummerFreeFlowSpeed1(80);
        ts.setSummerFreeFlowSpeed2(80);

        return ts;
    }
}
