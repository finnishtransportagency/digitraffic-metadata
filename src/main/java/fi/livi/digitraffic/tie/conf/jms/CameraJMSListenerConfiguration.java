package fi.livi.digitraffic.tie.conf.jms;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import fi.livi.digitraffic.tie.data.jms.CameraJMSMessageListener;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.camera.enabled")
@Configuration
public class CameraJMSListenerConfiguration extends AbstractJMSListenerConfiguration<Kuva> {

    private static final Logger log = LoggerFactory.getLogger(CameraJMSMessageListener.class);
    private final JMSParameters jmsParameters;

    @Autowired
    public CameraJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory")
                                          QueueConnectionFactory connectionFactory,
                                          @Value("${jms.userId}")
                                          final String jmsUserId,
                                          @Value("${jms.password}")
                                          final String jmsPassword,
                                          @Value("${jms.camera.inQueue}")
                                          final String jmsQueueKey,
                                          CameraJMSMessageListener cameraJMSMessageListener,
                                          LockingService lockingService) {
        super(cameraJMSMessageListener,
              connectionFactory,
              lockingService,
              log);

        jmsParameters = new JMSParameters(jmsQueueKey, jmsUserId, jmsPassword,
                                          CameraJMSMessageListener.class.getSimpleName(),
                                          UUID.randomUUID().toString());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }
}