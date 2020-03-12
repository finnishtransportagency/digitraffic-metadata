package fi.livi.digitraffic.tie.service.v2.datex2;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.annotation.Rollback;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.conf.jaxb2.XmlMarshallerConfiguration;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.external.tloik.ims.ImsMessage;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2UpdateService;

@Import({ V2Datex2DataService.class, V2Datex2UpdateService.class, XmlMarshallerConfiguration.class, JacksonAutoConfiguration.class })
public class V2Datex2DataServiceTest extends AbstractServiceTest {

    private final static String GUID_WITH_JSON = "GUID50001238";
    private final static String GUID_NO_JSON = "GUID50001234";

    @Autowired
    private V2Datex2DataService v2Datex2DataService;

    @Autowired
    private Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    private V2Datex2UpdateService v2Datex2UpdateService;

    @Autowired
    private Datex2UpdateService datex2UpdateService;

    @Test
    public void activeIncidentsDatex2AndJsonEquals() throws IOException {
        // One active
        initDataFromFile("TrafficIncidentImsMessage.xml");

        final D2LogicalModel d2 = v2Datex2DataService.findActive(0, Datex2MessageType.TRAFFIC_INCIDENT);
        final List<Situation> activeSituations = ((SituationPublication) d2.getPayloadPublication()).getSituations();
        final TrafficAnnouncementFeatureCollection activeJsons = v2Datex2DataService.findActiveJson(0, Datex2MessageType.TRAFFIC_INCIDENT);

        AssertHelper.assertCollectionSize(1, activeSituations);
        AssertHelper.assertCollectionSize(1, activeJsons.getFeatures());
        final Situation situation = activeSituations.get(0);
        final TrafficAnnouncementFeature situationJson = activeJsons.getFeatures().get(0);

        final TrafficAnnouncementProperties jsonProperties = situationJson.getProperties();
        Assert.assertEquals(GUID_WITH_JSON, situation.getId());
        Assert.assertEquals(GUID_WITH_JSON, jsonProperties.situationId);

        final Instant start = ZonedDateTime.parse("2019-12-13T14:43:18.388+02:00").toInstant();

        final Instant situationStart = situation.getSituationRecords().get(0).getValidity().getValidityTimeSpecification().getOverallStartTime();
        final ZonedDateTime situationJsonStart = jsonProperties.releaseTime;
        Assert.assertEquals(start, situationStart);
        Assert.assertEquals(start, situationJsonStart.toInstant());

        final String commentXml = situation.getSituationRecords().get(0).getGeneralPublicComments().get(0).getComment().getValues().getValues().stream()
            .filter(c -> c.getLang().equals("fi")).findFirst().orElseThrow().getValue();
        final String descJson = jsonProperties.announcements.get(0).getLocation().description;
        final String titleJson = jsonProperties.announcements.get(0).getTitle();

        Assert.assertTrue(commentXml.contains(titleJson.trim()));
        Assert.assertTrue(commentXml.contains(descJson.trim()));
    }

    @Test
    public void findBySituationId() throws IOException {
        // One active
        initDataFromFile("TrafficIncidentImsMessage.xml");

        final D2LogicalModel d2 = v2Datex2DataService.findAllBySituationId(GUID_WITH_JSON, Datex2MessageType.TRAFFIC_INCIDENT);
        final TrafficAnnouncementFeatureCollection jsons = v2Datex2DataService.findAllBySituationIdJson(GUID_WITH_JSON, Datex2MessageType.TRAFFIC_INCIDENT);

        final List<Situation> situations = ((SituationPublication) d2.getPayloadPublication()).getSituations();

        AssertHelper.assertCollectionSize(1, situations);
        AssertHelper.assertCollectionSize(1, jsons.getFeatures());
        final Situation situation = situations.get(0);
        final TrafficAnnouncementFeature situationJson = jsons.getFeatures().get(0);

        Assert.assertEquals(GUID_WITH_JSON, situation.getId());
        Assert.assertEquals(GUID_WITH_JSON, situationJson.getProperties().situationId);
    }

    @Test
    public void findActive() throws IOException {
        // One active with json
        initDataFromFile("TrafficIncidentImsMessage.xml");
        // One active without json
        initDataFromFile("TrafficIncidentImsMessageWithoutJson.xml");

        assertActiveMessageFound(GUID_WITH_JSON, true, true);
        assertActiveMessageFound(GUID_NO_JSON, true, false);
    }

    @Test
    public void findAllBySituationId() throws IOException {
        // One active with json
        initDataFromFile("TrafficIncidentImsMessage.xml");
        // One active without json
        initDataFromFile("TrafficIncidentImsMessageWithoutJson.xml");

        // Both guid should be found
        assertFoundBySituationId(GUID_WITH_JSON, true, true);
        // Only datex2 is found
        assertActiveMessageFound(GUID_NO_JSON, true, false);
    }

    private void assertActiveMessageFound(final String situationId, boolean foundInDatex2, boolean foundInJson) {
        final D2LogicalModel withOrWithoutJson = v2Datex2DataService.findActive(0, Datex2MessageType.TRAFFIC_INCIDENT);
        final SituationPublication situationPublication = ((SituationPublication) withOrWithoutJson.getPayloadPublication());
        final TrafficAnnouncementFeatureCollection withJson = v2Datex2DataService.findActiveJson(0, Datex2MessageType.TRAFFIC_INCIDENT);

        Assert.assertEquals(
            foundInDatex2,
            situationPublication.getSituations().stream().filter(s -> s.getId().equals(situationId)).findFirst().isPresent());
        Assert.assertEquals(
            foundInJson,
            withJson.getFeatures().stream().filter(f -> f.getProperties().situationId.equals(situationId)).findFirst().isPresent());
    }

    private void assertFoundBySituationId(final String situationId, boolean foundInDatex2, boolean foundInJson) {
        try {
            final D2LogicalModel withOrWithoutJson = v2Datex2DataService.findAllBySituationId(GUID_WITH_JSON, Datex2MessageType.TRAFFIC_INCIDENT);
            final SituationPublication situationPublication = ((SituationPublication) withOrWithoutJson.getPayloadPublication());
            Assert.assertEquals(
                foundInDatex2,
                situationPublication.getSituations().stream().filter(s -> s.getId().equals(situationId)).findFirst().isPresent());
        } catch (Exception e) { // not found
            Assert.assertFalse(foundInDatex2);
        }
        try {
            final TrafficAnnouncementFeatureCollection withJson =
                v2Datex2DataService.findAllBySituationIdJson(situationId, Datex2MessageType.TRAFFIC_INCIDENT);
            Assert.assertEquals(
                foundInJson,
                withJson.getFeatures().stream().filter(f -> f.getProperties().situationId.equals(situationId)).findFirst().isPresent());
        } catch (Exception e) { // not found
            Assert.assertFalse(foundInJson);
        }

    }

    private void initDataFromFile(final String file) throws IOException {
        final ArrayList<String> xmlImsMessages = readResourceContents("classpath:tloik/ims/" + file);
        final ImsMessage ims = (ImsMessage) jaxb2Marshaller.unmarshal(new StringSource(xmlImsMessages.get(0)));
        v2Datex2UpdateService.updateTrafficIncidentImsMessages(Collections.singletonList(ims));
    }
}
