package fi.livi.digitraffic.tie.service.jms.marshaller;

import java.util.Collections;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.service.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.helper.ToStringHelper;

public class TextMessageMarshaller<K> implements JMSMessageListener.MessageMarshaller<K> {
    private static final Logger log = LoggerFactory.getLogger(TextMessageMarshaller.class);

    private final Jaxb2Marshaller jaxb2Marshaller;

    public TextMessageMarshaller(final Jaxb2Marshaller jaxb2Marshaller) {
        this.jaxb2Marshaller = jaxb2Marshaller;
    }

    @Override
    public List<K> unmarshalMessage(final Message message) throws JMSException {
        assertTextMessage(message);

        final String text = parseTextMessageText((TextMessage) message);

        Object object = jaxb2Marshaller.unmarshal(new StringSource(text));
        if (object instanceof JAXBElement) {
            // For Datex2 messages extra stuff
            object = ((JAXBElement) object).getValue();
        }

        return transform(object, text);
    }

    protected List<K> transform(final Object object, final String text) {
        return Collections.singletonList((K) object);
    }

    private String parseTextMessageText(final TextMessage message) throws JMSException {
        assertTextMessage(message);

        final String text = message.getText();
        if (StringUtils.isBlank(text)) {
            log.error(JMSMessageListener.MESSAGE_UNMARSHALLING_ERROR_FOR_MESSAGE, ToStringHelper.toStringFull(message));
            throw new JMSException(JMSMessageListener.MESSAGE_UNMARSHALLING_ERROR + ": blank text");
        }
        return text.trim();
    }

    private void assertTextMessage(final Message message) {
        if (!(message instanceof TextMessage)) {
            log.error(JMSMessageListener.MESSAGE_UNMARSHALLING_ERROR_FOR_MESSAGE, ToStringHelper.toStringFull(message));
            throw new IllegalArgumentException("Unknown message type: " + message.getClass());
        }
    }

}
