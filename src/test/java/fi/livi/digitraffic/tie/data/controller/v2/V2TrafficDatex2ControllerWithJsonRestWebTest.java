package fi.livi.digitraffic.tie.data.controller.v2;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_DATEX2_PATH;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.TRAFFIC_INCIDENT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.external.tloik.ims.ImsMessage;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2DataService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;

public class V2TrafficDatex2ControllerWithJsonRestWebTest extends AbstractRestWebTest {

    @Autowired
    protected Datex2DataService datex2DataService;

    @Autowired
    protected V2Datex2UpdateService v2Datex2UpdateService;

    @Autowired
    protected Datex2Repository datex2Repository;

    @Autowired
    private Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    private ObjectMapper objectMapper;

    private final String incident1_past_id = "GUID50005166";
    private final String incident2_active_id = "GUID50006936";
    private final String incident3_active_id = "GUID50013339";
    private final String incident4_active_id_invalid_json_duration = "GUID60013339";
    private final String incident1_past_estimated_minimum = "PT6H";
    private final String incident2_active_estimated_minimum = "PT7H";
    private final String incident3_active_estimated_minimum = "PT8H";
    private final String incident3_active_estimated_maximum = "PT12H";

    @Before
    public void updateData() throws IOException {
        datex2Repository.deleteAll();
        // GUID5000526801 in past
        final String incident1 = readResourceContent("classpath:tloik/ims/TrafficIncidentImsMessage-2016-09-12-20-51-24-602.xml");
        // GUID50006936 active
        final String incident2 = readResourceContent("classpath:tloik/ims/TrafficIncidentImsMessage-2016-11-17-18-34-36-299.xml");
        // GUID50013339 active
        final String incident3 = readResourceContent("classpath:tloik/ims/TrafficIncidentImsMessage-2017-08-10-15-59-34-896.xml");
        // GUID60013339 active
        final String incident4 = readResourceContent("classpath:tloik/ims/TrafficIncidentImsMessage-invalid-duration.xml");

        updateFromImsMessage(incident1, TRAFFIC_INCIDENT);
        updateFromImsMessage(incident2, TRAFFIC_INCIDENT);
        updateFromImsMessage(incident3, TRAFFIC_INCIDENT);
        updateFromImsMessage(incident4, TRAFFIC_INCIDENT);
    }

    @Test
    public void datex2incident() throws Exception {
        final String xml = getResponse(getUrl(TRAFFIC_INCIDENT, false, 0));
        final String json = getResponse(getUrl(TRAFFIC_INCIDENT, true, 0));
        assertXml(xml);
        assertJson(json);
        assertTextNotExistInMessage(incident1_past_id, xml);
        assertTextNotExistInMessage(incident1_past_id, json);
        assertTextExistInMessage(incident2_active_id, xml);
        assertTextExistInMessage(incident2_active_id, json);
        assertTextExistInMessage(incident3_active_id, xml);
        assertTextExistInMessage(incident3_active_id, json);

        assertTextExistInMessage(incident4_active_id_invalid_json_duration, xml);
        assertTextNotExistInMessage(incident4_active_id_invalid_json_duration, json);

        assertTextNotExistInMessage(incident1_past_estimated_minimum, json);
        assertTextExistInMessage(incident2_active_estimated_minimum, json);
        assertTextExistInMessage(incident3_active_estimated_minimum, json);
        assertTextExistInMessage(incident3_active_estimated_maximum, json);
    }

    @Test
    public void datex2incidentInPast() throws Exception {
        final String xml = getResponse(getUrl(TRAFFIC_INCIDENT, false, 200000));
        final String json = getResponse(getUrl(TRAFFIC_INCIDENT, true, 200000));
        assertXml(xml);
        assertJson(json);
        assertTextExistInMessage(incident1_past_id, xml);
        assertTextExistInMessage(incident1_past_id, json);
        assertTextExistInMessage(incident2_active_id, xml);
        assertTextExistInMessage(incident2_active_id, json);
        assertTextExistInMessage(incident3_active_id, xml);
        assertTextExistInMessage(incident3_active_id, json);
    }

    private void assertXml(final String xml) {
        try {
            jaxb2Marshaller.unmarshal(new StringSource(xml));
        } catch (XmlMappingException e) {
            throw new IllegalArgumentException("Not XML: " + xml, e);
        }
    }

    private void assertJson(String json) {
        try {
            objectMapper.readTree(json);
        } catch (IOException e) {
            throw new IllegalArgumentException("Not JSON: " + json, e);
        }
    }

    private static String getUrl(final Datex2MessageType messageType, final boolean json, final int inactiveHours) {
        return API_BETA_BASE_PATH + TRAFFIC_DATEX2_PATH + "/" + messageType.toParameter() + (json ? ".json" : ".xml") + "?inactiveHours=" + inactiveHours;
    }

    private void assertTextExistInMessage(final String text, final String xml) {
        assertTextExistenceInMessage(text, true, xml);
    }

    private void assertTextNotExistInMessage(final String text, final String xml) {
        assertTextExistenceInMessage(text, false, xml);
    }

    private void assertTextExistenceInMessage(final String text, final boolean shouldExist, final String message) {
        if (shouldExist) {
            assertTrue(text + " should exist in response", message.contains(text));
        } else {
            assertFalse(text + " should not exist in response", message.contains(text));
        }
    }

    private String getResponse(final String url) throws Exception {
        return mockMvc.perform(get(url)).andReturn().getResponse().getContentAsString();
    }

    private void updateFromImsMessage(final String imsXml, final Datex2MessageType type) {
        final ImsMessage ims = (ImsMessage) jaxb2Marshaller.unmarshal(new StringSource(imsXml));
        v2Datex2UpdateService.updateTrafficImsMessages(Collections.singletonList(ims), type);
    }
}
