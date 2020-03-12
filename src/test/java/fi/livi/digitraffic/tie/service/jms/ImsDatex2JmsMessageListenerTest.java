package fi.livi.digitraffic.tie.service.jms;

import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.TRAFFIC_INCIDENT;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.external.tloik.ims.ImsMessage;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeatureCollection;
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
    private Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    private V2Datex2UpdateService v2Datex2UpdateService;

    @Test
    public void datex2ReceiveMessages() throws IOException {
        datex2Repository.deleteAll();

        final String SITUATION_ID_1 = "GUID50001238";
        final JMSMessageListener datexJmsMessageListener = createImsJmsMessageListener();

        final List<Resource> imsResources = loadResources("classpath:tloik/ims/TrafficIncidentImsMessage.xml");
        readAndSendMessages(imsResources, datexJmsMessageListener);

        final D2LogicalModel active = v2Datex2DataService.findActive(0, TRAFFIC_INCIDENT);

        List<Situation> situations = ((SituationPublication) active.getPayloadPublication()).getSituations();

        assertCollectionSize(1, situations);
        assertEquals(SITUATION_ID_1, situations.get(0).getId());

        TrafficAnnouncementFeatureCollection activeJson =
            v2Datex2DataService.findActiveJson(0, TRAFFIC_INCIDENT);

        assertCollectionSize(1, activeJson.getFeatures());
        assertEquals(SITUATION_ID_1, activeJson.getFeatures().get(0).getProperties().situationId);
    }

    private JMSMessageListener createImsJmsMessageListener() {
        final JMSMessageListener.JMSDataUpdater<ImsMessage> dataUpdater = (data) ->  v2Datex2UpdateService.updateTrafficIncidentImsMessages(data);
        return new JMSMessageListener(new ImsMessageMarshaller(jaxb2Marshaller), dataUpdater, false, log);
    }

    private void readAndSendMessages(final List<Resource> imsResources, final JMSMessageListener messageListener) throws IOException {
        readAndSendMessages(imsResources, messageListener, null, null);
    }

    private void readAndSendMessages(final List<Resource> imsResources, final JMSMessageListener messageListener,
                                     final String  placeholderName, final String replacement) throws IOException {
        log.info("Read and send " + imsResources.size() + " IMS Datex2 messages...");
        for (final Resource datex2Resource : imsResources) {
            final File datex2file = datex2Resource.getFile();
            log.info("Datex2file={}", datex2file.getName());
            String content = FileUtils.readFileToString(datex2file, StandardCharsets.UTF_8);
            if (placeholderName != null && replacement != null) {
                log.info("Replace {} with {}", placeholderName, replacement);
                content = content.replace(placeholderName, replacement);
            }
            try {
                messageListener.onMessage(createTextMessage(content,
                                                            datex2file.getName()));
            } catch (final Exception e) {
                log.error("Error with file " + datex2file.getName());
                throw e;
            }
        }
    }
}
