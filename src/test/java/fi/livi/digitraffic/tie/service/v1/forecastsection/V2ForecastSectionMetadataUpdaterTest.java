package fi.livi.digitraffic.tie.service.v1.forecastsection;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dao.v2.V2ForecastSectionMetadataDao;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Feature;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2FeatureCollection;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v2.forecastsection.V2ForecastSectionMetadataService;
import fi.livi.digitraffic.tie.service.v2.forecastsection.V2ForecastSectionMetadataUpdater;

public class V2ForecastSectionMetadataUpdaterTest extends AbstractDaemonTestWithoutS3 {

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private ForecastSectionClient forecastSectionClient;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private V2ForecastSectionMetadataUpdater forecastSectionMetadataUpdater;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private V2ForecastSectionMetadataService v2ForecastSectionMetadataService;

    @Autowired
    private V2ForecastSectionMetadataDao v2ForecastSectionMetadataDao;

    @Autowired
    private DataStatusService dataStatusService;

    private MockRestServiceServer server;

    @Autowired
    private RestTemplate restTemplate;

    @Before
    public void before() {
        forecastSectionClient = new ForecastSectionClient(restTemplate);
        forecastSectionMetadataUpdater = new V2ForecastSectionMetadataUpdater(forecastSectionClient, forecastSectionRepository,
            v2ForecastSectionMetadataDao,
                                                                              dataStatusService);
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void updateForecastSectionV2MetadataSucceeds() throws IOException {

        server.expect(requestTo("/nullroadsV2.php"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withSuccess(readResourceContent("classpath:forecastsection/roadsV2.json"), MediaType.APPLICATION_JSON));

        final Instant updated = forecastSectionMetadataUpdater.updateForecastSectionsV2Metadata();
        final Instant lastUpdated = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_V2_METADATA).toInstant();

        assertEquals(updated, lastUpdated);

        final ForecastSectionV2FeatureCollection featureCollection =
            v2ForecastSectionMetadataService.getForecastSectionV2Metadata(false, null, null, null, null, null,
                                                                          null);

        final ForecastSectionV2Feature feature = featureCollection.getFeatures().get(0);

        assertEquals(9473, featureCollection.getFeatures().size());

        assertEquals("00001_001_00000_1_0", feature.getProperties().getNaturalId());

        assertEquals(2, feature.getGeometry().getCoordinates().get(0).size());
        assertCoordinates(24.9430081, feature.getGeometry().getCoordinates().get(0).get(0).get(0));
        assertCoordinates(60.1667212, feature.getGeometry().getCoordinates().get(0).get(0).get(1));
        assertCoordinates(24.9418095, feature.getGeometry().getCoordinates().get(0).get(1).get(0));
        assertCoordinates(60.1675145, feature.getGeometry().getCoordinates().get(0).get(1).get(1));
        assertEquals(12, feature.getGeometry().getCoordinates().get(70).size());
        assertCoordinates(24.9336238, feature.getGeometry().getCoordinates().get(70).get(0).get(0));
        assertCoordinates(60.1739127, feature.getGeometry().getCoordinates().get(70).get(0).get(1));
        assertCoordinates(24.9335811, feature.getGeometry().getCoordinates().get(70).get(1).get(0));
        assertCoordinates(60.1739668, feature.getGeometry().getCoordinates().get(70).get(1).get(1));

        assertEquals(2, feature.getProperties().getRoadSegments().size());
        assertEquals(0, feature.getProperties().getRoadSegments().get(0).getStartDistance().intValue());
        assertEquals(3264, feature.getProperties().getRoadSegments().get(1).getEndDistance().intValue());

        assertEquals(52, feature.getProperties().getLinkIdList().size());
        assertEquals(441054L, feature.getProperties().getLinkIdList().get(0).longValue());
        assertEquals(452523L, feature.getProperties().getLinkIdList().get(51).longValue());
    }

    @Test
    public void findForecastSectionsByRoadNumberSucceeds() throws IOException {

        server.expect(requestTo("/nullroadsV2.php"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withSuccess(readResourceContent("classpath:forecastsection/roadsV2_slim.json"), MediaType.APPLICATION_JSON));

        forecastSectionMetadataUpdater.updateForecastSectionsV2Metadata();

        final ForecastSectionV2FeatureCollection featureCollection = v2ForecastSectionMetadataService.getForecastSectionV2Metadata(false, 3,
                                                                                                                                   null, null,
                                                                                                                                   null, null,
                                                                                                                                   null);

        assertEquals(2, featureCollection.getFeatures().size());

        final ForecastSectionV2Feature feature1 = featureCollection.getFeatures().get(0);
        final ForecastSectionV2Feature feature2 = featureCollection.getFeatures().get(1);

        assertEquals("00003_218_04302_0_0", feature1.getProperties().getNaturalId());
        assertEquals("Vaasantie 3.218", feature1.getProperties().getDescription());
        assertEquals(1, feature1.getProperties().getRoadSegments().size());
        assertEquals(16, feature1.getProperties().getLinkIdList().size());
        assertEquals(Geometry.Type.MultiLineString, feature1.getGeometry().getType());
        assertEquals(16, feature1.getGeometry().getCoordinates().size());
        assertEquals(9, feature1.getGeometry().getCoordinates().get(0).size());
        assertCoordinates(22.9983705, feature1.getGeometry().getCoordinates().get(0).get(0).get(0));
        assertCoordinates(62.1215860, feature1.getGeometry().getCoordinates().get(0).get(0).get(1));
        assertCoordinates(22.9800960, feature1.getGeometry().getCoordinates().get(14).get(2).get(0));
        assertCoordinates(62.1412124, feature1.getGeometry().getCoordinates().get(14).get(2).get(1));

        assertEquals("00003_226_00000_0_0", feature2.getProperties().getNaturalId());
        assertEquals("Tampereentie 3.226", feature2.getProperties().getDescription());
    }

    @Test
    public void findForecastSectionsByNaturalIdSucceeds() throws IOException {

        server.expect(requestTo("/nullroadsV2.php"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withSuccess(readResourceContent("classpath:forecastsection/roadsV2_slim.json"), MediaType.APPLICATION_JSON));

        forecastSectionMetadataUpdater.updateForecastSectionsV2Metadata();

        final ForecastSectionV2FeatureCollection featureCollection = v2ForecastSectionMetadataService.getForecastSectionV2Metadata(false, null,
                                                                                                                                   null, null,
                                                                                                                                   null, null,
                                                                                                                                   Arrays.asList("00009_216_03050_0_0"));

        assertEquals(1, featureCollection.getFeatures().size());
        assertEquals("00009_216_03050_0_0", featureCollection.getFeatures().get(0).getProperties().getNaturalId());
    }

    private void assertCoordinates(final double expected, final double actual) {
        assertEquals(expected, actual, 0.00000001);
    }

}