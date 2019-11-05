package fi.livi.digitraffic.tie.metadata.service.roadstation;

import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

public class RoadStationServiceTest extends AbstractServiceTest {
    @Autowired
    private RoadStationService roadStationService;

    @Test
    public void findAllTmsStations() {
        final List<RoadStation> tms = roadStationService.findByType(RoadStationType.TMS_STATION);

        assertCollectionSize(545, tms);
    }

    @Test
    public void findAllCameras() {
        final List<RoadStation> cameras = roadStationService.findByType(RoadStationType.CAMERA_STATION);

        Assert.assertTrue(String.format("Collection size was expected to be %s, was %d", "> 800", cameras.size()),
                 cameras.size() > 800);
    }

    @Test
    public void findAllWeatherStations() {
        final List<RoadStation> weatherStations = roadStationService.findByType(RoadStationType.WEATHER_STATION);

        assertCollectionSize(880, weatherStations);
    }
}
