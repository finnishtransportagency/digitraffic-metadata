package fi.livi.digitraffic.tie.conf.jms;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.service.CameraMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.LockingService;
import fi.livi.digitraffic.tie.service.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.service.jms.marshaller.CameraMetadataUpdatedMessageMarshaller;
import fi.livi.digitraffic.tie.service.v1.camera.CameraMetadataMessageHandler;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.camera.meta.inQueue")
@Configuration
public class CameraMetadataJMSListenerConfiguration extends AbstractJMSListenerConfiguration<CameraMetadataUpdatedMessageDto> {
    private static final Logger log = LoggerFactory.getLogger(CameraMetadataJMSListenerConfiguration.class);
    private final JMSParameters jmsParameters;
    private final CameraMetadataMessageHandler cameraMetadataMessageHandler;
    private final Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    public CameraMetadataJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory") QueueConnectionFactory connectionFactory,
                                                  @Value("${jms.userId}") final String jmsUserId,
                                                  @Value("${jms.password}") final String jmsPassword,
                                                  @Value("#{'${jms.camera.meta.inQueue}'.split(',')}")final List<String> jmsQueueKeys,
                                                  final CameraMetadataMessageHandler cameraMetadataMessageHandler,
                                                  final LockingService lockingService,
                                                  final Jaxb2Marshaller kameraMetadataJaxb2Marshaller) {
        super(connectionFactory, lockingService, log);
        this.cameraMetadataMessageHandler = cameraMetadataMessageHandler;
        this.jaxb2Marshaller = kameraMetadataJaxb2Marshaller;

        jmsParameters = new JMSParameters(jmsQueueKeys, jmsUserId, jmsPassword,
            CameraMetadataJMSListenerConfiguration.class.getSimpleName(),
            LockingService.generateInstanceId());
        }

        @Override
        public JMSParameters getJmsParameters() {
            return jmsParameters;
        }

        @Override
        public JMSMessageListener<CameraMetadataUpdatedMessageDto> createJMSMessageListener() {
            final JMSMessageListener.JMSDataUpdater<CameraMetadataUpdatedMessageDto> handleData = cameraMetadataMessageHandler::updateCameraMetadata;
            final CameraMetadataUpdatedMessageMarshaller messageMarshaller = new CameraMetadataUpdatedMessageMarshaller(jaxb2Marshaller);

            return new JMSMessageListener(messageMarshaller, handleData,
                                          isQueueTopic(jmsParameters.getJmsQueueKeys()),
                                          log);
        }
}
