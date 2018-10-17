package fi.livi.digitraffic.tie.data.model.json.maintenance.converter;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.json.maintenance.WorkMachine;
import fi.livi.digitraffic.tie.harja.Tyokone;

@Component
public class TyokoneToWorkMachineConverter extends AutoRegisteredConverter<Tyokone, WorkMachine> {

    @Override
    public WorkMachine convert(final Tyokone src) {
        return new WorkMachine(src.getId(), src.getTyokonetyyppi());
    }
}
