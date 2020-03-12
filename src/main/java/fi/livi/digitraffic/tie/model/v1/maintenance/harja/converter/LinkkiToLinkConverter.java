package fi.livi.digitraffic.tie.model.v1.maintenance.harja.converter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.AutoRegisteredConverter;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.Link;
import fi.livi.digitraffic.tie.external.harja.entities.LinkkisijaintiSchema;

@ConditionalOnWebApplication
@Component
public class LinkkiToLinkConverter extends AutoRegisteredConverter<LinkkisijaintiSchema, Link> {

    @Override
    public Link convert(final LinkkisijaintiSchema src) {
        return new Link(src.getId(), src.getMarvo());
    }
}
