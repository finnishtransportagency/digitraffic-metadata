package fi.livi.digitraffic.tie.metadata.service.roadstationsensor;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.helper.DataValidityHelper;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationSensorValueDtoRepository;
import fi.livi.digitraffic.tie.metadata.dao.SensorValueRepository;
import fi.livi.digitraffic.tie.metadata.dto.RoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.model.DataUpdated;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;
import fi.livi.digitraffic.tie.metadata.service.UpdateStatus;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamLaskennallinenAnturiVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaLaskennallinenAnturiVO;

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
    public List<RoadStationSensor> findAllNonObsoleteAndAllowedRoadStationSensors(RoadStationType roadStationType) {
        return roadStationSensorRepository.findByRoadStationTypeAndObsoleteFalseAndAllowed(roadStationType);
    }

    @Transactional(readOnly = true)
    public List<RoadStationSensor> findAllRoadStationSensors(final RoadStationType roadStationType) {
        return roadStationSensorRepository.findDistinctByRoadStationType(roadStationType);
    }

    @Transactional(readOnly = true)
    public Map<Long, RoadStationSensor> findAllRoadStationSensorsMappedByLotjuId(RoadStationType roadStationType) {
        final List<RoadStationSensor> all = findAllRoadStationSensors(roadStationType);
        return all.stream().filter(rss -> rss.getLotjuId() != null).collect(Collectors.toMap(RoadStationSensor::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public Map<Long, RoadStationSensor> findAllRoadStationSensorsWithOutLotjuIdMappedByNaturalId(RoadStationType roadStationType) {
        final List<RoadStationSensor> all = roadStationSensorRepository.findDistinctByRoadStationTypeAndLotjuIdIsNull(roadStationType);
        return all.stream().filter(rss -> rss.getLotjuId() == null).collect(Collectors.toMap(RoadStationSensor::getNaturalId, Function.identity()));
    }


    @Transactional(readOnly = true)
    public RoadStationsSensorsMetadata findRoadStationsSensorsMetadata(final RoadStationType roadStationType, final boolean onlyUpdateInfo) {
        final DataUpdated updated = dataStatusService.findDataUpdatedByDataType(DataType.getForRoadStationType
            (roadStationType));

        return new RoadStationsSensorsMetadata(
            !onlyUpdateInfo ?
            findAllNonObsoleteAndAllowedRoadStationSensors(roadStationType) :
            Collections.emptyList(),
                updated != null ? updated.getUpdatedTime() : null);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<SensorValueDto>> findAllPublishableRoadStationSensorValuesMappedByNaturalId(final RoadStationType roadStationType) {
        final List<SensorValueDto> sensors = roadStationSensorValueDtoRepository.findAllPublicPublishableRoadStationSensorValues(
                        roadStationType.getTypeNumber(),
                        sensorValueTimeLimitInMins.get(roadStationType));

        return sensors.stream()
            .collect(Collectors.groupingBy(SensorValueDto::getRoadStationNaturalId, Collectors.mapping(Function.identity(), toList())));
    }

    @Transactional(readOnly = true)
    public ZonedDateTime getLatestMeasurementTime(final RoadStationType roadStationType) {
        return DateHelper.toZonedDateTime(
                roadStationSensorValueDtoRepository.getLatestMeasurementTime(
                        roadStationType.getTypeNumber(),
                        sensorValueTimeLimitInMins.get(roadStationType)));
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
    public RoadStationSensor save(RoadStationSensor roadStationSensor) {
        return roadStationSensorRepository.save(roadStationSensor);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<SensorValue>> findNonObsoleteSensorvaluesListMappedByTmsLotjuId(List<Long> lamLotjuIds, RoadStationType roadStationType) {
        List<SensorValue> sensorValues = sensorValueRepository.findByRoadStationObsoleteDateIsNullAndRoadStationSensorObsoleteDateIsNullAndRoadStationLotjuIdInAndRoadStationType(lamLotjuIds, roadStationType);

        HashMap<Long, List<SensorValue>> sensorValuesListByTmsLotjuIdMap = new HashMap<>();
        for (SensorValue sensorValue : sensorValues) {
            Long rsLotjuId = sensorValue.getRoadStation().getLotjuId();
            List<SensorValue> list = sensorValuesListByTmsLotjuIdMap.get(rsLotjuId);
            if (list == null) {
                list = new LinkedList<>();
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
                DateHelper.toDate(updatedAfter));
    }

    @Transactional(readOnly = true)
    public ZonedDateTime getSensorValueLastUpdated(final RoadStationType roadStationType) {
        return DateHelper.toZonedDateTime(sensorValueRepository.getLastUpdated(roadStationType));
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
                                roadStationSensorRepository.deleteNonExistingSensors(roadStationType.name(),
                                    roadStationId,
                                                                                     sensorslotjuIds);

        final int inserted = sensorslotjuIds.isEmpty() ?
                                0 : roadStationSensorRepository.insertNonExistingSensors(roadStationType.name(),
                                                                                         roadStationId,
                                                                                         sensorslotjuIds);

        return Pair.of(deleted, inserted);
    }

    @Transactional
    public boolean fixTmsStationSensorsWithoutLotjuId(final Collection<LamLaskennallinenAnturiVO> anturis) {
        List<Pair<Integer, Long>> naturalIdLotjuId = anturis.stream().map(a -> Pair.of(a.getVanhaId(), a.getId())).collect(toList());
        return updateRoadStationSensorsWithOutLotjuIds(RoadStationType.TMS_STATION, naturalIdLotjuId);
    }

    @Transactional
    public boolean fixWeatherStationSensorsWithoutLotjuId(final Collection<TiesaaLaskennallinenAnturiVO> anturis) {
        List<Pair<Integer, Long>> naturalIdLotjuId = anturis.stream().map(a -> Pair.of(a.getVanhaId(), a.getId())).collect(toList());
        return updateRoadStationSensorsWithOutLotjuIds(RoadStationType.WEATHER_STATION, naturalIdLotjuId);
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
        update.set("obsolete", true);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add( cb.equal(root.get(rootModel.getSingularAttribute("roadStationType", RoadStationType.class)), roadStationType));
        for (List<Long> ids : Iterables.partition(sensorsLotjuIdsNotToObsolete, 1000)) {
            predicates.add(cb.not(root.get("lotjuId").in(ids)));
        }
        update.where(cb.and(predicates.toArray(new Predicate[0])));

        return this.entityManager.createQuery(update).executeUpdate();
    }

    private boolean updateRoadStationSensorsWithOutLotjuIds(final RoadStationType roadStationType, final List<Pair<Integer, Long>> naturalIdLotjuId) {
        final Map<Long, RoadStationSensor> currentSensorsMappedByNaturalId =
            findAllRoadStationSensorsWithOutLotjuIdMappedByNaturalId(roadStationType);

        int updated = 0;
        for(Pair<Integer, Long> vanhaIdLotjuId : naturalIdLotjuId) {
            {
                final RoadStationSensor currentSaved = currentSensorsMappedByNaturalId.remove(Long.valueOf(vanhaIdLotjuId.getLeft()));
                if ( currentSaved != null ) {
                    currentSaved.setLotjuId(vanhaIdLotjuId.getRight());
                    updated++;
                }
            }
        }
        // Obsolete not found sensors
        final long obsoleted = obsoleteSensors(currentSensorsMappedByNaturalId.values());

        log.info("Obsoleted {} {} RoadStationSensor", obsoleted, roadStationType);
        log.info("Fixed {} {} RoadStationSensor without lotjuId", updated, roadStationType);

        return obsoleted > 0 || updated > 0;
    }

    private static long obsoleteSensors(Collection<RoadStationSensor> sensors) {
        return sensors.stream().filter(RoadStationSensor::obsolete).count();
    }

    private static boolean updateRoadStationSensorAttributes(final LamLaskennallinenAnturiVO from, final RoadStationSensor to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        to.setRoadStationType(RoadStationType.TMS_STATION);
        to.setObsolete(false);
        to.setObsoleteDate(null);

        to.setLotjuId(from.getId());
        to.setNaturalId(from.getVanhaId());
        if (to.getName() == null) {
            to.setName(from.getNimi());
        }
        to.setNameFi(from.getNimi());
        to.setShortNameFi(from.getLyhytNimi());
        to.setDescription(from.getKuvaus());
        to.setAccuracy(from.getTarkkuus());
        to.setUnit(DataValidityHelper.nullifyUnknownValue(from.getYksikko()));

        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }

    private static boolean updateRoadStationSensorAttributes(final TiesaaLaskennallinenAnturiVO from, final RoadStationSensor to) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        to.setRoadStationType(RoadStationType.WEATHER_STATION);
        to.setObsolete(false);
        to.setObsoleteDate(null);

        to.setLotjuId(from.getId());
        to.setNaturalId(from.getVanhaId());
        if (to.getName() == null) {
            to.setName(from.getNimi());
        }
        to.setNameFi(from.getNimi());
        to.setShortNameFi(from.getLyhytNimi());
        to.setDescription(from.getKuvaus());
        to.setAccuracy(from.getTarkkuus());
        to.setUnit(from.getYksikko());

        return HashCodeBuilder.reflectionHashCode(to) != hash;
    }
}
