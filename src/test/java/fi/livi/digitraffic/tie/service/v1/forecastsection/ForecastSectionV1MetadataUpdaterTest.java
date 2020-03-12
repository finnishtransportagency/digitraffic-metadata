package fi.livi.digitraffic.tie.service.v1.forecastsection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import java.io.IOException;
import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeatureCollection;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;

@TestPropertySource(properties = { "logging.level.fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionV1MetadataUpdater=WARN" })
public class ForecastSectionV1MetadataUpdaterTest extends AbstractDaemonTestWithoutS3 {

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private ForecastSectionClient forecastSectionClient;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private ForecastSectionV1MetadataUpdater forecastSectionMetadataUpdater;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private ForecastSectionV1MetadataService forecastSectionService;

    @Autowired
    private DataStatusService dataStatusService;

    private MockRestServiceServer server;

    @Autowired
    private RestTemplate restTemplate;

    @Before
    public void before() {
        forecastSectionClient = new ForecastSectionClient(restTemplate);
        forecastSectionMetadataUpdater = new ForecastSectionV1MetadataUpdater(forecastSectionClient, forecastSectionRepository, dataStatusService);
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void updateForecastSectionV1MetadataSucceeds() throws IOException {

        server.expect(requestTo("/nullroads.php"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(readResourceContent("classpath:forecastsection/roadsV1.json"), MediaType.APPLICATION_JSON));

        forecastSectionMetadataUpdater.updateForecastSectionV1Metadata();

        final Instant lastUpdate = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_METADATA).toInstant();
        final Instant now = Instant.now();
        assertTrue(now.minusSeconds(2).isBefore(lastUpdate));
        assertTrue(now.minusSeconds(2).isBefore(lastUpdate));
        assertTrue(now.plusSeconds(2).isAfter(lastUpdate));

        final ForecastSectionFeatureCollection collection = forecastSectionService.findForecastSectionsV1Metadata();

        assertEquals(277, collection.getFeatures().size());
        assertEquals("00001_001_000_0", collection.getFeatures().get(0).getProperties().getNaturalId());
        assertEquals(10, collection.getFeatures().get(0).getGeometry().getCoordinates().size());
        assertEquals(Double.parseDouble("24.944"), collection.getFeatures().get(0).getGeometry().getCoordinates().get(0).get(0).doubleValue(), 0.01);
        assertEquals(Double.parseDouble("60.167"), collection.getFeatures().get(0).getGeometry().getCoordinates().get(0).get(1).doubleValue(), 0.01);
    }
}
