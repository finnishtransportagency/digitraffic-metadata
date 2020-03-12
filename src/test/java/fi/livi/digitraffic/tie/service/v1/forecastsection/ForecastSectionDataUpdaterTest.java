package fi.livi.digitraffic.tie.service.v1.forecastsection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.junit.Assert;
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
import fi.livi.digitraffic.tie.dto.v1.forecast.ForecastSectionWeatherRootDto;
import fi.livi.digitraffic.tie.service.v1.ForecastSectionDataService;
import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dao.v2.V2ForecastSectionMetadataDao;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.forecastsection.RoadCondition;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v2.forecastsection.V2ForecastSectionMetadataUpdater;

public class ForecastSectionDataUpdaterTest extends AbstractDaemonTestWithoutS3 {

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private ForecastSectionClient forecastSectionClient;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private ForecastSectionDataUpdater forecastSectionDataUpdater;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private ForecastSectionDataService forecastSectionDataService;

    private MockRestServiceServer server;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private V2ForecastSectionMetadataUpdater forecastSectionMetadataUpdater;

    @Autowired
    private V2ForecastSectionMetadataDao v2ForecastSectionMetadataDao;

    @Autowired
    private DataStatusService dataStatusService;

    @Before
    public void before() {
        forecastSectionClient = new ForecastSectionClient(restTemplate);
        forecastSectionDataUpdater = new ForecastSectionDataUpdater(forecastSectionClient, forecastSectionRepository, dataStatusService);
        forecastSectionMetadataUpdater =
            new V2ForecastSectionMetadataUpdater(forecastSectionClient, forecastSectionRepository, v2ForecastSectionMetadataDao, dataStatusService);
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void updateForecastSectionV1DataSucceeds() throws IOException {

        server.expect(requestTo("/nullroadConditionsV1-json.php"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withSuccess(readResourceContent("classpath:forecastsection/roadConditionsV1.json"), MediaType.APPLICATION_JSON));

        final Instant dataUpdated = forecastSectionDataUpdater.updateForecastSectionWeatherData(ForecastSectionApiVersion.V1);

        final Instant lastUpdated = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_WEATHER_DATA).toInstant();

        Assert.assertEquals(dataUpdated, lastUpdated);

        final ForecastSectionWeatherRootDto data = forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V1, false,
                                                                                                            null,
                                                                                                            null, null,
                                                                                                            null, null,
                                                                                                            null);

        assertEquals(277, data.weatherData.size());
        assertEquals("00009_303_000_0", data.weatherData.get(0).naturalId);
        assertEquals(5, data.weatherData.get(0).roadConditions.size());
        assertEquals("0h", data.weatherData.get(0).roadConditions.get(0).getForecastName());
        assertEquals("2h", data.weatherData.get(0).roadConditions.get(1).getForecastName());
        assertEquals("4h", data.weatherData.get(0).roadConditions.get(2).getForecastName());
        assertEquals("6h", data.weatherData.get(0).roadConditions.get(3).getForecastName());
        assertEquals("12h", data.weatherData.get(0).roadConditions.get(4).getForecastName());
    }

    @Test
    public void updateForecastSectionV2DataSucceeds() throws IOException {

        forecastSectionRepository.deleteAllInBatch();

        server.expect(requestTo("/nullroadsV2.php"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withSuccess(readResourceContent("classpath:forecastsection/roadsV2_slim.json"), MediaType.APPLICATION_JSON));

        server.expect(requestTo("/nullroadConditionsV2-json.php"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withSuccess(readResourceContent("classpath:forecastsection/roadConditionsV2.json"), MediaType.APPLICATION_JSON));

        final Instant metadataUpdated = forecastSectionMetadataUpdater.updateForecastSectionsV2Metadata();

        final Instant dataUpdated = forecastSectionDataUpdater.updateForecastSectionWeatherData(ForecastSectionApiVersion.V2);

        final Instant metadataLastUpdated = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_V2_METADATA).toInstant();
        final Instant dataLastUpdated = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_V2_WEATHER_DATA).toInstant();

        assertEquals(metadataUpdated, metadataLastUpdated);
        assertEquals(dataUpdated, dataLastUpdated);

        final ForecastSectionWeatherRootDto data = forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V2, false,
                                                                                                            null,
                                                                                                            null, null, null, null,
                                                                                                            null);

        assertNotNull(data);
        assertEquals("00003_226_00000_0_0", data.weatherData.get(0).naturalId);
        assertEquals(5, data.weatherData.get(0).roadConditions.size());
        assertEquals("0h", data.weatherData.get(0).roadConditions.get(0).getForecastName());
        assertEquals(ZonedDateTime.parse("2018-11-14T14:00+02:00[Europe/Helsinki]").toInstant(), data.weatherData.get(0).roadConditions.get(0).getTime().toInstant());
        assertEquals("+4.2", data.weatherData.get(0).roadConditions.get(0).getTemperature());
        assertEquals("12h", data.weatherData.get(0).roadConditions.get(4).getForecastName());
        assertEquals(RoadCondition.MOIST, data.weatherData.get(0).roadConditions.get(4).getForecastConditionReason().getRoadCondition());

        assertEquals("00009_216_03050_0_0", data.weatherData.get(1).naturalId);
        assertEquals(5, data.weatherData.get(1).roadConditions.size());
        assertEquals("2h", data.weatherData.get(0).roadConditions.get(1).getForecastName());
        assertEquals("+3", data.weatherData.get(0).roadConditions.get(4).getRoadTemperature());
    }
}
