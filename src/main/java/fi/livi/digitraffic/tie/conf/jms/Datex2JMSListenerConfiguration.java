package fi.livi.digitraffic.tie.conf.jms;

import java.util.UUID;
import javax.jms.JMSException;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import fi.livi.digitraffic.tie.conf.jms.listener.Datex2JMSMessageListener;
import fi.livi.digitraffic.tie.data.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.data.service.Datex2DataService;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

@ConditionalOnProperty(name = "jms.datex2.enabled")
@Configuration
public class Datex2JMSListenerConfiguration extends AbstractJMSListenerConfiguration<D2LogicalModel, Pair<D2LogicalModel, String>> {
    private static final Logger log = LoggerFactory.getLogger(Datex2JMSListenerConfiguration.class);
    private final JMSParameters jmsParameters;
    private final Datex2DataService datex2DataService;

    @Autowired
    public Datex2JMSListenerConfiguration(@Value("${jms.datex2.connectionUrls}")
                                          final String jmsConnectionUrls,
                                          @Value("${jms.datex2.userId}")
                                          final String jmsUserId,
                                          @Value("${jms.datex2.password}")
                                          final String jmsPassword,
                                          @Value("${jms.datex2.inQueue}")
                                          final String jmsQueueKey,
                                          final Datex2DataService datex2DataService,
                                          final LockingService lockingService) throws JMSException {

        super(JMSConfiguration.createQueueConnectionFactory(jmsConnectionUrls),
              lockingService,
              log);
        this.datex2DataService = datex2DataService;

        jmsParameters = new JMSParameters(jmsQueueKey, jmsUserId, jmsPassword,
                                          Datex2JMSListenerConfiguration.class.getSimpleName(),
                                          UUID.randomUUID().toString());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }

    @Override
    public Datex2JMSMessageListener createJMSMessageListener() throws JAXBException {
        final JMSMessageListener.JMSDataUpdater<Pair<D2LogicalModel, String>> handleData = datex2DataService::updateDatex2Data;

        return new Datex2JMSMessageListener(handleData,
                                        isQueueTopic(jmsParameters.getJmsQueueKey()),
                                        log);
    }

}
