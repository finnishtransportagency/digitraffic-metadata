package fi.livi.digitraffic.tie.metadata.service.weather;

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

import fi.livi.digitraffic.tie.metadata.converter.WeatherStationMetadata2FeatureConverter;
import fi.livi.digitraffic.tie.metadata.dao.SensorValueRepository;
import fi.livi.digitraffic.tie.metadata.dao.WeatherStationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.model.SensorValue;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class WeatherStationService {

    private static final Logger log = LoggerFactory.getLogger(WeatherStationService.class);
    private final WeatherStationRepository weatherStationRepository;
    private final SensorValueRepository sensorValueRepository;
    private final StaticDataStatusService staticDataStatusService;
    private final WeatherStationMetadata2FeatureConverter weatherStationMetadata2FeatureConverter;

    @Autowired
    public WeatherStationService(final WeatherStationRepository weatherStationRepository,
                                 final SensorValueRepository sensorValueRepository,
                                 final StaticDataStatusService staticDataStatusService,
                                 final WeatherStationMetadata2FeatureConverter weatherStationMetadata2FeatureConverter) {

        this.weatherStationRepository = weatherStationRepository;
        this.sensorValueRepository = sensorValueRepository;
        this.staticDataStatusService = staticDataStatusService;
        this.weatherStationMetadata2FeatureConverter = weatherStationMetadata2FeatureConverter;
    }

    @Transactional(readOnly = true)
    public Map<Long, WeatherStation> findAllWeatherStationsMappedByLotjuId() {
        final Map<Long, WeatherStation> map = new HashMap<>();
        final List<WeatherStation> all = weatherStationRepository.findAll();
        for (final WeatherStation weatherStation : all) {
            if (weatherStation.getLotjuId() != null) {
                map.put(weatherStation.getLotjuId(), weatherStation);
            } else {
                log.warn("Null lotjuId: " + weatherStation);
            }
        }
        return map;
    }

    @Transactional
    public WeatherStation save(final WeatherStation weatherStation) {
        try {
            final WeatherStation rws = weatherStationRepository.save(weatherStation);
            weatherStationRepository.flush();
            return rws;
        } catch (Exception e) {
            log.error("Could not save " + weatherStation);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<SensorValue> findAllSensorValues() {
        return sensorValueRepository.findAll();
    }

    @Transactional(readOnly = true)
    public WeatherStation findByLotjuId(long lotjuId) {
        return weatherStationRepository.findByLotjuId(lotjuId);
    }

    @Transactional(readOnly = true)
    public Map<Long, WeatherStation> findWeatherStationsMappedByLotjuId(List<Long> weatherStationLotjuIds) {
        final List<WeatherStation> all = weatherStationRepository.findByLotjuIdIn(weatherStationLotjuIds);
        return all.stream().collect(Collectors.toMap(WeatherStation::getLotjuId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public WeatherStationFeatureCollection findAllNonObsoletePublicWeatherStationAsFeatureCollection(final boolean onlyUpdateInfo) {

        final MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.WEATHER_STATION);

        return weatherStationMetadata2FeatureConverter.convert(
                !onlyUpdateInfo ?
                    weatherStationRepository.findByRoadStationObsoleteFalseAndRoadStationIsPublicTrueAndLotjuIdIsNotNullOrderByRoadStation_NaturalId() :
                    Collections.emptyList(),
                updated != null ? updated.getUpdated() : null);
    }

    public List<WeatherStation> findAllNonObsoletePublicWeatherStations() {
        return weatherStationRepository.findByRoadStationObsoleteFalseAndRoadStationIsPublicTrueAndLotjuIdIsNotNullOrderByRoadStation_NaturalId();
    }

    @Transactional(readOnly = true)
    public Map<Long, WeatherStation> findAllWeatherStationsMappedByByRoadStationNaturalId() {
        final List<WeatherStation> allStations = weatherStationRepository.findAll();
        final Map<Long, WeatherStation> stationMap = new HashMap<>();

        for(final WeatherStation weatherStation : allStations) {
            stationMap.put(weatherStation.getRoadStationNaturalId(), weatherStation);
        }

        return stationMap;
    }

    @Transactional(readOnly = true)
    public Map<Long, WeatherStation> findAllWeatherStationsWithoutLotjuIdMappedByByRoadStationNaturalId() {
        final List<WeatherStation> allStations = weatherStationRepository.findByLotjuIdIsNull();
        final Map<Long, WeatherStation> stationMap = new HashMap<>();

        for(final WeatherStation weatherStation : allStations) {
            if (weatherStation.getRoadStationNaturalId() != null) {
                stationMap.put(weatherStation.getRoadStationNaturalId(), weatherStation);
            } else {
                log.warn("Null lotjuId: " + weatherStation);
            }
        }

        return stationMap;
    }
}
