package fi.livi.digitraffic.tie.metadata.geojson.converter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fi.livi.digitraffic.tie.metadata.geojson.Point;

public class CoordinateConverterTest {

    @Test
    public void convertFromETRSToWGS84Succeeds() {

        assertCoordinates(CoordinateConverter.convertFromETRS89ToWGS84(new Point(386261, 6686131)));
    }

    @Test
    public void convertFromKKJ3ToWGS84Succeeds() {

        assertCoordinates(CoordinateConverter.convertFromKKJ3ToWGS84(new Point(3386386, 6688942)));
    }

    // Reference system https://www.retkikartta.fi/
    private void assertCoordinates(final Point point) {
        assertEquals(24.9422, point.getLongitude(), 0.0001);
        assertEquals(60.2958, point.getLatitude(), 0.0001);
    }
}
