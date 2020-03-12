package fi.livi.digitraffic.tie.service;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

import fi.livi.digitraffic.tie.dao.v1.RoadStationRepository;
import fi.livi.digitraffic.tie.dao.v1.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.dao.v1.RoadStationSensorValueDtoRepository;
import fi.livi.digitraffic.tie.dao.v1.SensorValueRepository;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import fi.livi.digitraffic.tie.dto.v1.TmsRoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.dto.v1.WeatherRoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.helper.DataValidityHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.VehicleClass;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;
import fi.livi.digitraffic.tie.model.v1.SensorValue;

@Service
public class RoadStationSensorService {
    private static final Logger log = LoggerFactory.getLogger(RoadStationSensorService.class);

    private final RoadStationSensorValueDtoRepository roadStationSensorValueDtoRepository;
    private final RoadStationSensorRepository roadStationSensorRepository;
    private final RoadStationRepository roadStationRepository;
    private final DataStatusService dataStatusService;
    private final SensorValueRepository sensorValueRepository;
    private final EntityManager entityManager;

    private final Map<RoadStationType, Integer> sensorValueTimeLimitInMins;

    @Autowired
    public RoadStationSensorService(final RoadStationSensorValueDtoRepository roadStationSensorValueDtoRepository,
                                    final RoadStationSensorRepository roadStationSensorRepository,
                                    final DataStatusService dataStatusService,
                                    final RoadStationRepository roadStationRepository,
                                    final SensorValueRepository sensorValueRepository,
                                    final EntityManager entityManager,
                                    @Value("${weatherStation.sensorValueTimeLimitInMinutes}")
                                    final int weatherStationSensorValueTimeLimitInMins,
                                    @Value("${tmsStation.sensorValueTimeLimitInMinutes}")
                                    final int tmsStationSensorValueTimeLimitInMins) {
        this.roadStationSensorValueDtoRepository = roadStationSensorValueDtoRepository;
        this.roadStationSensorRepository = roadStationSensorRepository;
        this.roadStationRepository = roadStationRepository;
        this.dataStatusService = dataStatusService;
        this.sensorValueRepository = sensorValueRepository;
        this.entityManager = entityManager;

        sensorValueTimeLimitInMins = new EnumMap<>(RoadStationType.class);
        sensorValueTimeLimitInMins.put(RoadStationType.WEATHER_STATION, weatherStationSensorValueTimeLimitInMins);
        sensorValueTimeLimitInMins.put(RoadStationType.TMS_STATION, tmsStationSensorValueTimeLimitInMins);
    }

    private CriteriaBuilder createCriteriaBuilder() {
        return entityManager
            .getCriteriaBuilder();
    }

    @Transactional(readOnly = true)
    public List<RoadStationSensor> findAllPublishableRoadStationSensors(final RoadStationType roadStationType) {
        return roadStationSensorRepository.findByRoadStationTypeAndPublishable(roadStationType);
    }

    @Transactional(readOnly = true)
    public List<RoadStationSensor> findAllRoadStationSensors(final RoadStationType roadStationType) {
        return roadStationSensorRepository.findDistinctByRoadStationType(roadStationType);
    }

    @Transactional(readOnly = true)
    public Map<Long, RoadStationSensor> findAllRoadStationSensorsMappedByLotjuId(RoadStationType roadStationType) {
        final List<RoadStationSensor> all = findAllRoadStationSensors(roadStationType);
        return all.stream().collect(Collectors.toMap(RoadStationSensor::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public WeatherRoadStationsSensorsMetadata findWeatherRoadStationsSensorsMetadata(final boolean onlyUpdateInfo) {
        return new WeatherRoadStationsSensorsMetadata(
            onlyUpdateInfo ?
                Collections.emptyList() :
                RoadStationSensorDtoConverter.convertWeatherSensors(findAllPublishableRoadStationSensors(RoadStationType.WEATHER_STATION)),
            dataStatusService.findDataUpdatedTime(DataType.getSensorMetadataTypeForRoadStationType(RoadStationType.WEATHER_STATION)),
            dataStatusService.findDataUpdatedTime(DataType.getSensorMetadataCheckTypeForRoadStationType(RoadStationType.WEATHER_STATION)));
    }

    @Transactional(readOnly = true)
    public TmsRoadStationsSensorsMetadata findTmsRoadStationsSensorsMetadata(final boolean onlyUpdateInfo) {
        return new TmsRoadStationsSensorsMetadata(
            onlyUpdateInfo ?
                Collections.emptyList() :
                RoadStationSensorDtoConverter.convertTmsSensors(findAllPublishableRoadStationSensors(RoadStationType.TMS_STATION)),
            dataStatusService.findDataUpdatedTime(DataType.getSensorMetadataTypeForRoadStationType(RoadStationType.TMS_STATION)),
            dataStatusService.findDataUpdatedTime(DataType.getSensorMetadataCheckTypeForRoadStationType(RoadStationType.TMS_STATION)));
    }

    @Transactional(readOnly = true)
    public Map<Long, List<SensorValueDto>> findAllPublishableRoadStationSensorValuesMappedByNaturalId(final RoadStationType roadStationType) {
        final Stream<SensorValueDto> sensors = roadStationSensorValueDtoRepository.findAllPublicPublishableRoadStationSensorValues(
                        roadStationType.getTypeNumber(),
                        sensorValueTimeLimitInMins.get(roadStationType));

        return sensors.parallel()
            .collect(Collectors.groupingBy(SensorValueDto::getRoadStationNaturalId, Collectors.mapping(Function.identity(), toList())));
    }

    @Transactional(readOnly = true)
    public ZonedDateTime getLatestSensorValueUpdatedTime(final RoadStationType roadStationType) {
        return dataStatusService.findDataUpdatedTime(DataType.getSensorValueUpdatedDataType(roadStationType));
    }

    @Transactional(readOnly = true)
    public ZonedDateTime getLatestSensorValueMeasurementTime(final RoadStationType roadStationType) {
        return dataStatusService.findDataUpdatedTime(DataType.getSensorValueMeasuredDataType(roadStationType));
    }

    @Transactional(readOnly = true)
    public List<SensorValueDto> findAllPublishableRoadStationSensorValues(final long roadStationNaturalId,
                                                                          final RoadStationType roadStationType) {
        final boolean publicAndNotObsolete = roadStationRepository.isPublishableRoadStation(roadStationNaturalId, roadStationType);

        if ( !publicAndNotObsolete ) {
            return Collections.emptyList();
        }

        return roadStationSensorValueDtoRepository.findAllPublicPublishableRoadStationSensorValues(
                roadStationNaturalId,
                roadStationType.getTypeNumber(),
                sensorValueTimeLimitInMins.get(roadStationType));
    }

    @Transactional
    public RoadStationSensor save(final RoadStationSensor roadStationSensor) {
        return roadStationSensorRepository.save(roadStationSensor);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<SensorValue>> findNonObsoleteSensorvaluesListMappedByTmsLotjuId(final List<Long> lamLotjuIds,
        final RoadStationType roadStationType) {
        final List<SensorValue> sensorValues = sensorValueRepository
            .findByRoadStationObsoleteDateIsNullAndRoadStationSensorObsoleteDateIsNullAndRoadStationLotjuIdInAndRoadStationType(lamLotjuIds, roadStationType);

        final HashMap<Long, List<SensorValue>> sensorValuesListByTmsLotjuIdMap = new HashMap<>();
        for (final SensorValue sensorValue : sensorValues) {
            final Long rsLotjuId = sensorValue.getRoadStation().getLotjuId();

            List<SensorValue> list = sensorValuesListByTmsLotjuIdMap.get(rsLotjuId);
            if (list == null) {
                list = new ArrayList<>();
                sensorValuesListByTmsLotjuIdMap.put(rsLotjuId, list);
            }
            list.add(sensorValue);
        }

        return sensorValuesListByTmsLotjuIdMap;
    }

    @Transactional(readOnly = true)
    public List<SensorValueDto> findAllPublicNonObsoleteRoadStationSensorValuesUpdatedAfter(final ZonedDateTime updatedAfter, final RoadStationType roadStationType) {
        return roadStationSensorValueDtoRepository.findAllPublicPublishableRoadStationSensorValuesUpdatedAfter(
                roadStationType.getTypeNumber(),
                updatedAfter.toInstant());
    }

    /**
     *
     * @param roadStationId station which sensors to update
     * @param roadStationType what is type of station
     * @param sensorslotjuIds current anturis lotju ids
     * @return Pair of deleted and inserted count of sensors for given weather station
     */
    @Transactional
    public Pair<Integer, Integer> updateSensorsOfWeatherStations(final long roadStationId,
                                                                 final RoadStationType roadStationType,
                                                                 final List<Long> sensorslotjuIds) {

        final int deleted = sensorslotjuIds.isEmpty() ?
                                roadStationSensorRepository.deleteRoadStationsSensors(roadStationId) :
                                roadStationSensorRepository.deleteNonExistingSensors(
                                    roadStationType.name(), roadStationId, sensorslotjuIds);

        final int inserted = sensorslotjuIds.isEmpty() ?
                                0 : roadStationSensorRepository.insertNonExistingSensors(roadStationType.name(),
                                                                                         roadStationId,
                                                                                         sensorslotjuIds);

        return Pair.of(deleted, inserted);
    }

    @Transactional
    public UpdateStatus updateOrInsert(final TiesaaLaskennallinenAnturiVO anturi) {

        final RoadStationSensor sensor = roadStationSensorRepository.findByRoadStationTypeAndLotjuId(RoadStationType.WEATHER_STATION, anturi.getId());

        if (sensor != null) {
            final String before = ReflectionToStringBuilder.toString(sensor);
            if ( updateRoadStationSensorAttributes(anturi, sensor) ) {
                log.info("Updated RoadStationSensor:\n{} -> \n{}",  before , ReflectionToStringBuilder.toString(sensor));
                return UpdateStatus.UPDATED;
            }
            return UpdateStatus.NOT_UPDATED;
        } else {
            RoadStationSensor newSensor = new RoadStationSensor();
            updateRoadStationSensorAttributes(anturi, newSensor);
            save(newSensor);
            log.info("Created new {}", newSensor);
            return UpdateStatus.INSERTED;
        }
    }

    @Transactional
    public UpdateStatus updateOrInsert(LamLaskennallinenAnturiVO anturi) {

        final RoadStationSensor sensor = roadStationSensorRepository.findByRoadStationTypeAndLotjuId(RoadStationType.TMS_STATION, anturi.getId());

        if (sensor != null) {
            final String before = ReflectionToStringBuilder.toString(sensor);
            if ( updateRoadStationSensorAttributes(anturi, sensor) ) {
                log.info("Updated RoadStationSensor:\n{} -> \n{}",  before , ReflectionToStringBuilder.toString(sensor));
                return UpdateStatus.UPDATED;
            }
            return UpdateStatus.NOT_UPDATED;
        } else {
            RoadStationSensor newSensor = new RoadStationSensor();
            updateRoadStationSensorAttributes(anturi, newSensor);
            save(newSensor);
            log.info("Created new {}", newSensor);
            return UpdateStatus.INSERTED;
        }
    }

    @Transactional
    public int obsoleteSensorsExcludingLotjuIds(final RoadStationType roadStationType, final List<Long> sensorsLotjuIdsNotToObsolete) {
        final CriteriaBuilder cb = createCriteriaBuilder();
        final CriteriaUpdate<RoadStationSensor> update = cb.createCriteriaUpdate(RoadStationSensor.class);
        final Root<RoadStationSensor> root = update.from(RoadStationSensor.class);
        EntityType<RoadStationSensor> rootModel = root.getModel();
        update.set("obsoleteDate", LocalDate.now());

        List<Predicate> predicates = new ArrayList<>();
        predicates.add( cb.equal(root.get(rootModel.getSingularAttribute("roadStationType", RoadStationType.class)), roadStationType));
        for (List<Long> ids : Iterables.partition(sensorsLotjuIdsNotToObsolete, 1000)) {
            predicates.add(cb.not(root.get("lotjuId").in(ids)));
        }
        update.where(cb.and(predicates.toArray(new Predicate[0])));

        return this.entityManager.createQuery(update).executeUpdate();
    }

    private static boolean updateRoadStationSensorAttributes(final LamLaskennallinenAnturiVO from, final RoadStationSensor to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        to.setRoadStationType(RoadStationType.TMS_STATION);
        to.setObsoleteDate(null);
        to.setPublic(from.isJulkinen());

        to.setLotjuId(from.getId());
        to.setNaturalId(from.getVanhaId());
        if (to.getName() == null) {
            to.setName(from.getNimi());
        }
        to.setNameFi(from.getNimi());
        to.setShortNameFi(from.getLyhytNimi());
        to.setPresentationNameFi(from.getEsitysnimiFi());
        to.setPresentationNameSv(from.getEsitysnimiSe());
        to.setPresentationNameEn(from.getEsitysnimiEn());
        to.setDescriptionFi(from.getKuvausFi());
        to.setDescriptionEn(from.getKuvausEn());
        to.setDescriptionSv(from.getKuvausSe());
        to.setAccuracy(from.getTarkkuus());
        to.setUnit(DataValidityHelper.nullifyUnknownValue(from.getYksikko()));
        to.setVehicleClass(VehicleClass.fromAjoneuvoluokka(from.getAjoneuvoluokka()));
        to.setLane(from.getKaista());
        to.setDirection(from.getSuunta());

        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    private static boolean updateRoadStationSensorAttributes(final TiesaaLaskennallinenAnturiVO from, final RoadStationSensor to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        to.setRoadStationType(RoadStationType.WEATHER_STATION);
        to.setObsoleteDate(null);
        to.setPublic(from.isJulkinen());

        to.setLotjuId(from.getId());
        to.setNaturalId(from.getVanhaId());
        if (to.getName() == null) {
            to.setName(from.getNimi());
        }
        to.setNameFi(from.getNimi());
        to.setShortNameFi(from.getLyhytNimi());
        to.setPresentationNameFi(from.getEsitysnimiFi());
        to.setPresentationNameSv(from.getEsitysnimiSe());
        to.setPresentationNameEn(from.getEsitysnimiEn());
        to.setDescriptionFi(from.getKuvausFi());
        to.setDescriptionSv(from.getKuvausSe());
        to.setDescriptionEn(from.getKuvausEn());
        to.setAccuracy(from.getTarkkuus());
        to.setUnit(from.getYksikko());

        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }
}
