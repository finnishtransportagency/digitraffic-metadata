package fi.livi.digitraffic.tie.metadata.service.tms;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.metadata.controller.TmsState;
import fi.livi.digitraffic.tie.metadata.converter.NonPublicRoadStationException;
import fi.livi.digitraffic.tie.metadata.converter.TmsStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.tms.TmsStationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class TmsStationService {
    private static final Logger log = LoggerFactory.getLogger(TmsStationService.class);
    private final TmsStationRepository tmsStationRepository;
    private final StaticDataStatusService staticDataStatusService;
    private final TmsStationMetadata2FeatureConverter tmsStationMetadata2FeatureConverter;

    @Autowired
    public TmsStationService(final TmsStationRepository tmsStationRepository,
                             final StaticDataStatusService staticDataStatusService,
                             final TmsStationMetadata2FeatureConverter tmsStationMetadata2FeatureConverter) {
        this.tmsStationRepository = tmsStationRepository;
        this.staticDataStatusService = staticDataStatusService;
        this.tmsStationMetadata2FeatureConverter = tmsStationMetadata2FeatureConverter;
    }

    @Transactional(readOnly = true)
    public TmsStationFeatureCollection findAllPublishableTmsStationsAsFeatureCollection(final boolean onlyUpdateInfo,
        final TmsState tmsState) {
        final MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.LAM_STATION);
        final List<TmsStation> stations = findStations(onlyUpdateInfo, tmsState);

        return tmsStationMetadata2FeatureConverter.convert(
                stations,
                updated != null ? updated.getUpdatedTime() : null);
    }

    private List<TmsStation> findStations(final boolean onlyUpdateInfo, final TmsState tmsState) {
        if(onlyUpdateInfo) {
            return Collections.emptyList();
        }

        switch(tmsState) {
            case ACTIVE:
                return tmsStationRepository.findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();
            case REMOVED:
                return tmsStationRepository.findByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByRoadStation_NaturalId
                    (CollectionStatus.REMOVED_PERMANENTLY);
            case ALL:
                return tmsStationRepository.findByRoadStationIsPublicIsTrueOrderByRoadStation_NaturalId();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Transactional(readOnly = true)
    public TmsStationFeatureCollection listTmsStationsByRoadNumber(final Integer roadNumber, final TmsState tmsState) {
        final MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.LAM_STATION);
        final List<TmsStation> stations = findStations(roadNumber, tmsState);

        return tmsStationMetadata2FeatureConverter.convert(
            stations,
            updated != null ? updated.getUpdatedTime() : null);
    }

    private List<TmsStation> findStations(final Integer roadNumber, final TmsState tmsState) {
        switch(tmsState) {
            case ACTIVE:
                return tmsStationRepository
                    .findByRoadStationPublishableIsTrueAndRoadStationRoadAddressRoadNumberIsOrderByRoadStation_NaturalId(roadNumber);
            case REMOVED:
                return tmsStationRepository
                    .findByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsAndRoadStationRoadAddressRoadNumberIsOrderByRoadStation_NaturalId
                    (CollectionStatus.REMOVED_PERMANENTLY, roadNumber);
            case ALL:
                return tmsStationRepository
                    .findByRoadStationIsPublicIsTrueAndRoadStationRoadAddressRoadNumberIsOrderByRoadStation_NaturalId(roadNumber);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Transactional(readOnly = true)
    public TmsStationFeature getTmsStationByRoadStationId(final Long roadStationId) throws NonPublicRoadStationException {
        final TmsStation station = tmsStationRepository.findByRoadStationIsPublicIsTrueAndRoadStation_NaturalId(roadStationId);

        return convert(roadStationId, station);
    }

    @Transactional(readOnly = true)
    public TmsStationFeature getTmsStationByLamId(final Long lamId) throws NonPublicRoadStationException {
        return convert(lamId, tmsStationRepository.findByRoadStationIsPublicIsTrueAndNaturalId(lamId));
    }

    private TmsStationFeature convert(final Long id, final TmsStation station) throws NonPublicRoadStationException {
        if(station == null) {
            throw new ObjectNotFoundException(TmsStation.class, id);
        }

        return tmsStationMetadata2FeatureConverter.convert(station);

    }

    @Transactional
    public List<TmsStation> findAllPublishableTmsStations() {
        return tmsStationRepository.findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();
    }

    @Transactional
    public TmsStation save(final TmsStation tmsStation) {
        try {
            final TmsStation tms = tmsStationRepository.save(tmsStation);
            tmsStationRepository.flush();
            return tms;
        } catch (Exception e) {
            log.error("Could not save " + tmsStation);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsMappedByByTmsNaturalId() {
        final List<TmsStation> allStations = tmsStationRepository.findAll();

        return allStations.stream().collect(Collectors.toMap(TmsStation::getNaturalId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsMappedByByLotjuId() {
        final List<TmsStation> allStations = tmsStationRepository.findAll();

        return allStations.stream().filter(tms -> tms.getLotjuId() != null).collect(Collectors.toMap(TmsStation::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsByMappedByLotjuId() {
        final Map<Long, TmsStation> map = new HashMap<>();
        final List<TmsStation> all = tmsStationRepository.findAll();
        for (final TmsStation tmsStation : all) {
            if (tmsStation.getLotjuId() != null) {
                map.put(tmsStation.getLotjuId(), tmsStation);
            } else {
                log.warn("Null lotjuId: " + tmsStation);
            }
        }
        return map;
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllPublishableTmsStationsMappedByLotjuId() {
        final List<TmsStation> all = findAllPublishableTmsStations();

        return all.stream().collect(Collectors.toMap(TmsStation::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public TmsStation findPublishableTmsStationByRoadStationNaturalId(long roadStationNaturalId) {
        final TmsStation entity = tmsStationRepository.findByRoadStation_NaturalIdAndRoadStationPublishableIsTrue(roadStationNaturalId);

        if (entity == null) {
            throw new ObjectNotFoundException(TmsStation.class, roadStationNaturalId);
        }

        return entity;
    }

    @Transactional(readOnly = true)
    public boolean tmsStationExistsWithRoadStationNaturalId(long roadStationNaturalId) {
        return tmsStationRepository.tmsExistsWithRoadStationNaturalId(roadStationNaturalId);
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsWithoutLotjuIdMappedByTmsNaturalId() {
        final List<TmsStation> all = tmsStationRepository.findByLotjuIdIsNull();

        return all.stream().collect(Collectors.toMap(TmsStation::getNaturalId, Function.identity()));
    }
}
