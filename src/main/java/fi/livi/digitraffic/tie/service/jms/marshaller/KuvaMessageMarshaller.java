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

import fi.ely.lotju.kamera.proto.KuvaProtos;
import fi.livi.digitraffic.tie.service.jms.JMSMessageListener;

public class KuvaMessageMarshaller implements JMSMessageListener.MessageMarshaller<KuvaProtos.Kuva> {
    private static final Logger log = LoggerFactory.getLogger(KuvaMessageMarshaller.class);

    @Override
    public List<KuvaProtos.Kuva> unmarshalMessage(Message message) throws JMSException {
        return unmarshallBytes((BytesMessage) message);
    }

    private List<KuvaProtos.Kuva> unmarshallBytes(final BytesMessage message) throws JMSException {
        final int bodyLength = (int) message.getBodyLength();
        final byte[] bytes = new byte[bodyLength];
        message.readBytes(bytes);

        final List<KuvaProtos.Kuva> kuvaList = new ArrayList<>();

        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

            while(bais.available() > 0) {
                final KuvaProtos.Kuva kuva = KuvaProtos.Kuva.parseDelimitedFrom(bais);

                kuvaList.add(kuva);
            }
        } catch (final IOException e) {
            log.error("Exception while parsing", e);
        }

        return kuvaList;
    }
}
