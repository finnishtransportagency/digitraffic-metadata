package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;

public class RoadStationSensorServiceTest extends AbstractServiceTest {
    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Test
    public void findAllNonObsoleteAndAllowedRoadStationSensorsForWeatherStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.WEATHER_STATION);

        assertCollectionSize(69, sensors);
    }

    @Test
    public void findAllNonObsoleteAndAllowedRoadStationSensorsForTmsStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllPublishableRoadStationSensors(RoadStationType.TMS_STATION);

        assertCollectionSize(26, sensors);
    }

    @Test
    public void findAllRoadStationSensorsForWeatherStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllRoadStationSensors(RoadStationType.WEATHER_STATION);

        assertCollectionSize(139, sensors);
    }

    @Test
    public void findAllRoadStationSensorsForTmsStation() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllRoadStationSensors(RoadStationType.TMS_STATION);

        assertCollectionSize(231, sensors);
    }
}
