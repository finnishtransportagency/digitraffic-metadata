package fi.livi.digitraffic.tie.service.v1.tms;

import static fi.livi.digitraffic.tie.helper.DateHelper.getNewest;
import static fi.livi.digitraffic.tie.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.controller.TmsState;
import fi.livi.digitraffic.tie.converter.exception.NonPublicRoadStationException;
import fi.livi.digitraffic.tie.converter.feature.TmsStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.dao.v1.tms.TmsStationRepository;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.model.CalculatorDeviceType;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.TmsStationType;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.TmsStation;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.RoadDistrictService;
import fi.livi.digitraffic.tie.service.UpdateStatus;

@Service
public class TmsStationService extends AbstractTmsStationAttributeUpdater {
    private static final Logger log = LoggerFactory.getLogger(TmsStationService.class);

    private final TmsStationRepository tmsStationRepository;
    private final DataStatusService dataStatusService;
    private final RoadDistrictService roadDistrictService;
    private final TmsStationMetadata2FeatureConverter tmsStationMetadata2FeatureConverter;

    @Autowired
    public TmsStationService(final TmsStationRepository tmsStationRepository,
                             final DataStatusService dataStatusService,
                             final RoadDistrictService roadDistrictService,
                             final TmsStationMetadata2FeatureConverter tmsStationMetadata2FeatureConverter) {
        this.tmsStationRepository = tmsStationRepository;
        this.dataStatusService = dataStatusService;
        this.roadDistrictService = roadDistrictService;
        this.tmsStationMetadata2FeatureConverter = tmsStationMetadata2FeatureConverter;
    }

    @Transactional(readOnly = true)
    public TmsStationFeatureCollection findAllPublishableTmsStationsAsFeatureCollection(final boolean onlyUpdateInfo, final TmsState tmsState) {
        final List<TmsStation> stations = onlyUpdateInfo ? Collections.emptyList() : findPublishableStations(tmsState);

        return tmsStationMetadata2FeatureConverter.convert(
            stations,
            getMetadataLastUpdated(),
            getMetadataLastChecked());
    }

    @Transactional(readOnly = true)
    public TmsStationFeatureCollection listTmsStationsByRoadNumber(final Integer roadNumber, final TmsState tmsState) {
        final List<TmsStation> stations = findPublishableStations(roadNumber, tmsState);

        return tmsStationMetadata2FeatureConverter.convert(
            stations,
            getMetadataLastUpdated(),
            getMetadataLastChecked());
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


    @Transactional
    public List<TmsStation> findAllPublishableTmsStations() {
        return tmsStationRepository.findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsMappedByByTmsNaturalId() {
        final List<TmsStation> allStations = tmsStationRepository.findAll();

        return allStations.stream().collect(Collectors.toMap(TmsStation::getNaturalId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsMappedByByLotjuId() {
        final List<TmsStation> allStations = tmsStationRepository.findAll();

        return allStations.stream().collect(Collectors.toMap(TmsStation::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public List<Long> findAllTmsStationsLotjuIds() {
        return tmsStationRepository.findAllTmsStationsLotjuIds();
    }

    @Transactional(readOnly = true)
    public Map<Long, TmsStation> findAllTmsStationsByMappedByLotjuId() {
        final List<TmsStation> all = tmsStationRepository.findAll();
        return all.stream().collect( Collectors.toMap(TmsStation::getLotjuId, Function.identity()));
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

    @Transactional
    public UpdateStatus updateOrInsertTmsStation(LamAsemaVO lam) {
        TmsStation existingTms = findTmsStationByLotjuId(lam.getId());

        if (existingTms != null) {
            final int hash = HashCodeBuilder.reflectionHashCode(existingTms);
            final String before = ReflectionToStringBuilder.toString(existingTms);

            if ( updateTmsStationAttributes(lam, existingTms) ||
                hash != HashCodeBuilder.reflectionHashCode(existingTms) ) {
                log.info("Updated:\n{} ->\n{}", before, ReflectionToStringBuilder.toString(existingTms));
                return UpdateStatus.UPDATED;
            }
            return UpdateStatus.NOT_UPDATED;
        } else {
            final TmsStation newTms = new TmsStation();
            newTms.setRoadStation(RoadStation.createTmsStation());
            updateTmsStationAttributes(lam, newTms);
            tmsStationRepository.save(newTms);

            log.info("Created new newTmsCount={}", newTms);
            return UpdateStatus.INSERTED;
        }
    }

    private List<TmsStation> findPublishableStations(final Integer roadNumber, final TmsState tmsState) {
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

    private List<TmsStation> findPublishableStations(final TmsState tmsState) {
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

    private boolean updateTmsStationAttributes(final LamAsemaVO from, final TmsStation to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);
        to.setNaturalId(convertToTmsNaturalId(from.getVanhaId()));
        to.setLotjuId(from.getId());

        to.setName(from.getNimi());
        to.setDirection1Municipality(from.getSuunta1Kunta());
        to.setDirection1MunicipalityCode(from.getSuunta1KuntaKoodi());
        to.setDirection2Municipality(from.getSuunta2Kunta());
        to.setDirection2MunicipalityCode(from.getSuunta2KuntaKoodi());
        to.setTmsStationType(TmsStationType.convertFromLamasemaTyyppi(from.getTyyppi()));
        to.setCalculatorDeviceType(CalculatorDeviceType.convertFromLaiteTyyppi(from.getLaskinlaite()));

        final Integer roadNaturalId = from.getTieosoite() != null ? from.getTieosoite().getTienumero() : null;
        final Integer roadSectionNaturalId = from.getTieosoite() != null ? from.getTieosoite().getTieosa() : null;

        if (roadNaturalId != null && roadSectionNaturalId != null) {
            to.setRoadDistrict(roadDistrictService.findByRoadSectionAndRoadNaturalId(roadSectionNaturalId, roadNaturalId));
            if (to.getRoadDistrict() == null && !isPermanentlyDeletedKeruunTila(from.getKeruunTila())) {
                log.warn("Could not find RoadDistrict with roadSectionNaturalId={}, roadNaturalId={} for {}",
                         roadSectionNaturalId, roadNaturalId, ToStringHelper.toString(from));
            }
        } else {
            to.setRoadDistrict(null);
        }

        // Update RoadStation
        final boolean updated = updateRoadStationAttributes(from, to.getRoadStation());
        to.setObsoleteDate(to.getRoadStation().getObsoleteDate());

        return updated ||
            hash != HashCodeBuilder.reflectionHashCode(to);
    }

    private TmsStation findTmsStationByLotjuId(final Long lotjuId) {
        return tmsStationRepository.findByLotjuId(lotjuId);
    }

    private static Long convertToTmsNaturalId(final Integer roadStationVanhaId) {
        return roadStationVanhaId == null ? null : roadStationVanhaId - 23000L;
    }

    private TmsStationFeature convert(final Long id, final TmsStation station) throws NonPublicRoadStationException {
        if(station == null) {
            throw new ObjectNotFoundException(TmsStation.class, id);
        }

        return tmsStationMetadata2FeatureConverter.convert(station);
    }

    ZonedDateTime getMetadataLastUpdated() {
        final ZonedDateTime sensorsUpdated = dataStatusService.findDataUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA);
        final ZonedDateTime stationsUpdated = dataStatusService.findDataUpdatedTime(DataType.TMS_STATION_METADATA);
        return getNewest(sensorsUpdated, stationsUpdated);
    }

    private ZonedDateTime getMetadataLastChecked() {
        final ZonedDateTime sensorsUpdated = dataStatusService.findDataUpdatedTime(DataType.TMS_STATION_SENSOR_METADATA_CHECK);
        final ZonedDateTime stationsUpdated = dataStatusService.findDataUpdatedTime(DataType.TMS_STATION_METADATA_CHECK);
        return getNewest(sensorsUpdated, stationsUpdated);
    }
}
