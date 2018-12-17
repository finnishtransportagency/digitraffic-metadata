package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.dto.v1.ForecastSectionCoordinatesDto;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.dto.v2.ForecastSectionV2Dto;

public class ForecastSectionClientTest extends AbstractTest {

    @Autowired
    private ForecastSectionClient forecastSectionClient;

    @Test
    public void getForecastSectionV1MetadataSucceeds() {

        List<ForecastSectionCoordinatesDto> forecastSectionCoordinates = forecastSectionClient.getForecastSectionV1Metadata();

        assertTrue(forecastSectionCoordinates.size() > 2);
        assertEquals("00001_001_000_0", forecastSectionCoordinates.get(0).getNaturalId());
        assertEquals("Vt 1: Helsinki - Kehä III", forecastSectionCoordinates.get(0).getName());
        assertEquals(10, forecastSectionCoordinates.get(0).getCoordinates().size());
        assertEquals(BigDecimal.valueOf(24.944), forecastSectionCoordinates.get(0).getCoordinates().get(0).longitude);
        assertEquals(BigDecimal.valueOf(60.167), forecastSectionCoordinates.get(0).getCoordinates().get(0).latitude);
    }

    @Test
    public void getForecastSectionV2MetadataSucceeds() {

        final ForecastSectionV2Dto forecastSectionV2Metadata = forecastSectionClient.getForecastSectionV2Metadata();

        assertNotNull(forecastSectionV2Metadata);
    }

    @Test
    public void getRoadConditionsSucceeds() {

        ForecastSectionDataDto roadConditions = forecastSectionClient.getRoadConditions();

        assertTrue(roadConditions.forecastSectionWeatherList.size() > 250);
        assertNotNull(roadConditions.forecastSectionWeatherList.get(0));
        assertNotNull(roadConditions.forecastSectionWeatherList.get(0).forecast);
        assertNotNull(roadConditions.forecastSectionWeatherList.get(0).forecast.get(0));
        assertNotNull(roadConditions.forecastSectionWeatherList.get(0).forecast.get(0).time);
        assertNotNull(roadConditions.forecastSectionWeatherList.get(0).forecast.get(0).weatherSymbol);
    }
}