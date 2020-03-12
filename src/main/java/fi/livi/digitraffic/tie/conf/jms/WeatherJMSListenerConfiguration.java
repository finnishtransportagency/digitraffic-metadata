package fi.livi.digitraffic.tie.conf.jms;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import fi.ely.lotju.tiesaa.proto.TiesaaProtos;
import fi.livi.digitraffic.tie.service.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.service.jms.marshaller.WeatherMessageMarshaller;
import fi.livi.digitraffic.tie.service.LockingService;
import fi.livi.digitraffic.tie.service.v1.SensorDataUpdateService;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.weather.inQueue")
@Configuration
public class WeatherJMSListenerConfiguration extends AbstractJMSListenerConfiguration<TiesaaProtos.TiesaaMittatieto> {
    private static final Logger log = LoggerFactory.getLogger(WeatherJMSListenerConfiguration.class);

    private final JMSParameters jmsParameters;
    private final SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    public WeatherJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory") QueueConnectionFactory connectionFactory,
                                           @Value("${jms.userId}") final String jmsUserId, @Value("${jms.password}") final String jmsPassword,
                                           @Value("#{'${jms.weather.inQueue}'.split(',')}") final List<String> jmsQueueKey, final SensorDataUpdateService sensorDataUpdateService,
                                           LockingService lockingService) {

        super(connectionFactory,
              lockingService,
              log);
        this.sensorDataUpdateService = sensorDataUpdateService;

        jmsParameters = new JMSParameters(jmsQueueKey, jmsUserId, jmsPassword,
                                          WeatherJMSListenerConfiguration.class.getSimpleName(),
                                          LockingService.generateInstanceId());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }

    @Override
    public JMSMessageListener<TiesaaProtos.TiesaaMittatieto> createJMSMessageListener() {
        final JMSMessageListener.JMSDataUpdater<TiesaaProtos.TiesaaMittatieto> handleData = sensorDataUpdateService::updateWeatherData;
        final WeatherMessageMarshaller messageMarshaller = new WeatherMessageMarshaller();

        return new JMSMessageListener<>(messageMarshaller, handleData,
                                        isQueueTopic(jmsParameters.getJmsQueueKeys()),
                                        log);
    }
}
