package fi.livi.digitraffic.tie.model.v1.maintenance.harja.converter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.AutoRegisteredConverter;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.WorkMachine;
import fi.livi.digitraffic.tie.external.harja.Tyokone;

@ConditionalOnWebApplication
@Component
public class TyokoneToWorkMachineConverter extends AutoRegisteredConverter<Tyokone, WorkMachine> {

    @Override
    public WorkMachine convert(final Tyokone src) {
        return new WorkMachine(src.getId(), src.getTyokonetyyppi());
    }
}
