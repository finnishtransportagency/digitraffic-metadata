package fi.livi.digitraffic.tie.model.v1.maintenance.harja.converter;

import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.AutoRegisteredConverter;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.Caption;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.ObservationFeature;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.ObservationFeatureCollection;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.WorkMachineTrackingRecord;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;

@ConditionalOnWebApplication
@Component
public class TyokoneenseurannanKirjausToWorkMachineTrackingRecordConverter
    extends AutoRegisteredConverter<TyokoneenseurannanKirjausRequestSchema, WorkMachineTrackingRecord> {

    @Override
    public WorkMachineTrackingRecord convert(final TyokoneenseurannanKirjausRequestSchema src) {

        final ObservationFeatureCollection ofc = new ObservationFeatureCollection(
            src.getHavainnot() == null ?
                Collections.emptyList() :
                src.getHavainnot().stream().map(havainnot -> convert(havainnot.getHavainto(), ObservationFeature.class))
                    .collect(Collectors.toList()));

        return new WorkMachineTrackingRecord(convert(src.getOtsikko(), Caption.class), ofc);
    }
}
