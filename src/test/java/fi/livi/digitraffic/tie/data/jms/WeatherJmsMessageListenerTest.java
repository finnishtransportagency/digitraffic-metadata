package fi.livi.digitraffic.tie.data.jms;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;
import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.data.service.SensorDataUpdateService;
import fi.livi.digitraffic.tie.lotju.xsd.tiesaa.Tiesaa;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationService;

@Transactional
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WeatherJmsMessageListenerTest extends MetadataIntegrationTest {
    
    private static final Logger log = LoggerFactory.getLogger(WeatherJmsMessageListenerTest.class);

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    private WeatherStationService weatherStationService;

    @Autowired
    private SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    LockingService lockingService;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Before
    public void setUpTestData() {
        // Generate test-data: WeatherStations with sensors
        Map<Long, WeatherStation> lamsWithLotjuId = weatherStationService.findAllWeatherStationsMappedByLotjuId();
        Set<Long> usedLotjuIds = new HashSet<>(lamsWithLotjuId.keySet());

        long stationGeneratedLotjuId = -1;
        ArrayList<WeatherStation> stations = new ArrayList<>();
        Map<Long, WeatherStation> weatherStations = weatherStationService.findAllWeatherStationsMappedByByRoadStationNaturalId();

        for (Map.Entry<Long, WeatherStation> longLamStationEntry : weatherStations.entrySet()) {
            WeatherStation lam = longLamStationEntry.getValue();
            if (lam.getLotjuId() == null) {
                while (usedLotjuIds.contains(stationGeneratedLotjuId)) {
                    stationGeneratedLotjuId--;
                }
                usedLotjuIds.add(stationGeneratedLotjuId);
                lam.setLotjuId(stationGeneratedLotjuId);
                lam.getRoadStation().setLotjuId(stationGeneratedLotjuId);
            }
            stations.add(lam);
        }

        log.info("Found " + stations.size() + " Weather Stations");

        Assert.assertTrue(stations.size() > 100);

        long sensorGeneratedLotjuId = -1;
        List<RoadStationSensor> availableSensors =
                roadStationSensorService.findAllNonObsoleteRoadStationSensors(RoadStationType.WEATHER_STATION);

        log.info("Found " + availableSensors.size() + " available sensors for Weather Stations");

        for (RoadStationSensor availableSensor : availableSensors) {
            if ( availableSensor.getLotjuId() == null ) {
                availableSensor.setLotjuId(sensorGeneratedLotjuId);
                sensorGeneratedLotjuId--;
            }
        }

        String merge =
                "MERGE INTO ROAD_STATION_SENSORS TGT\n" +
                "USING (\n" +
                "  SELECT RS.ID ROAD_STATION_ID, S.ID ROAD_STATION_SENSOR_ID\n" +
                "  FROM ROAD_STATION_SENSOR S, ROAD_STATION RS\n" +
                "  WHERE S.OBSOLETE = 0\n" +
                "    AND S.ROAD_STATION_TYPE = 'WEATHER_STATION'\n" +
                "    AND EXISTS (\n" +
                "      SELECT NULL\n" +
                "      FROM ALLOWED_ROAD_STATION_SENSOR ALLOWED\n" +
                "      WHERE ALLOWED.NATURAL_ID = S.NATURAL_ID\n" +
                "        AND ALLOWED.ROAD_STATION_TYPE = S.ROAD_STATION_TYPE\n" +
                "   )\n" +
                "   AND RS.ROAD_STATION_TYPE = 'WEATHER_STATION'\n" +
                "   AND RS.OBSOLETE_DATE IS NULL\n" +
                ") SRC\n" +
                "ON (SRC.ROAD_STATION_ID = TGT.ROAD_STATION_ID AND SRC.ROAD_STATION_SENSOR_ID = TGT.ROAD_STATION_SENSOR_ID)\n" +
                "WHEN NOT MATCHED THEN INSERT (TGT.ROAD_STATION_ID, TGT.ROAD_STATION_SENSOR_ID)\n" +
                "     VALUES (SRC.ROAD_STATION_ID, SRC.ROAD_STATION_SENSOR_ID)";
        jdbcTemplate.execute(merge);

        log.info("Commit changes");
        assertTrue(TestTransaction.isActive());
        TestTransaction.flagForCommit();
        TestTransaction.end();
        assertFalse(TestTransaction.isActive());
        log.info("Commit done");
    }

    @After
    public void setRollBackLotjuIds() {
        if (!TestTransaction.isActive()) {
            TestTransaction.start();
        }

        Iterator<WeatherStation> stationsIter =
                weatherStationService.findAllWeatherStationsMappedByLotjuId().values().iterator();
        while (stationsIter.hasNext()) {
            WeatherStation lam = stationsIter.next();
            if (lam.getLotjuId() < 0) {
                lam.setLotjuId(null);
                lam.getRoadStation().setLotjuId(null);
            }
        }

        List<RoadStationSensor> availableSensors =
                roadStationSensorService.findAllRoadStationSensors(RoadStationType.WEATHER_STATION);
        for (RoadStationSensor availableSensor : availableSensors) {
            if (availableSensor.getLotjuId() < 0) {
                availableSensor.setLotjuId(null);
            }
        }
        assertTrue(TestTransaction.isActive());
        TestTransaction.flagForCommit();
        TestTransaction.end();
        assertFalse(TestTransaction.isActive());
    }

    @Test
    public void test1PerformanceForReceivedMessages() throws JAXBException, DatatypeConfigurationException {

        Map<Long, WeatherStation> weatherStationsWithLotjuId = weatherStationService.findAllWeatherStationsMappedByLotjuId();

        JmsMessageListener<Tiesaa> tiesaaJmsMessageListener =
                new JmsMessageListener<Tiesaa>(Tiesaa.class, "weatherJmsMessageListener", lockingService, UUID.randomUUID().toString()) {
            @Override
            protected void handleData(List<Tiesaa> data) {
                long start = System.currentTimeMillis();
                if (TestTransaction.isActive()) {
                    TestTransaction.flagForCommit();
                    TestTransaction.end();
                }
                TestTransaction.start();
                Assert.assertTrue("Update failed", sensorDataUpdateService.updateWeatherData(data));

                TestTransaction.flagForCommit();
                TestTransaction.end();
                long end = System.currentTimeMillis();
                log.info("handleData took " + (end-start) + " ms");
            }
        };

        DatatypeFactory df = DatatypeFactory.newInstance();
        GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
        XMLGregorianCalendar xgcal = df.newXMLGregorianCalendar(gcal);

        // Generate update-data
        final float minX = 0.0f;
        final float maxX = 100.0f;
        Random rand = new Random();
        float arvo = rand.nextFloat() * (maxX - minX) + minX;
        log.info("Start with arvo " + arvo);

        final List<RoadStationSensor> availableSensors =
                roadStationSensorService.findAllRoadStationSensors(RoadStationType.WEATHER_STATION);

        Iterator<WeatherStation> stationsIter = weatherStationsWithLotjuId.values().iterator();

        int testBurstsLeft = 10;
        long handleDataTotalTime = 0;
        long maxHandleTime = testBurstsLeft * (long)(1000 * 1.7);
        final List<Tiesaa> data = new ArrayList<>();
        while(testBurstsLeft > 0) {
            testBurstsLeft--;

            long start = System.currentTimeMillis();
            data.clear();
            while (true) {
                if (!stationsIter.hasNext()) {
                    stationsIter = weatherStationsWithLotjuId.values().iterator();
                }
                WeatherStation currentStation = stationsIter.next();

                Tiesaa tiesaa = new Tiesaa();
                data.add(tiesaa);

                tiesaa.setAsemaId(currentStation.getLotjuId());
                tiesaa.setAika(xgcal);
                Tiesaa.Anturit tiesaaAnturit = new Tiesaa.Anturit();
                tiesaa.setAnturit(tiesaaAnturit);
                List<Tiesaa.Anturit.Anturi> anturit = tiesaaAnturit.getAnturi();
                for (RoadStationSensor availableSensor : availableSensors) {
                    Tiesaa.Anturit.Anturi anturi = new Tiesaa.Anturit.Anturi();
                    anturit.add(anturi);

                    anturi.setArvo(arvo);
                    anturi.setLaskennallinenAnturiId(availableSensor.getLotjuId());
                    if (anturit.size() >= 30) {
                        break;
                    }
                }

                xgcal.add(df.newDuration(1000));
                arvo = arvo + 0.1f;

                if (data.size() >= 30) {
                    break;
                }
            }
            long end = System.currentTimeMillis();
            long duartion = (end - start);
            log.info("Data generation took " + duartion + " ms");
            long startHandle = System.currentTimeMillis();
            tiesaaJmsMessageListener.handleData(data);
            long endHandle = System.currentTimeMillis();
            handleDataTotalTime = handleDataTotalTime + (endHandle-startHandle);

            try {
                // send data with 1 s intervall
                long sleep = 1000 - duartion;
                if (sleep < 0) {
                    log.error("Data generation took too long");
                } else {
                    Thread.sleep(sleep);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("End with arvo " + arvo);
        log.info("Handle weather data total took " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms " + (handleDataTotalTime <= maxHandleTime ? "(OK)" : "(FAIL)"));

        log.info("Check data validy");
        // Assert sensor values are updated to db
        List<Long> tiesaaLotjuIds = data.stream().map(Tiesaa::getAsemaId).collect(Collectors.toList());
        Map<Long, List<SensorValue>> valuesMap =
                    roadStationSensorService.findSensorvaluesListMappedByLamLotjuId(tiesaaLotjuIds, RoadStationType.WEATHER_STATION);

        for (Tiesaa tiesaa : data) {
            long asemaLotjuId = tiesaa.getAsemaId();
            List<SensorValue> sensorValues = valuesMap.get(asemaLotjuId);
            List<Tiesaa.Anturit.Anturi> anturit = tiesaa.getAnturit().getAnturi();

            for (Tiesaa.Anturit.Anturi anturi : anturit) {
                Optional<SensorValue> found =
                        sensorValues
                                .stream()
                                .filter(sensorValue -> sensorValue.getRoadStationSensor().getLotjuId() != null)
                                .filter(sensorValue -> sensorValue.getRoadStationSensor().getLotjuId() == anturi.getLaskennallinenAnturiId())
                                .findFirst();
                Assert.assertTrue(found.isPresent());
                Assert.assertEquals(found.get().getValue(), (double) anturi.getArvo(), 0.05d);
            }
        }

        Assert.assertTrue("Handle data took too much time " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms", handleDataTotalTime <= maxHandleTime);
    }

    @Test
    public void test2LastUpdated() {
        LocalDateTime lastUpdated = roadStationSensorService.getSensorValueLastUpdated(RoadStationType.WEATHER_STATION);
        assertTrue(lastUpdated.isAfter(LocalDateTime.now().minusMinutes(2)));

        List<SensorValueDto> updated = roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(lastUpdated.minusSeconds(1), RoadStationType.WEATHER_STATION);
        assertFalse(updated.isEmpty());
    }
}