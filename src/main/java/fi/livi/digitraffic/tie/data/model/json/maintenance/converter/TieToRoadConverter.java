package fi.livi.digitraffic.tie.data.model.json.maintenance.converter;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.json.maintenance.Lane;
import fi.livi.digitraffic.tie.data.model.json.maintenance.Road;
import fi.livi.digitraffic.tie.harja.entities.Tie;
import fi.livi.digitraffic.tie.helper.DateHelper;

@Component
public class TieToRoadConverter extends AutoRegisteredConverter<Tie, Road> {

    @Override
    public Road convert(final Tie src) {
        return new Road(
            src.getNimi(),
            src.getNumero(),
            src.getAet(),
            src.getAosa(),
            src.getLet(),
            src.getLosa(),
            src.getAjr(),
            conversionService.convert(src.getKaista(), Lane.class),
            src.getPuoli(),
            DateHelper.toZonedDateTime(src.getAlkupvm()),
            DateHelper.toZonedDateTime(src.getLoppupvm()),
            DateHelper.toZonedDateTime(src.getKarttapvm()),
            src.getAdditionalProperties());
    }
}
