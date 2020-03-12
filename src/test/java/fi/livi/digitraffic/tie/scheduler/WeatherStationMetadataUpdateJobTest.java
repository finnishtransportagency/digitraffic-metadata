package fi.livi.digitraffic.tie.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuTiesaaPerustiedotServiceEndpointMock;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationSensorUpdater;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationService;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationUpdater;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationsSensorsUpdater;

public class WeatherStationMetadataUpdateJobTest extends AbstractDaemonTestWithoutS3 {

    private static final Logger log = LoggerFactory.getLogger(WeatherStationMetadataUpdateJobTest.class);

    @Autowired
    private WeatherStationSensorUpdater weatherStationSensorUpdater;

    @Autowired
    private WeatherStationsSensorsUpdater weatherStationsSensorsUpdater;

    @Autowired
    private WeatherStationUpdater weatherStationUpdater;

    @Autowired
    private WeatherStationService weatherStationService;

    @Autowired
    private LotjuTiesaaPerustiedotServiceEndpointMock lotjuTiesaaPerustiedotServiceMock;

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    private WeatherStationFeatureCollection allInitial;
    private WeatherStationFeatureCollection allAfterChange;
    private Map<Long, RoadStationSensor> sensorsInitial;
    private Map<Long, RoadStationSensor> sensorsAfterChange;

    @Before
    public void initData() {
        lotjuTiesaaPerustiedotServiceMock.initStateAndService();

        // Update road weather stations to initial state (2 non obsolete stations and 2 obsolete)
        weatherStationSensorUpdater.updateRoadStationSensors();
        weatherStationUpdater.updateWeatherStations();
        weatherStationsSensorsUpdater.updateWeatherStationsSensors();
        sensorsInitial = roadStationSensorService.findAllRoadStationSensorsMappedByLotjuId(RoadStationType.WEATHER_STATION);

        allInitial =
            weatherStationService.findAllPublishableWeatherStationAsFeatureCollection(false);
        assertEquals(2, allInitial.getFeatures().size());
        sensorsInitial.values().forEach(s -> entityManager.detach(s));

        // Now change lotju metadata and update tms stations (3 non obsolete stations and 1 bsolete)
        lotjuTiesaaPerustiedotServiceMock.setStateAfterChange(true);
        weatherStationSensorUpdater.updateRoadStationSensors();
        weatherStationUpdater.updateWeatherStations();
        weatherStationsSensorsUpdater.updateWeatherStationsSensors();

        sensorsAfterChange = roadStationSensorService.findAllRoadStationSensorsMappedByLotjuId(RoadStationType.WEATHER_STATION);
        allAfterChange =
            weatherStationService.findAllPublishableWeatherStationAsFeatureCollection(false);
        assertEquals(3, allAfterChange.getFeatures().size());
    }

    @Test
    public void testUpdateWeatherStations() {

        Assert.assertNull(findWithLotjuId(allInitial, 33));
        Assert.assertNotNull(findWithLotjuId(allInitial, 34));
        Assert.assertNull(findWithLotjuId(allInitial, 35));
        Assert.assertNotNull(findWithLotjuId(allInitial, 36));

        Assert.assertNull(findWithLotjuId(allAfterChange, 33));
        Assert.assertNotNull(findWithLotjuId(allAfterChange, 34));
        Assert.assertNotNull(findWithLotjuId(allAfterChange, 35)); // removed temporary -> gathering
        Assert.assertNotNull(findWithLotjuId(allAfterChange, 36));

        final WeatherStationFeature before = findWithLotjuId(allInitial, 34);
        final WeatherStationFeature after = findWithLotjuId(allAfterChange, 34);

        assertEquals(before.getProperties().getName() + "1", after.getProperties().getName());

        assertEquals(after.getProperties().getCollectionStatus(), CollectionStatus.GATHERING);

        assertEquals(before.getProperties().getNames().get("fi"), "Tie 3 Helsinki, Pirkkola");
        assertEquals(before.getProperties().getNames().get("sv"), "Väg 3 Helsingfors, Britas");
        assertEquals(before.getProperties().getNames().get("en"), "Road 3 Helsinki, Pirkkola");

        assertEquals(after.getProperties().getNames().get("fi"), "Tie 3 Helsinki, Kirkkola");
        assertEquals(after.getProperties().getNames().get("sv"), "Väg 3 Helsingfors, Kritas");
        assertEquals(after.getProperties().getNames().get("en"), "Road 3 Helsinki, Kirkkola");

        assertEquals(before.getProperties().getRoadAddress().getDistanceFromRoadSectionStart(), (Integer) 4715);
        assertEquals(after.getProperties().getRoadAddress().getDistanceFromRoadSectionStart(), (Integer) 4716);

        assertEquals(before.getProperties().getLongitudeETRS89(), 384007.0, 0.01);
        assertEquals(after.getProperties().getLongitudeETRS89(), 384008.0, 0.01);

        assertEquals(before.getProperties().getLatitudeETRS89(), 6678738.0, 0.01);
        assertEquals(after.getProperties().getLatitudeETRS89(), 6678739.0, 0.01);

        assertEquals(before.getProperties().getAltitudeETRS89(), 0.0, 0.01);
        assertEquals(after.getProperties().getAltitudeETRS89(), 1.0, 0.01);

        final WeatherStationFeature initial36 = findWithLotjuId(allInitial, 36);
        final WeatherStationFeature after36 = findWithLotjuId(allAfterChange, 36);

        final RoadStationSensor sensorInitial = findSensorWithLotjuId(initial36, 1, true);
        final RoadStationSensor sensorAfter = findSensorWithLotjuId(after36, 1, false);


        assertEquals("EsitysFi", sensorInitial.getPresentationNameFi());
        assertEquals("EsitysFi2", sensorAfter.getPresentationNameFi());

        assertEquals("EsitysSe", sensorInitial.getPresentationNameSv());
        assertEquals("EsitysSe2", sensorAfter.getPresentationNameSv());

        assertEquals("EsitysEn", sensorInitial.getPresentationNameEn());
        assertEquals("EsitysEn2", sensorAfter.getPresentationNameEn());

        assertEquals("Ilman nopeus", sensorInitial.getDescriptionFi());
        assertEquals("Ilman lämpötila", sensorAfter.getDescriptionFi());

        assertEquals("Luft hastighet", sensorInitial.getDescriptionSv());
        assertEquals("Luft temperatur", sensorAfter.getDescriptionSv());

        assertEquals("Air velocity", sensorInitial.getDescriptionEn());
        assertEquals("Air temperature", sensorAfter.getDescriptionEn());

        assertEquals("°CC", sensorInitial.getUnit());
        assertEquals("°C", sensorAfter.getUnit());

        assertEquals(10, sensorInitial.getAccuracy().intValue());
        assertEquals(1, sensorAfter.getAccuracy().intValue());

        final RoadStationSensor sensor2Initial = findSensorWithLotjuId(initial36, 2, true);
        final RoadStationSensor sensor2After = findSensorWithLotjuId(after36, 2, false);

        assertNull(sensor2Initial);
        assertNotNull(sensor2After);

        final RoadStationSensor sensor36Initial = findSensorWithLotjuId(initial36, 3, true);
        final RoadStationSensor sensor36After = findSensorWithLotjuId(after36, 3, false);

        assertNotNull(sensor36Initial);
        assertNull(sensor36After);

        assertEquals(CollectionStatus.GATHERING,
                     findWithLotjuId(allAfterChange, 35).getProperties().getCollectionStatus());
    }

    private WeatherStationFeature findWithLotjuId(final WeatherStationFeatureCollection collection, final long lotjuId) {
        final Optional<WeatherStationFeature> initial =
                collection.getFeatures().stream()
                        .filter(x -> x.getProperties().getLotjuId() == lotjuId)
                        .findFirst();
        return initial.orElse(null);
    }

    private RoadStationSensor findSensorWithLotjuId(final WeatherStationFeature feature, final long lotjuId, final boolean initial) {
        final RoadStationSensor sensor = initial ? sensorsInitial.get(lotjuId) : sensorsAfterChange.get(lotjuId);
        if (sensor != null) {
            final Optional<Long> optional =
                feature.getProperties().getStationSensors().stream()
                    .filter(naturalId -> naturalId == sensor.getNaturalId())
                    .findFirst();
            return optional.isPresent() ? sensor : null;
        }
        return null;
    }
}
