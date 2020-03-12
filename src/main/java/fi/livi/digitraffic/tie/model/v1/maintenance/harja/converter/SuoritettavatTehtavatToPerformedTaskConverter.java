package fi.livi.digitraffic.tie.model.v1.maintenance.harja.converter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.AutoRegisteredConverter;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.PerformedTask;
import fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat;

@ConditionalOnWebApplication
@Component
public class SuoritettavatTehtavatToPerformedTaskConverter extends AutoRegisteredConverter<SuoritettavatTehtavat, PerformedTask> {

    @Override
    public PerformedTask convert(final SuoritettavatTehtavat src) {
        return PerformedTask.fromValue(src.value());
    }
}
