package fi.livi.digitraffic.tie.service.jms.marshaller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2MessageDto;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2SimpleMessageUpdater;
import fi.livi.digitraffic.tie.datex2.SituationPublication;

public class Datex2MessageMarshaller extends TextMessageMarshaller<Datex2MessageDto> {
    private static final Logger log = LoggerFactory.getLogger(Datex2MessageMarshaller.class);
    private final Datex2SimpleMessageUpdater datex2SimpleMessageUpdater;

    public Datex2MessageMarshaller(final Jaxb2Marshaller jaxb2Marshaller,
                                   final Datex2SimpleMessageUpdater datex2SimpleMessageUpdater) {
        super(jaxb2Marshaller);
        this.datex2SimpleMessageUpdater = datex2SimpleMessageUpdater;
    }

    @Override
    protected List<Datex2MessageDto> transform(final Object object, final String text) {
        final List<Datex2MessageDto> unmarshalled =
            datex2SimpleMessageUpdater.convert(text, Datex2MessageType.TRAFFIC_INCIDENT, null);
        log.info("method=transform situations {}", unmarshalled.stream()
            .map(d -> ((SituationPublication)d.model.getPayloadPublication()).getSituations().get(0).getId())
            .collect( Collectors.joining( ", " ) ));
        return unmarshalled;
    }
}
