package fi.livi.digitraffic.tie.service.jms;

import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.ROADWORK;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.TRAFFIC_INCIDENT;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.WEIGHT_RESTRICTION;
import static fi.livi.digitraffic.tie.service.AbstractDatex2DateServiceTest.GUID_WITH_JSON;
import static fi.livi.digitraffic.tie.service.AbstractDatex2DateServiceTest.ImsJsonVersion;
import static fi.livi.digitraffic.tie.service.AbstractDatex2DateServiceTest.readImsMessageResourceContent;
import static org.apache.commons.collections.CollectionUtils.union;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.conf.jms.ExternalIMSMessage;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncement;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.service.AbstractDatex2DateServiceTest.ImsXmlVersion;
import fi.livi.digitraffic.tie.service.jms.marshaller.ImsMessageMarshaller;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2DataService;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2UpdateService;

public class ImsDatex2JmsMessageListenerTest extends AbstractJmsMessageListenerTest {
    private static final Logger log = LoggerFactory.getLogger(ImsDatex2JmsMessageListenerTest.class);

    @Autowired
    private V2Datex2DataService v2Datex2DataService;

    @Autowired
    private Datex2Repository datex2Repository;

    @Autowired
    @Qualifier("imsJaxb2Marshaller")
    private Jaxb2Marshaller jaxb2MarshallerimsJaxb2Marshaller;

    @Autowired
    private V2Datex2UpdateService v2Datex2UpdateService;

    @Before
    public void cleanDbBefore() {
        datex2Repository.deleteAll();
    }

    @Before
    public void cleanDb() {
        datex2Repository.deleteAll();
    }

    @Test
    public void datex2ReceiveImsMessagesAllVersions() throws IOException {
        final JMSMessageListener<ExternalIMSMessage> jmsMessageListener = createImsJmsMessageListener();

        for (final ImsXmlVersion imsXmlVersion : ImsXmlVersion.values()) {
            for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                cleanDb();
                sendJmsMessage(imsXmlVersion, imsJsonVersion, jmsMessageListener);
                log.info("Run activeIncidentsDatex2AndJsonEquals with imsXmlVersion={} and imsJsonVersion={}", imsXmlVersion, imsJsonVersion);
                checkActiveSituations(GUID_WITH_JSON);
            }
        }
    }

    @Test
    public void datex2ReceiveImsMessagesV1_2_1JsonV0_2_6WithMultipleMessages() throws IOException {
        sendJmsMessage("tloik/ims/TrafficIncidentImsMessageV1_2_1JsonV0_2_6MultipleMessages.xml", createImsJmsMessageListener());
        checkActiveSituations("GUID00000001", "GUID00000002", "GUID00000003", "GUID00000004", "GUID00000005", "GUID00000006", "GUID00000007");
    }


    private void checkActiveSituations(final String...situationIdsToFind) {
        final List<Situation> situationIncidents = getSituations(v2Datex2DataService.findActive(0, TRAFFIC_INCIDENT));
        final List<Situation> situationRoadworks = getSituations(v2Datex2DataService.findActive(0, ROADWORK));
        final List<Situation> situationWeightRestrictions = getSituations(v2Datex2DataService.findActive(0, WEIGHT_RESTRICTION));
        final Collection<Situation> situations = union(union(situationIncidents, situationRoadworks), situationWeightRestrictions);

        final List<TrafficAnnouncementFeature> featureIncidents =
            v2Datex2DataService.findActiveJson(0, TRAFFIC_INCIDENT).getFeatures();
        final List<TrafficAnnouncementFeature> featureRoadworks =
            v2Datex2DataService.findActiveJson(0, ROADWORK).getFeatures();
        final List<TrafficAnnouncementFeature> featureWeightRestrictions =
            v2Datex2DataService.findActiveJson(0, WEIGHT_RESTRICTION).getFeatures();
        final Collection<TrafficAnnouncementFeature> features = union(union(featureIncidents, featureRoadworks), featureWeightRestrictions);

        assertCollectionSize("Situations size won't match.", situationIdsToFind.length, situations);
        assertCollectionSize("GeoJSON features size won't match.", situationIdsToFind.length, features);

        for (String id : situationIdsToFind) {
            assertTrue(String.format("Situation %s not found in situations", id),situations.stream().anyMatch(s -> s.getId().equals(id)));
            assertTrue(String.format("Situation %s not found in features", id), features.stream().anyMatch(f -> f.getProperties().situationId.equals(id)));
        }

        checkDatex2MatchJson(situationIncidents, featureIncidents);
        for (Situation s : situationIncidents) {
            assertTrue(String.format("Incident situation %s not found in features.", s.getId()),featureIncidents.stream().anyMatch(f -> f.getProperties().situationId.equals(s.getId())));
        }

        for (Situation s : situationWeightRestrictions) {
            assertTrue(String.format("Weight restrictions situation %s not found in features.", s.getId()),featureWeightRestrictions.stream().anyMatch(f -> f.getProperties().situationId.equals(s.getId())));
        }

        for (Situation s : situationRoadworks) {
            assertTrue(String.format("Roadwork situation %s not found in features.", s.getId()),featureRoadworks.stream().anyMatch(f -> f.getProperties().situationId.equals(s.getId())));
        }
    }

    private List<Situation> getSituations(final D2LogicalModel d2) {
        if (d2.getPayloadPublication() == null) {
            return Collections.emptyList();
        }
        final List<Situation> situations = ((SituationPublication) d2.getPayloadPublication()).getSituations();
        return situations != null ? situations : Collections.emptyList();
    }

    private void checkDatex2MatchJson(final List<Situation> situations, final List<TrafficAnnouncementFeature> features) {
        // Assert both contains each other
        for (Situation s : situations) {
            assertTrue(String.format("Situation %s was not found in features.",
                s.getId()), features.stream().anyMatch(f -> f.getProperties().situationId.equals(s.getId())));
        }
        for (TrafficAnnouncementFeature f : features) {
            assertTrue(String.format("Feature %s was not found in situations.",
                f.getProperties().situationId), situations.stream().anyMatch(s -> s.getId().equals(f.getProperties().situationId)));
        }
        // Check Datex2 vs Json content
        for (Situation s : situations) {
            Optional<TrafficAnnouncementFeature> feature =
                features.stream().filter(f -> f.getProperties().situationId.equals(s.getId())).findFirst();
            assertTrue(feature.isPresent());

            final TrafficAnnouncement announcement = feature.get().getProperties().announcements.get(0);
            final SituationRecord situationRecord = s.getSituationRecords().get(0);
            final String situationComment = situationRecord.getGeneralPublicComments().get(0).getComment().getValues().getValues().get(0).getValue();

            assertTrue(String.format("Feature title \"%s\" should exist in situation comment \"%s\"", announcement.title, situationComment),
                       situationComment.contains(announcement.title));
            assertEquals(announcement.timeAndDuration.startTime.toInstant(),
                         situationRecord.getValidity().getValidityTimeSpecification().getOverallStartTime());
        }
    }

    private JMSMessageListener<ExternalIMSMessage> createImsJmsMessageListener() {
        final JMSMessageListener.JMSDataUpdater<ExternalIMSMessage> dataUpdater = (data) ->  v2Datex2UpdateService.updateTrafficDatex2ImsMessages(data);
        return new JMSMessageListener<>(new ImsMessageMarshaller(jaxb2MarshallerimsJaxb2Marshaller), dataUpdater, false, log);
    }

    private void sendJmsMessage(final String resourceFilePath, JMSMessageListener<ExternalIMSMessage> messageListener) throws IOException {
        final String xmlImsMessage = readResourceContent("classpath:" + resourceFilePath);
        createAndSendJmsMessage(xmlImsMessage, messageListener);
    }

    private void sendJmsMessage(final ImsXmlVersion xmlVersion, final ImsJsonVersion jsonVersion,
                                final JMSMessageListener<ExternalIMSMessage> messageListener) throws IOException {
        final String xmlImsMessage = readImsMessageResourceContent(xmlVersion, jsonVersion);
        createAndSendJmsMessage(xmlImsMessage, messageListener);
    }

    private void createAndSendJmsMessage(final String xmlImsMessage, final JMSMessageListener<ExternalIMSMessage> messageListener) {
        messageListener.onMessage(createTextMessage(xmlImsMessage, getRandomId(1000, 9999).toString()));
    }
}
