package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;

@RunWith(SpringJUnit4ClassRunner.class)
public class RoadStationSensorServiceTest extends MetadataTest {
    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Test
    public void testFindAllNonObsoleteRoadStationSensors() {
        final List<RoadStationSensor> sensors = roadStationSensorService.findAllNonObsoleteRoadStationSensors();
        Assert.assertEquals(56, sensors.size());
    }
}
