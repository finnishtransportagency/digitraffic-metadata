package fi.livi.digitraffic.tie.metadata.service.roadstation;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

public class RoadStationServiceTest extends AbstractTest {
    @Autowired
    private RoadStationService roadStationService;

    @Test
    public void testFindAllTmsStations() {
        final List<RoadStation> tms = roadStationService.findByType(RoadStationType.TMS_STATION);

        assertCollectionSize(545, tms);
    }

    @Test
    public void testFindAllCameras() {
        final List<RoadStation> cameras = roadStationService.findByType(RoadStationType.CAMERA_STATION);

        assertCollectionSize(866, cameras);
    }

    @Test
    public void testFindAllWeatherStations() {
        final List<RoadStation> weatherStations = roadStationService.findByType(RoadStationType.WEATHER_STATION);

        assertCollectionSize(880, weatherStations);
    }
}
