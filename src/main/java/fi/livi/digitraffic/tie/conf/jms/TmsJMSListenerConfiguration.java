package fi.livi.digitraffic.tie.conf.jms;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import fi.ely.lotju.lam.proto.LAMRealtimeProtos;
import fi.livi.digitraffic.tie.service.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.service.jms.marshaller.TmsMessageMarshaller;
import fi.livi.digitraffic.tie.service.LockingService;
import fi.livi.digitraffic.tie.service.v1.SensorDataUpdateService;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.tms.inQueue")
@Configuration
public class    TmsJMSListenerConfiguration extends AbstractJMSListenerConfiguration<LAMRealtimeProtos.Lam> {
    private static final Logger log = LoggerFactory.getLogger(TmsJMSListenerConfiguration.class);

    private final JMSParameters jmsParameters;
    private final SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    public TmsJMSListenerConfiguration(final @Qualifier("sonjaJMSConnectionFactory") QueueConnectionFactory connectionFactory,
                                       final @Value("${jms.userId}") String jmsUserId,
                                       final @Value("${jms.password}") String jmsPassword,
                                       final @Value("#{'${jms.tms.inQueue}'.split(',')}") List<String> jmsQueueKeys,
                                       final SensorDataUpdateService sensorDataUpdateService,
                                       final LockingService lockingService) {

        super(connectionFactory,
              lockingService,
              log);
        this.sensorDataUpdateService = sensorDataUpdateService;
        jmsParameters = new JMSParameters(jmsQueueKeys, jmsUserId, jmsPassword,
                                          TmsJMSListenerConfiguration.class.getSimpleName(),
                                          LockingService.generateInstanceId());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }

    @Override
    public JMSMessageListener<LAMRealtimeProtos.Lam> createJMSMessageListener() throws JAXBException {
        final JMSMessageListener.JMSDataUpdater<LAMRealtimeProtos.Lam> handleData = sensorDataUpdateService::updateLamData;
        final TmsMessageMarshaller messageMarshaller = new TmsMessageMarshaller();

        return new JMSMessageListener<>(messageMarshaller, handleData,
                                        isQueueTopic(jmsParameters.getJmsQueueKeys()),
                                        log);
    }
}
