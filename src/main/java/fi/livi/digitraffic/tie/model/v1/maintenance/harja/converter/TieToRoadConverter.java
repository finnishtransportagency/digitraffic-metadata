package fi.livi.digitraffic.tie.model.v1.maintenance.harja.converter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.AutoRegisteredConverter;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.Lane;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.Road;
import fi.livi.digitraffic.tie.external.harja.entities.TiesijaintiSchema;
import fi.livi.digitraffic.tie.helper.DateHelper;

@ConditionalOnWebApplication
@Component
public class TieToRoadConverter extends AutoRegisteredConverter<TiesijaintiSchema, Road> {

    @Override
    public Road convert(final TiesijaintiSchema src) {
        return new Road(
            src.getNimi(),
            src.getNumero(),
            src.getAet(),
            src.getAosa(),
            src.getLet(),
            src.getLosa(),
            src.getAjr(),
            convert(src.getKaista(), Lane.class),
            src.getPuoli(),
            DateHelper.toZonedDateTimeAtUtc(src.getAlkupvm()),
            DateHelper.toZonedDateTimeAtUtc(src.getLoppupvm()),
            DateHelper.toZonedDateTimeAtUtc(src.getKarttapvm()),
            src.getAdditionalProperties());
    }
}
