package fi.livi.digitraffic.tie.data.service;

import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.Before;
import org.junit.Test;

import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.response.TrafficDisordersDatex2Response;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

public class Datex2DataServiceTest extends AbstractDatex2DataServiceTest {

    private String disorder1;
    private String disorder2;
    private String disorder3;
    private static final String DISORDER1_GUID = "GUID50005166";
    private static final String DISORDER2_GUID = "GUID50006936";
    private static final String DISORDER3_GUID = "GUID50013339";

    private String roadwork1;
    private static final String ROADWORK1_GUID = "GUID50350441";

    private String weightRestriction1;
    private static final String WR1_GUID = "GUID50354262";

    private static final String NOT_FOUND_GUID = "NOT_FOUND";

    @Before
    public void init() throws IOException {
        disorder1 = readResourceContent("classpath:lotju/datex2/InfoXML_2016-09-12-20-51-24-602.xml");
        disorder2 = readResourceContent("classpath:lotju/datex2/InfoXML_2016-11-17-18-34-36-299.xml");
        disorder3 = readResourceContent("classpath:lotju/datex2/Datex2_2017-08-10-15-59-34-896.xml");
        roadwork1 = readResourceContent("classpath:lotju/roadwork/roadwork1.xml");
        weightRestriction1 = readResourceContent("classpath:lotju/weight_restrictions/wr1.xml");
    }


    @Test(expected = ObjectNotFoundException.class)
    public void getAllTrafficDisordersBySituationIdNotFound() {
        deleteAllDatex2();

        datex2DataService.getAllTrafficDisordersBySituationId(NOT_FOUND_GUID);
    }

    @Test
    public void endedShouldNotFound() {
        deleteAllDatex2();
        updateTrafficIncidents(disorder3);
        // Not ended yet
        findActiveTrafficAlertsAndAssert(DISORDER3_GUID, true, 0);

        // Set situation Endtime to 1 min ago
        final String disorder3Ended = addEndTime(disorder3, Instant.now().minus(10, ChronoUnit.MINUTES));

        updateTrafficIncidents(disorder3Ended);

        // Disorder should not be found as active
        findActiveTrafficAlertsAndAssert(DISORDER3_GUID, false, 0);
    }

    @Test
    public void findActiveInPast() {
        deleteAllDatex2();
        // Set situation Endtime to 2h 1 min ago
        final String disorder3Ended = addEndTime(disorder3, Instant.now().minus(121, ChronoUnit.MINUTES));
        updateTrafficIncidents(disorder3Ended);

        // Disorder is ended > 2h in past. With parameter value > 3 it should found, but not with < 3
        findActiveTrafficAlertsAndAssert(DISORDER3_GUID, false, 2);
        findActiveTrafficAlertsAndAssert(DISORDER3_GUID, true, 3);
    }

    @Test
    public void activeAndActiveInPast() {
        deleteAllDatex2();
        updateTrafficIncidents(disorder2);
        updateTrafficIncidents(disorder3);
        // Both active
        findActiveTrafficAlertsAndAssert(DISORDER2_GUID, true, 0);
        findActiveTrafficAlertsAndAssert(DISORDER3_GUID, true, 0);

        // After ending disorder3 it  not not be found
        final String disorder3Ended = addEndTime(disorder3, Instant.now().minus(10, ChronoUnit.MINUTES));
        updateTrafficIncidents(disorder3Ended);

        findActiveTrafficAlertsAndAssert(DISORDER2_GUID, true, 0);
        findActiveTrafficAlertsAndAssert(DISORDER3_GUID, false, 0);
    }

    @Test
    public void updateTrafficAlerts() {
        deleteAllDatex2();

        updateTrafficIncidents(disorder1);
        findTrafficAlertsAndAssert(DISORDER1_GUID, true);
        findTrafficAlertsAndAssert(DISORDER2_GUID, false);
        updateTrafficIncidents(disorder2);

        assertCollectionSize(2, datex2Repository.findAll());

        findTrafficAlertsAndAssert(DISORDER1_GUID, true);
        findTrafficAlertsAndAssert(DISORDER2_GUID, true);

        final TrafficDisordersDatex2Response allActive = datex2DataService.findActiveTrafficDisorders(0);
        assertCollectionSize(1, allActive.getDisorders());

        final SituationPublication active = getSituationPublication(allActive);
        assertCollectionSize(1, active.getSituations());
        assertTrue(active.getSituations().get(0).getId().equals(DISORDER2_GUID));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getAllRoadworksBySituationIdNotFound() {
        deleteAllDatex2();

        datex2DataService.getAllRoadworksBySituationId(NOT_FOUND_GUID);
    }

    @Test
    public void findActiveRoadworks() {
        deleteAllDatex2();

        updateRoadworks(roadwork1);

        assertCollectionSize(1, datex2DataService.findActiveRoadworks(0).getRoadworks());

        assertNotNull(datex2DataService.getAllRoadworksBySituationId(ROADWORK1_GUID));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getAllWeightRestrictionsBySituationIdNotFound() {
        deleteAllDatex2();

        datex2DataService.getAllWeightRestrictionsBySituationId(NOT_FOUND_GUID);
    }

    @Test
    public void findActiveWeightRestrictions() {
        deleteAllDatex2();

        updateWeightRestrictions(weightRestriction1);

        assertCollectionSize(1, datex2DataService.findActiveWeightRestrictions(0).getRestrictions());

        assertNotNull(datex2DataService.getAllWeightRestrictionsBySituationId(WR1_GUID));
    }
}
