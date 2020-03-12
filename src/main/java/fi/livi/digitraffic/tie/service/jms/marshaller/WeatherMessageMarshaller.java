package fi.livi.digitraffic.tie.service.jms.marshaller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.ely.lotju.tiesaa.proto.TiesaaProtos;
import fi.livi.digitraffic.tie.service.jms.JMSMessageListener;

public class WeatherMessageMarshaller implements JMSMessageListener.MessageMarshaller<TiesaaProtos.TiesaaMittatieto> {
    private static final Logger log = LoggerFactory.getLogger(WeatherMessageMarshaller.class);

    @Override
    public List<TiesaaProtos.TiesaaMittatieto> unmarshalMessage(Message message) throws JMSException {
        return unmarshallBytes((BytesMessage) message);
    }

    private List<TiesaaProtos.TiesaaMittatieto> unmarshallBytes(final BytesMessage message) throws JMSException {
        final int bodyLength = (int) message.getBodyLength();
        final byte[] bytes = new byte[bodyLength];
        message.readBytes(bytes);

        final List<TiesaaProtos.TiesaaMittatieto> weatherList = new ArrayList<>();

        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

            while(bais.available() > 0) {
                final TiesaaProtos.TiesaaMittatieto weather = TiesaaProtos.TiesaaMittatieto.parseDelimitedFrom(bais);

                weatherList.add(weather);
            }
        } catch (final IOException e) {
            log.error("Exception while parsing", e);
        }

        return weatherList;
    }
}
