package fi.livi.digitraffic.tie.converter.feature;

import static fi.livi.digitraffic.tie.dao.v1.RoadStationSensorRepository.WEATHER_STATION_TYPE;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.StationSensorConverter;
import fi.livi.digitraffic.tie.dao.v1.WeatherStationRepository;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationProperties;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.WeatherStation;

@Component
public final class WeatherStationMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {
    private static final Logger log = LoggerFactory.getLogger( WeatherStationMetadata2FeatureConverter.class );

    private final WeatherStationRepository weatherStationRepository;
    private final StationSensorConverter stationSensorConverter;

    @Autowired
    public WeatherStationMetadata2FeatureConverter(final CoordinateConverter coordinateConverter,
        final WeatherStationRepository weatherStationRepository, final StationSensorConverter stationSensorConverter) {
        super(coordinateConverter);
        this.weatherStationRepository = weatherStationRepository;
        this.stationSensorConverter = stationSensorConverter;
    }

    public WeatherStationFeatureCollection convert(final List<WeatherStation> stations, final ZonedDateTime lastUpdated, final ZonedDateTime dataLastCheckedTime) {

        final Map<Long, List<Long>> sensorMap = stationSensorConverter.createPublishableSensorMap(WEATHER_STATION_TYPE);
        final List<WeatherStationFeature> features =
            stations.stream()
                .filter(rws -> rws.getRoadStation().isPublicNow())
                .map(rws -> convert(sensorMap, rws)).collect(Collectors.toList());

        return new WeatherStationFeatureCollection(lastUpdated, dataLastCheckedTime, features);
    }

    private WeatherStationFeature convert(final Map<Long, List<Long>> sensorMap, final WeatherStation rws) {
        if (log.isDebugEnabled()) {
            log.debug("Convert: " + rws);
        }

        final WeatherStationProperties properties = new WeatherStationProperties();

        // weather station properties
        properties.setId(rws.getRoadStationNaturalId());
        properties.setLotjuId(rws.getLotjuId());
        properties.setWeatherStationType(rws.getWeatherStationType());
        properties.setMaster(rws.isMaster());

        if (rws.getRoadStation() != null) {
            final List<Long> sensorList = sensorMap.get(rws.getRoadStationId());
            properties.setStationSensors(ObjectUtils.firstNonNull(sensorList, Collections.emptyList()));
        }

        // RoadStation properties
        final RoadStation rs = rws.getRoadStation();
        if (rs == null) {
            log.error("Null roadStation: {}", rws);
        } else {
            setRoadStationProperties(properties, rs);
        }

        return new WeatherStationFeature(getGeometry(rs), properties, rws.getRoadStationNaturalId());
    }
}
