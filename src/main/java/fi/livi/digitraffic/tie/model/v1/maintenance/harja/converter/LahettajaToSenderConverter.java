package fi.livi.digitraffic.tie.model.v1.maintenance.harja.converter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.AutoRegisteredConverter;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.Organisation;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.Sender;
import fi.livi.digitraffic.tie.external.harja.entities.Lahettaja;

@ConditionalOnWebApplication
@Component
public class LahettajaToSenderConverter extends AutoRegisteredConverter<Lahettaja, Sender> {

    @Override
    public Sender convert(final Lahettaja src) {
        return new Sender(
            src.getJarjestelma(),
            convert(src.getOrganisaatio(), Organisation.class));
    }
}
