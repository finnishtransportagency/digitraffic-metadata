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
import fi.livi.digitraffic.tie.lotju.xsd.lam.Lam;
import fi.livi.digitraffic.tie.metadata.model.LamStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;
import fi.livi.digitraffic.tie.metadata.service.lam.LamStationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

@Transactional
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LamJmsMessageListenerTest extends MetadataIntegrationTest {
    
    private static final Logger log = LoggerFactory.getLogger(LamJmsMessageListenerTest.class);

    @Autowired
    private RoadStationSensorService roadStationSensorService;

    @Autowired
    private LamStationService lamStationService;

    @Autowired
    private SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    LockingService lockingService;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Before
    public void setUpTestData() {

        // Generate test-data: Lamstations with sensors
        Map<Long, LamStation> lamsWithLotjuId = lamStationService.findAllLamStationsByMappedByLotjuId();
        Set<Long> usedLotjuIds = new HashSet<>(lamsWithLotjuId.keySet());

        long stationGeneratedLotjuId = -1;
        ArrayList<LamStation> stations = new ArrayList<>();
        Map<Long, LamStation> lams = lamStationService.findAllLamStationsMappedByByRoadStationNaturalId();

        for (Map.Entry<Long, LamStation> longLamStationEntry : lams.entrySet()) {
            LamStation lam = longLamStationEntry.getValue();
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

        log.info("Found " + stations.size() + " Lam Stations");

        Assert.assertTrue(stations.size() > 100);

        List<RoadStationSensor> availableSensors =
                roadStationSensorService.findAllRoadStationSensors(RoadStationType.LAM_STATION);

        Assert.assertTrue(availableSensors.size() > 5);

        log.info("Found " + availableSensors.size() + " available sensors for Lam Stations");

        jdbcTemplate.execute(
                "UPDATE LAM_STATION\n" +
                "SET LOTJU_ID = -1 * (1+DBMS_ROWID.ROWID_ROW_NUMBER(ROWID))\n" +
                "WHERE LOTJU_ID IS NULL");

        jdbcTemplate.execute(
                "UPDATE ROAD_STATION_SENSOR\n" +
                "SET LOTJU_ID = -1 * (1+DBMS_ROWID.ROWID_ROW_NUMBER(ROWID))\n" +
                "WHERE LOTJU_ID IS NULL");
        log.info("Add available sensors for Lam Stations");
        String merge =
                "MERGE INTO ROAD_STATION_SENSORS TGT\n" +
                "USING (\n" +
                "  SELECT RS.ID ROAD_STATION_ID, S.ID ROAD_STATION_SENSOR_ID\n" +
                "  FROM ROAD_STATION_SENSOR S, ROAD_STATION RS\n" +
                "  WHERE S.OBSOLETE = 0\n" +
                "    AND S.ROAD_STATION_TYPE = 'LAM_STATION'\n" +
                "    AND EXISTS (\n" +
                "      SELECT NULL\n" +
                "      FROM ALLOWED_ROAD_STATION_SENSOR ALLOWED\n" +
                "      WHERE ALLOWED.NATURAL_ID = S.NATURAL_ID\n" +
                "        AND ALLOWED.ROAD_STATION_TYPE = S.ROAD_STATION_TYPE\n" +
                "   )\n" +
                "   AND RS.ROAD_STATION_TYPE = 'LAM_STATION'\n" +
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
        // Now we have stations with sensors and lotjuid:s that we can update
    }


    @After
    public void setRollBackLotjuIds() {
        if (!TestTransaction.isActive()) {
            TestTransaction.start();
        }

        jdbcTemplate.execute(
                "UPDATE LAM_STATION\n" +
                "SET LOTJU_ID = NULL\n" +
                "WHERE LOTJU_ID < 0");

        jdbcTemplate.execute(
                "UPDATE ROAD_STATION_SENSOR\n" +
                "SET LOTJU_ID = NULL\n" +
                "WHERE LOTJU_ID < 0");

        assertTrue(TestTransaction.isActive());
        TestTransaction.flagForCommit();
        TestTransaction.end();
        assertFalse(TestTransaction.isActive());
    }

    @Test
    public void test1PerformanceForReceivedMessages() throws JAXBException, DatatypeConfigurationException {

        Map<Long, LamStation> lamsWithLotjuId = lamStationService.findAllLamStationsByMappedByLotjuId();

        JmsMessageListener<Lam> lamJmsMessageListener =
                new JmsMessageListener<Lam>(Lam.class, "lamJmsMessageListener", lockingService, UUID.randomUUID().toString()) {
            @Override
            protected void handleData(List<Lam> data) {
                long start = System.currentTimeMillis();
                if (TestTransaction.isActive()) {
                    TestTransaction.flagForCommit();
                    TestTransaction.end();
                }
                TestTransaction.start();
                Assert.assertTrue("Update failed", sensorDataUpdateService.updateLamData(data));
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
        final Random rand = new Random();
        float arvo = rand.nextFloat() * (maxX - minX) + minX;
        log.info("Start with arvo " + arvo);


        final List<RoadStationSensor> availableSensors =
                roadStationSensorService.findAllRoadStationSensors(RoadStationType.LAM_STATION);

        Iterator<LamStation> stationsIter = lamsWithLotjuId.values().iterator();

        int testBurstsLeft = 10;
        long handleDataTotalTime = 0;
        long maxHandleTime = testBurstsLeft * 1000;
        final List<Lam> data = new ArrayList<>(lamsWithLotjuId.size());
        while(testBurstsLeft > 0) {
            testBurstsLeft--;

            long start = System.currentTimeMillis();
            data.clear();
            while (true) {
                if (!stationsIter.hasNext()) {
                    stationsIter = lamsWithLotjuId.values().iterator();
                }
                LamStation currentStation = stationsIter.next();

                Lam lam = new Lam();
                data.add(lam);

                lam.setAsemaId(currentStation.getLotjuId());
                lam.setAika(xgcal);
                Lam.Anturit lamAnturit = new Lam.Anturit();
                lam.setAnturit(lamAnturit);
                List<Lam.Anturit.Anturi> anturit = lamAnturit.getAnturi();
                for (RoadStationSensor availableSensor : availableSensors) {
                    Lam.Anturit.Anturi anturi = new Lam.Anturit.Anturi();
                    anturit.add(anturi);
                    anturi.setArvo(arvo);
                    anturi.setLaskennallinenAnturiId(availableSensor.getLotjuId().toString());
                }
                xgcal.add(df.newDuration(1000));
                arvo += 0.1f;

                if (data.size() >= 100) {
                    break;
                }
            }
            long end = System.currentTimeMillis();
            long duartion = (end - start);
            log.info("Data generation took " + duartion + " ms");
            long startHandle = System.currentTimeMillis();
            lamJmsMessageListener.handleData(data);
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
        log.info("Handle lam data total took " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms " + (handleDataTotalTime <= maxHandleTime ? "(OK)" : "(FAIL)"));

        log.info("Check data validy");
        // Assert sensor values are updated to db
        List<Long> lamLotjuIds = data.stream().map(Lam::getAsemaId).collect(Collectors.toList());
        Map<Long, List<SensorValue>> valuesMap =
                roadStationSensorService.findSensorvaluesListMappedByLamLotjuId(lamLotjuIds, RoadStationType.LAM_STATION);

        for (Lam lam : data) {
            long asemaLotjuId = lam.getAsemaId();
            List<SensorValue> sensorValues = valuesMap.get(asemaLotjuId);
            List<Lam.Anturit.Anturi> anturit = lam.getAnturit().getAnturi();

            for (Lam.Anturit.Anturi anturi : anturit) {
                Optional<SensorValue> found =
                        sensorValues
                                .stream()
                                .filter(sensorValue -> sensorValue.getRoadStationSensor().getLotjuId() != null)
                                .filter(sensorValue -> anturi.getLaskennallinenAnturiId().equals(sensorValue.getRoadStationSensor().getLotjuId().toString()))
                                .findFirst();
                Assert.assertTrue(found.isPresent());
                Assert.assertEquals(found.get().getValue(), (double) anturi.getArvo(), 0.05d);
            }
        }

        Assert.assertTrue("Handle data took too much time " + handleDataTotalTime + " ms and max was " + maxHandleTime + " ms", handleDataTotalTime <= maxHandleTime);
    }

    @Test
    public void test2LastUpdated() {
        LocalDateTime lastUpdated = roadStationSensorService.getSensorValueLastUpdated(RoadStationType.LAM_STATION);
        assertTrue(lastUpdated.isAfter(LocalDateTime.now().minusMinutes(2)));

        List<SensorValueDto> updated = roadStationSensorService.findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(lastUpdated.minusSeconds(1), RoadStationType.LAM_STATION);
        assertFalse(updated.isEmpty());
    }
}