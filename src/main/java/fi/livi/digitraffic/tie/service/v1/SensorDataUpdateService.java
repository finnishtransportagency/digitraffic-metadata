package fi.livi.digitraffic.tie.service.v1;

import static fi.ely.lotju.lam.proto.LAMRealtimeProtos.Lam;
import static fi.ely.lotju.tiesaa.proto.TiesaaProtos.TiesaaMittatieto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.SensorValueDao;
import fi.livi.digitraffic.tie.dto.v1.SensorValueUpdateParameterDto;
import fi.livi.digitraffic.tie.helper.TimestampCache;
import fi.livi.digitraffic.tie.dao.v1.RoadStationDao;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.DataStatusService;

@Service
public class SensorDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(SensorDataUpdateService.class);
    private static final long ALLOWED_SENSOR_EXPIRATION_MILLIS = 300000; // 5min

    private final Map<RoadStationType, Set<Long>> allowedSensorsLotjuIds = new EnumMap<RoadStationType, Set<Long>>(RoadStationType.class);
    private final Map<RoadStationType, Long> allowedSensorsLastUpdatedTimeMillis = new EnumMap<RoadStationType, Long>(RoadStationType.class);

    private final SensorValueDao sensorValueDao;
    private final RoadStationSensorService roadStationSensorService;
    private final RoadStationDao roadStationDao;
    private final DataStatusService dataStatusService;

    @Autowired
    public SensorDataUpdateService(final SensorValueDao sensorValueDao, final RoadStationSensorService roadStationSensorService,
                                   final RoadStationDao roadStationDao, final DataStatusService dataStatusService) {
        this.sensorValueDao = sensorValueDao;
        this.roadStationSensorService = roadStationSensorService;
        this.roadStationDao = roadStationDao;
        this.dataStatusService = dataStatusService;
    }

    private Set<Long> getAllowedRoadStationSensorsLotjuIds(final RoadStationType roadStationType) {
        if (allowedSensorsLotjuIds.get(roadStationType) == null || allowedSensorsLastUpdatedTimeMillis.get(roadStationType) < System.currentTimeMillis() - ALLOWED_SENSOR_EXPIRATION_MILLIS) {
            final List<RoadStationSensor> allowedTmsSensors =
                roadStationSensorService.findAllPublishableRoadStationSensors(roadStationType);

            allowedSensorsLotjuIds.put(roadStationType, allowedTmsSensors.stream().map(RoadStationSensor::getLotjuId).collect(Collectors.toSet()));

            allowedSensorsLastUpdatedTimeMillis.put(roadStationType, System.currentTimeMillis());
            log.info("method=getAllowedRoadStationSensorsLotjuIds fetched sensorCount={} for roadStationType={}", allowedSensorsLotjuIds.get(roadStationType).size(), roadStationType);
        }
        return allowedSensorsLotjuIds.get(roadStationType);
    }

    /**
     * Updates tms sensors data to db
     * @param data
     * @return count of updated db rows
     */
    @Transactional
    public int updateLamData(final List<Lam> data) {
        final StopWatch stopWatch = StopWatch.createStarted();

        final Map<Long, Long> allowedStationsLotjuIdtoIds = roadStationDao.findPublishableRoadStationsIdsMappedByLotjuId(RoadStationType.TMS_STATION);

        final long initialDataRowCount = data.stream().mapToLong(lam -> lam.getAnturiList().size()).sum();

        final List<Lam> filteredByMissingStation =
            data.stream().filter(lam -> allowedStationsLotjuIdtoIds.containsKey(lam.getAsemaId())).collect(Collectors.toList());
        final long filteredByStationRowCount = filteredByMissingStation.stream().mapToLong(lam -> lam.getAnturiList().size()).sum();

        if (filteredByMissingStation.size() < data.size()) {
            log.warn("method=updateLamData filter data from originalCount={} with missingTmsStationsCount={} to resultCount={}" ,
                     data.size(), data.size()-filteredByMissingStation.size(), filteredByMissingStation.size());
        }

        final List<LotjuAnturiWrapper<Lam.Anturi>> wrappedAnturiValues = wrapLamData(filteredByMissingStation);
        final List<LotjuAnturiWrapper<Lam.Anturi>> filteredByOnlyNewest = filterNewestAnturiValues(wrappedAnturiValues);

        if (filteredByOnlyNewest.size() < filteredByStationRowCount) {
            log.info("method=updateLamData filter data rows from originalCount={} with oldDataCount={} to resultCount={}",
                     filteredByStationRowCount, filteredByStationRowCount-filteredByOnlyNewest.size(), filteredByOnlyNewest.size());
        }

        final long stationsCount = filteredByOnlyNewest.stream().map(LotjuAnturiWrapper::getAsemaLotjuId).distinct().count();

        final TimestampCache timestampCache = new TimestampCache();

        final List<SensorValueUpdateParameterDto> params =
            filteredByOnlyNewest.stream()
            .filter(wrapper -> getAllowedRoadStationSensorsLotjuIds(RoadStationType.TMS_STATION).contains(wrapper.getAnturi().getLaskennallinenAnturiId()))
                            .map(anturi -> new SensorValueUpdateParameterDto(anturi, allowedStationsLotjuIdtoIds.get(anturi.getAsemaLotjuId()), timestampCache))
            .collect(Collectors.toList());

        final Pair<Integer, Integer> updatedAndInsertedCount = updateSensorData(params, RoadStationType.TMS_STATION);
        stopWatch.stop();

        log.info("method=updateLamData initial data rowCount={} filtered to updateRowCount={}. Sensors updateCount={} insertCount={} of sations stationCount={} . hasRealtime={} . hasNonRealtime={} tookMs={}",
            initialDataRowCount, filteredByOnlyNewest.size(),
            updatedAndInsertedCount.getLeft(), updatedAndInsertedCount.getRight(), stationsCount,
            filteredByMissingStation.stream().anyMatch(Lam::getIsRealtime),
            filteredByMissingStation.stream().anyMatch(lam -> !lam.getIsRealtime()),
            stopWatch.getTime());

        return updatedAndInsertedCount.getLeft() + updatedAndInsertedCount.getRight();
    }

    /**
     * Updates weather data to db
     * @param data
     * @return count of updated db rows
     */
    @Transactional
    public int updateWeatherData(final List<TiesaaMittatieto> data) {
        final StopWatch stopWatch = StopWatch.createStarted();

        final Map<Long, Long> allowedStationsLotjuIdtoIds = roadStationDao.findPublishableRoadStationsIdsMappedByLotjuId(RoadStationType.WEATHER_STATION);

        final long initialDataRowCount = data.stream().mapToLong(tiesaa -> tiesaa.getAnturiList().size()).sum();

        final List<TiesaaMittatieto> filteredByMissingStation =
            data.stream().filter(tiesaa -> allowedStationsLotjuIdtoIds.containsKey(tiesaa.getAsemaId())).collect(Collectors.toList());

        final long filteredByMissingStationRowCount = filteredByMissingStation.stream().mapToLong(lam -> lam.getAnturiList().size()).sum();

        final List<LotjuAnturiWrapper<TiesaaMittatieto.Anturi>> wrappedAnturiValues = wrapTiesaaData(filteredByMissingStation);
        final List<LotjuAnturiWrapper<TiesaaMittatieto.Anturi>> filteredByOnlyNewest = filterNewestAnturiValues(wrappedAnturiValues);

        if (filteredByMissingStation.size() < data.size() || filteredByOnlyNewest.size() < filteredByMissingStationRowCount) {
            log.warn("method=updateWeatherData filter data from originalCount={} with missingWeatherStationsCount={} and oldDataCount={} to resultCount={}" ,
                data.size(), data.size()-filteredByMissingStation.size(), filteredByMissingStationRowCount-filteredByOnlyNewest.size(), filteredByOnlyNewest.size());
        }

        final TimestampCache timestampCache = new TimestampCache();

        final long stationsCount = filteredByOnlyNewest.stream().map(LotjuAnturiWrapper::getAsemaLotjuId).distinct().count();

        final List<SensorValueUpdateParameterDto> params =
            filteredByOnlyNewest.stream()
                .filter(wrapper -> getAllowedRoadStationSensorsLotjuIds(RoadStationType.WEATHER_STATION).contains(wrapper.getAnturi().getLaskennallinenAnturiId()))
                .map(anturi -> new SensorValueUpdateParameterDto(anturi, timestampCache, allowedStationsLotjuIdtoIds.get(anturi.getAsemaLotjuId())))
                .collect(Collectors.toList());

        final Pair<Integer, Integer> updatedAndInsertedCount = updateSensorData(params, RoadStationType.WEATHER_STATION);

        stopWatch.stop();
        log.info("method=updateWeatherData initial data rowCount={} filtered to updateRowCount={}. Sensors updateCount={} insertCount={} of stations stationCount={} tookMs={}",
            initialDataRowCount, filteredByOnlyNewest.size(),
            updatedAndInsertedCount.getLeft(), updatedAndInsertedCount.getRight(),
            stationsCount, stopWatch.getTime());
        return updatedAndInsertedCount.getLeft() + updatedAndInsertedCount.getRight();
    }

    /**
     *  Update sensor values to database
     *
     * @param params
     * @param roadStationType
     * @return Pair<updateCount, insertCount>
     */
    private Pair<Integer, Integer> updateSensorData(final List<SensorValueUpdateParameterDto> params, final RoadStationType roadStationType) {
        if(CollectionUtils.isEmpty(params)) {
            log.info("method=updateSensorData for 0 stations updateCount=0 insertCount=0 tookMs=0");

            return ImmutablePair.of(0 ,0);
        }

        final StopWatch stopWatch = StopWatch.createStarted();
        final OffsetDateTime maxMeasuredTime = getMaxMeasured(params);

        // First try to update with given parameters data. 0 value in return array means that parameter in question didn't cause update -> should be inserted.
        final int[] updated = sensorValueDao.updateSensorData(params);
        // Resolve parameters that didn't cause update and do insert with those parameters.
        final ArrayList<SensorValueUpdateParameterDto> toInsert = getSensorValueInsertParameters(params, updated);
        final int[] inserted = sensorValueDao.insertSensorData(toInsert);

        updateDataMeasuredTime(roadStationType, maxMeasuredTime);
        updateDataUpdatedTime(roadStationType);

        final int updatedCount = countSum(updated);
        final int insertedCount = countSum(inserted);
        log.info("method=updateSensorData for {} stations updateCount={} insertCount={} tookMs={}",
                 roadStationType, updatedCount, insertedCount, stopWatch.getTime());
        return ImmutablePair.of(updatedCount, insertedCount);
    }

    private void updateDataUpdatedTime(RoadStationType roadStationType) {
        dataStatusService.updateDataUpdated(DataType.getSensorValueUpdatedDataType(roadStationType),
                                            dataStatusService.getTransactionStartTime());
    }

    private void updateDataMeasuredTime(RoadStationType roadStationType, OffsetDateTime maxMeasuredTime) {
        dataStatusService.updateDataUpdated(DataType.getSensorValueMeasuredDataType(roadStationType),
                                            maxMeasuredTime.toInstant());
    }

    private static List<LotjuAnturiWrapper<Lam.Anturi>> wrapLamData(final List<Lam> lams) {
        return lams.stream()
            .flatMap(lam -> lam.getAnturiList().stream()
                .map(anturi -> new LotjuAnturiWrapper<>(lam.getAsemaId(), anturi.getLaskennallinenAnturiId(),
                                                        anturi, lam.getAika()))
            ).collect(Collectors.toList());
    }

    private static List<LotjuAnturiWrapper<TiesaaMittatieto.Anturi>> wrapTiesaaData(final List<TiesaaMittatieto> tiesaaMittatietos) {
        return tiesaaMittatietos.stream()
            .flatMap(tiesaa -> tiesaa.getAnturiList().stream()
                               .map(anturi -> new LotjuAnturiWrapper<>(tiesaa.getAsemaId(), anturi.getLaskennallinenAnturiId(),
                                                                       anturi, tiesaa.getAika()))
            ).collect(Collectors.toList());
    }

    private static <T> List<LotjuAnturiWrapper<T>> filterNewestAnturiValues(final List<LotjuAnturiWrapper<T>> wrappedValues) {
        final HashMap<Long, Map<Long, LotjuAnturiWrapper<T>>> stationIdToSensoridToSensorData = new HashMap<>();

        for (final LotjuAnturiWrapper<T> anturiCandidate : wrappedValues) {
            Map<Long, LotjuAnturiWrapper<T>> sensoridToSensorData = stationIdToSensoridToSensorData.get(anturiCandidate.getAsemaLotjuId());
            if (sensoridToSensorData == null) {
                sensoridToSensorData = new HashMap<>();
                stationIdToSensoridToSensorData.put(anturiCandidate.getAsemaLotjuId(), sensoridToSensorData);
            }
            LotjuAnturiWrapper<T> currentAnturi = sensoridToSensorData.get(anturiCandidate.getAnturiLotjuId());
            if (currentAnturi == null || currentAnturi.getAika() < anturiCandidate.getAika()) {
                sensoridToSensorData.put(anturiCandidate.getAnturiLotjuId(), anturiCandidate);
            }
        }

        return stationIdToSensoridToSensorData.values()
            .stream()
            .map(Map::values)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    /**
     * @param params list of parameters used in update
     * @param updated list of return values for each update parameter
     * @return list of parameters that had zero update count, meaning that insert should be performed with those parameters.
     */
    private static ArrayList<SensorValueUpdateParameterDto> getSensorValueInsertParameters(final List<SensorValueUpdateParameterDto> params,
                                                                                           final int[] updated) {
        final ArrayList<SensorValueUpdateParameterDto> toInsert = new ArrayList<>(updated.length);
        for(int i = 0; i < updated.length; i++) {
            if (updated[i] == 0) {
                toInsert.add(params.get(i));
            }
        }
        return toInsert;
    }

    private int countSum(final int[] values) {
        return IntStream.of(values).sum();
    }

    private OffsetDateTime getMaxMeasured(final List<SensorValueUpdateParameterDto> params) {
        return params.stream()
                .map(SensorValueUpdateParameterDto::getMeasured)
                .max(OffsetDateTime::compareTo)
                .orElse(null);
    }
}
