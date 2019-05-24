package fi.livi.digitraffic.tie.metadata.service.tms;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.lotju.xsd.datex2.TmsStationDatex2Response;
import fi.livi.digitraffic.tie.metadata.controller.TmsState;
import fi.livi.digitraffic.tie.metadata.converter.TmsStationMetadata2Datex2Converter;
import fi.livi.digitraffic.tie.metadata.dao.tms.TmsStationDatex2Repository;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;

@ConditionalOnWebApplication
@Service
public class TmsStationDatex2Service {

    private final TmsStationDatex2Repository tmsStationDatex2Repository;
    private final TmsStationMetadata2Datex2Converter tmsStationMetadata2Datex2Converter;
    private final TmsStationService tmsStationService;

    public TmsStationDatex2Service(final TmsStationDatex2Repository tmsStationDatex2Repository,
                                   final TmsStationMetadata2Datex2Converter tmsStationMetadata2Datex2Converter,
                                   final TmsStationService tmsStationService) {
        this.tmsStationDatex2Repository = tmsStationDatex2Repository;
        this.tmsStationMetadata2Datex2Converter = tmsStationMetadata2Datex2Converter;
        this.tmsStationService = tmsStationService;
    }

    @Transactional(readOnly = true)
    public TmsStationDatex2Response findAllPublishableTmsStationsAsDatex2(final TmsState tmsState) {
        final List<TmsStation> stations = findStations(tmsState);

        return new TmsStationDatex2Response().withD2LogicalModel(
            tmsStationMetadata2Datex2Converter.convert(stations,tmsStationService.getMetadataLastUpdated()));
    }

    @Transactional(readOnly = true)
    public List<TmsStation> findAllPublishableTmsStations() {
        return tmsStationDatex2Repository.findDistinctByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();
    }

    private List<TmsStation> findStations(final TmsState tmsState) {

        switch(tmsState) {
        case ACTIVE:
            return tmsStationDatex2Repository.findDistinctByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();
        case REMOVED:
            return tmsStationDatex2Repository
                .findDistinctByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByRoadStation_NaturalId(CollectionStatus.REMOVED_PERMANENTLY);
        case ALL:
            return tmsStationDatex2Repository.findDistinctByRoadStationIsPublicIsTrueOrderByRoadStation_NaturalId();
        default:
            throw new IllegalArgumentException();
        }
    }
}
