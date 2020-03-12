package fi.livi.digitraffic.tie.service.jms.marshaller;

import static fi.livi.digitraffic.tie.service.CameraMetadataUpdatedMessageDto.EntityType;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.lotju.xsd.metatietomuutos.kameratietovirta.Metatietomuutos;
import fi.livi.digitraffic.tie.service.CameraMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.MetadataUpdatedMessageDto.UpdateType;

public class CameraMetadataUpdatedMessageMarshaller extends TextMessageMarshaller<CameraMetadataUpdatedMessageDto> {
    private static final Logger log = LoggerFactory.getLogger(CameraMetadataUpdatedMessageMarshaller.class);

    public CameraMetadataUpdatedMessageMarshaller(final Jaxb2Marshaller jaxb2Marshaller) {
        super(jaxb2Marshaller);
    }

    @Override
    protected List<CameraMetadataUpdatedMessageDto> transform(final Object object, final String text) {
        log.debug("method=transform text={} object={}", text, ToStringHelper.toStringFull(object));
        final Metatietomuutos muutos = (Metatietomuutos) object;
        final CameraMetadataUpdatedMessageDto dto =
            new CameraMetadataUpdatedMessageDto(muutos.getId(),
                                                new HashSet<>(muutos.getAsemat().getId()),
                                                UpdateType.fromNameValue(muutos.getTyyppi()),
                                                DateHelper.toInstant(muutos.getAika()),
                                                EntityType.fromNameValue(muutos.getEntiteetti()));
        return Collections.singletonList(dto);
    }
}
