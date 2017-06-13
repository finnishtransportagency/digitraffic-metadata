package fi.livi.digitraffic.tie.conf.jms;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.data.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.data.service.CameraDataUpdateService;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.camera.enabled")
@Configuration
public class CameraJMSListenerConfiguration extends AbstractJMSListenerConfiguration<Kuva> {

    private static final Logger log = LoggerFactory.getLogger(CameraJMSListenerConfiguration.class);
    private final JMSParameters jmsParameters;
    private final CameraDataUpdateService cameraDataUpdateService;
    private final Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    public CameraJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory") QueueConnectionFactory connectionFactory,
                                          @Value("${jms.userId}") final String jmsUserId,
                                          @Value("${jms.password}") final String jmsPassword,
                                          @Value("${jms.camera.inQueue}") final String jmsQueueKey,
                                          final CameraDataUpdateService cameraDataUpdateService,
                                          final LockingService lockingService,
                                          final Jaxb2Marshaller jaxb2Marshaller) {
        super(connectionFactory,
              lockingService,
              log);
        this.cameraDataUpdateService = cameraDataUpdateService;
        this.jaxb2Marshaller = jaxb2Marshaller;

        jmsParameters = new JMSParameters(jmsQueueKey, jmsUserId, jmsPassword,
                                          CameraJMSListenerConfiguration.class.getSimpleName(),
                                          UUID.randomUUID().toString());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }

    @Override
    public JMSMessageListener<Kuva> createJMSMessageListener() throws JAXBException {

        final JMSMessageListener.JMSDataUpdater<Kuva> handleData = (List<Pair<Kuva, String>> data) -> {
            try {
                final List<Kuva> kuvaData = data.stream().map(Pair::getLeft).collect(Collectors.toList());
                return cameraDataUpdateService.updateCameraData(kuvaData);
            } catch (SQLException e) {
                log.error("Error while handling Camera data", e);
                return 0;
            }
        };

        return new JMSMessageListener<>(jaxb2Marshaller,
                                        handleData,
                                        isQueueTopic(jmsParameters.getJmsQueueKey()),
                                        log);
    }
}
