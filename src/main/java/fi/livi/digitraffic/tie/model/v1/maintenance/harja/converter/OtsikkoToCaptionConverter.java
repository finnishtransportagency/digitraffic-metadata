package fi.livi.digitraffic.tie.model.v1.maintenance.harja.converter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.AutoRegisteredConverter;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.Caption;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.Sender;
import fi.livi.digitraffic.tie.external.harja.entities.OtsikkoSchema;
import fi.livi.digitraffic.tie.helper.DateHelper;

@ConditionalOnWebApplication
@Component
public class OtsikkoToCaptionConverter extends AutoRegisteredConverter<OtsikkoSchema, Caption> {

    @Override
    public Caption convert(final OtsikkoSchema src) {
        return new Caption(
            convert(src.getLahettaja(), Sender.class),
            src.getViestintunniste() != null ? src.getViestintunniste().getId() : null,
            DateHelper.toZonedDateTimeAtUtc(src.getLahetysaika()));
    }
}
