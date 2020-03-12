package fi.livi.digitraffic.tie.data.controller.v2;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_DATEX2_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_DISORDERS_DATEX2_PATH;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.ROADWORK;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.TRAFFIC_INCIDENT;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.WEIGHT_RESTRICTION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2DataService;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2UpdateService;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2SimpleMessageUpdater;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;

public class V2TrafficDatex2ControllerRestWebTest extends AbstractRestWebTest {

    @Autowired
    protected Datex2DataService datex2DataService;

    @Autowired
    protected Datex2UpdateService datex2UpdateService;

    @Autowired
    protected Datex2Repository datex2Repository;

    @Autowired
    private Datex2SimpleMessageUpdater datex2SimpleMessageUpdater;

    private final String incident1_past_id = "GUID50005166";
    private final String incident2_active_id = "GUID50006936";
    private final String incident3_active_id = "GUID50013339";
    private final String roadwork1_active_id = "GUID50350441";
    private final String weightRestriction1_active_id = "GUID5035473201";

    @Before
    public void updateData() throws IOException {
        datex2Repository.deleteAll();
        // GUID5000526801 in past
        final String incident1 = readResourceContent("classpath:lotju/datex2/InfoXML_2016-09-12-20-51-24-602.xml");
        // GUID50006936 active
        final String incident2 = readResourceContent("classpath:lotju/datex2/InfoXML_2016-11-17-18-34-36-299.xml");
        // GUID50013339 active
        final String incident3 = readResourceContent("classpath:lotju/datex2/Datex2_2017-08-10-15-59-34-896.xml");
        // GUID50350441 active
        final String roadwork1 = readResourceContent("classpath:lotju/roadwork/roadwork1.xml");
        // GUID5035473201 active
        final String weightRestriction1 = readResourceContent("classpath:lotju/weight_restrictions/wr1.xml");

        updateDatex2(incident1, TRAFFIC_INCIDENT);
        updateDatex2(incident2, TRAFFIC_INCIDENT);
        updateDatex2(incident3, TRAFFIC_INCIDENT);
        updateDatex2(roadwork1, ROADWORK);
        updateDatex2(weightRestriction1, WEIGHT_RESTRICTION);
    }


    @Test
    public void datex2incident() throws Exception {
        final String url = getUrl(TRAFFIC_INCIDENT, false, 0);
        final String xml = getResponse(url);
        assertSituationNotExistInXml(incident1_past_id, xml);
        assertSituationExistInXml(incident2_active_id, xml);
        assertSituationExistInXml(incident3_active_id, xml);
        assertSituationNotExistInXml(roadwork1_active_id, xml);
        assertSituationNotExistInXml(weightRestriction1_active_id, xml);
    }

    @Test
    public void datex2incidentInPast() throws Exception {
        final String url = getUrl(TRAFFIC_INCIDENT, false, 200000);
        final String xml = getResponse(url);
        assertSituationExistInXml(incident1_past_id, xml);
        assertSituationExistInXml(incident2_active_id, xml);
        assertSituationExistInXml(incident3_active_id, xml);
        assertSituationNotExistInXml(roadwork1_active_id, xml);
        assertSituationNotExistInXml(weightRestriction1_active_id, xml);
    }

    @Test
    public void datex2roadwork() throws Exception {
        final String url = getUrl(ROADWORK, false, 0);
        final String xml = getResponse(url);
        assertSituationNotExistInXml(incident1_past_id, xml);
        assertSituationNotExistInXml(incident2_active_id, xml);
        assertSituationNotExistInXml(incident3_active_id, xml);
        assertSituationExistInXml(roadwork1_active_id, xml);
        assertSituationNotExistInXml(weightRestriction1_active_id, xml);
    }

    @Test
    public void datex2weightRestriction() throws Exception {
        final String url = getUrl(WEIGHT_RESTRICTION, false, 0);
        final String xml = getResponse(url);
        assertSituationNotExistInXml(incident1_past_id, xml);
        assertSituationNotExistInXml(incident2_active_id, xml);
        assertSituationNotExistInXml(incident3_active_id, xml);
        assertSituationNotExistInXml(roadwork1_active_id, xml);
        assertSituationExistInXml(weightRestriction1_active_id, xml);
    }

    private static String getUrl(final Datex2MessageType messageType, final boolean json, final int inactiveHours) {
        return API_BETA_BASE_PATH + TRAFFIC_DATEX2_PATH + "/" + messageType.toParameter() + (json ? ".json" : ".xml") + "?inactiveHours=" + inactiveHours;
    }

    private void assertSituationExistInXml(final String situationId,  final String xml) {
        assertSituationExistenceInXml(situationId, true, xml);
    }

    private void assertSituationNotExistInXml(final String situationId,  final String xml) {
        assertSituationExistenceInXml(situationId, false, xml);
    }

    private void assertSituationExistenceInXml(final String situationId, final boolean shouldExist, final String xml) {
        if (shouldExist) {
            assertTrue(situationId + " should exist in response", xml.contains(situationId));
        } else {
            assertFalse(situationId + " should not exist in response", xml.contains(situationId));
        }
    }

    private String getResponse(final String url) throws Exception {
        return mockMvc.perform(get(url)).andReturn().getResponse().getContentAsString();
    }

    private void updateDatex2(final String datex2Xml, final Datex2MessageType type) {
        datex2UpdateService.updateDatex2Data(datex2SimpleMessageUpdater.convert(datex2Xml, type, ZonedDateTime.now()));
    }
}
