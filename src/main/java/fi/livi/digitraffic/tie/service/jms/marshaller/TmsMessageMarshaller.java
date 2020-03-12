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

import fi.ely.lotju.lam.proto.LAMRealtimeProtos;
import fi.livi.digitraffic.tie.service.jms.JMSMessageListener;

public class TmsMessageMarshaller implements JMSMessageListener.MessageMarshaller<LAMRealtimeProtos.Lam> {
    private static final Logger log = LoggerFactory.getLogger(TmsMessageMarshaller.class);

    private List<LAMRealtimeProtos.Lam> getObjectFromBytes(final byte[] body) {
        final List<LAMRealtimeProtos.Lam> lamList = new ArrayList<>();

        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(body);

            while(bais.available() > 0) {
                final LAMRealtimeProtos.Lam lam = LAMRealtimeProtos.Lam.parseDelimitedFrom(bais);

                lamList.add(lam);
            }
        } catch (final IOException e) {
            log.error("Exception while parsing", e);
        }

        return lamList;
    }

    @Override
    public List<LAMRealtimeProtos.Lam> unmarshalMessage(final Message message) throws JMSException {
        return unmarshallBytes((BytesMessage) message);
    }

    private List<LAMRealtimeProtos.Lam> unmarshallBytes(final BytesMessage message) throws JMSException {
        final int bodyLength = (int) message.getBodyLength();
        final byte[] bytes = new byte[bodyLength];

        message.readBytes(bytes);

        return getObjectFromBytes(bytes);
    }

}
