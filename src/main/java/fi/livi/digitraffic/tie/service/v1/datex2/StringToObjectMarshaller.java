package fi.livi.digitraffic.tie.service.v1.datex2;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

@Component
public class StringToObjectMarshaller<T> {
    private final Jaxb2Marshaller jaxb2Marshaller;

    public StringToObjectMarshaller(final Jaxb2Marshaller jaxb2Marshaller) {
        this.jaxb2Marshaller = jaxb2Marshaller;
    }

    public T convertToObject(final String xmlSting) {
        // Trim empty control before and after xml-declaration as they are not allowed
        final Object object = jaxb2Marshaller.unmarshal(new StringSource(StringUtils.trim(xmlSting)));
        if (object instanceof JAXBElement) {
            return (T)((JAXBElement) object).getValue();
        }
        return (T)object;
    }

    public <T> String convertToString(final T object) {
        final StringResult result = new StringResult();

        jaxb2Marshaller.marshal(object, result);

        return result.toString();
    }
}
