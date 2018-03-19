package fi.livi.digitraffic.tie.metadata.dao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;

public class RoadStationSensorRepositoryTest extends AbstractTest {

    @Autowired
    private RoadStationSensorRepository roadStationSensorRepository;

    @Test
    public void notFound() {
        final RoadStationSensor result = roadStationSensorRepository.findByRoadStationTypeAndLotjuId(RoadStationType.WEATHER_STATION, -18L);

        assertNull(result);
    }

    @Test
    public void foundWithSensors() {
        final RoadStationSensor result = roadStationSensorRepository.findByRoadStationTypeAndLotjuId(RoadStationType.WEATHER_STATION, 22L);

        assertNotNull(result);
        assertCollectionSize(7, result.getSensorValueDescriptions());
    }

    @Test
    public void foundWithoutSensorDescriptions() {
        final RoadStationSensor result = roadStationSensorRepository.findByRoadStationTypeAndLotjuId(RoadStationType.WEATHER_STATION, 18L);

        assertNotNull(result);
        assertEmpty(result.getSensorValueDescriptions());
    }
}
