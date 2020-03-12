package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.model.v1.maintenance.WorkMachineObservation;
import fi.livi.digitraffic.tie.service.v1.MaintenanceDataService;

public class WorkMachineObservationServiceTest extends AbstractWorkmachineDataServiceTest {
    private static final Logger log = LoggerFactory.getLogger(WorkMachineObservationServiceTest.class);

    @Autowired
    protected EntityManager entityManager;

    @Test
    public void findWorkMachineObservationByWorkMachineHarjaIdAndHarjaUrakkaId() throws IOException {
        readTrackingJsonAndSave("timegap", 3 );
        List<WorkMachineObservation> all =
            workMachineObservationService.findWorkMachineObservationsByWorkMachineHarjaIdAndHarjaUrakkaId(1L, 1L);
        log.info("all: {}", all);
    }

    @Test
    public void findLastWorkMachineObservationByWorkMachineHarjaIdAndHarjaUrakkaId() throws IOException {
        readTrackingJsonAndSave("timegap", 3);
        WorkMachineObservation found =
            workMachineObservationService.findLastWorkMachineObservationByWorkMachineHarjaIdAndHarjaUrakkaId(1L, 1L);
        log.info("found: {}", found);
    }

    /**
     * Tests that trackings with gap between observation time is over limit will be divided to distinct observations
     */
    @Test
    public void convertUnhandledWorkMachineTrackingsToObservationsWithTimeGap() throws IOException {

        readTrackingJsonAndSave("timegap", 3);

        Map<Pair<Integer, Integer>, List<MaintenanceDataService.ObservationFeatureWrapper>> unhandledMap =
            maintenanceDataService.findUnhandledTrakkingsOldestFirstMappedByHarjaWorkMachineAndContract(100);

        convertWorkMachineTrackingsToObservationsAndSave(unhandledMap);

        // Must be flushed to get coordinates read
        entityManager.flush();
        entityManager.clear();

        List<WorkMachineObservation> obs =
            workMachineObservationService.findWorkMachineObservationsByWorkMachineHarjaIdAndHarjaUrakkaId(1234, 999999);

        // 3. observation has over 30 min gap from 2. observation -> should be divided to two distinct observations
        Assert.assertEquals(2, obs.size());

        final WorkMachineObservation first = obs.get(0);
        final WorkMachineObservation second = obs.get(1);

        Assert.assertEquals(10, first.getCoordinates().size());
        Assert.assertEquals(5, second.getCoordinates().size());
    }

    /**
     * Tests for trackings with and without task will be divided to distinct observations
     */
    @Test
    public void convertUnhandledWorkMachineTrackingsToObservationsWithMissingTask() throws IOException {

        readTrackingJsonAndSave("missingtask", 5);

        Map<Pair<Integer, Integer>, List<MaintenanceDataService.ObservationFeatureWrapper>> unhandledMap =
            maintenanceDataService.findUnhandledTrakkingsOldestFirstMappedByHarjaWorkMachineAndContract(100);

        convertWorkMachineTrackingsToObservationsAndSave(unhandledMap);

        // Must be flushed to get coordinates read
        entityManager.flush();
        entityManager.clear();

        List<WorkMachineObservation> obs =
            workMachineObservationService.findWorkMachineObservationsByWorkMachineHarjaIdAndHarjaUrakkaId(12345, 999999);

        // Observation 3/5 has empty task -> 1+2, 3, 3+4 should be result observations, 3 as transition
        Assert.assertEquals(3, obs.size());

        final WorkMachineObservation first = obs.get(0);
        final WorkMachineObservation transition = obs.get(1);
        final WorkMachineObservation second = obs.get(2);

        Assert.assertEquals(10, first.getCoordinates().size());
        Assert.assertEquals(5, transition.getCoordinates().size());
        Assert.assertEquals(9, second.getCoordinates().size());

        Assert.assertEquals(false, first.isTransition());
        Assert.assertEquals(true, transition.isTransition());
        Assert.assertEquals(false, second.isTransition());

    }
}