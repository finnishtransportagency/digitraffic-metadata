package fi.livi.digitraffic.tie.service.v1.forecastsection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ForecastSectionNaturalIdHelperTest {

    private final String naturalId = "00003_207_010_8";

    @Test
    public void getRoadNumberSucceeds() {
        assertEquals(3, ForecastSectionNaturalIdHelper.getRoadNumber(naturalId));
    }

    @Test
    public void getRoadSectionNumberSucceeds() {
        assertEquals(207, ForecastSectionNaturalIdHelper.getRoadSectionNumber(naturalId));
    }

    @Test
    public void getRoadSectionVersionNumberSucceeds() {
        assertEquals(10, ForecastSectionNaturalIdHelper.getRoadSectionVersionNumber(naturalId));
    }
}