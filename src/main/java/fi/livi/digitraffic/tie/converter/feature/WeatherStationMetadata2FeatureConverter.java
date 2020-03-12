package fi.livi.digitraffic.tie.converter.feature;

import static fi.livi.digitraffic.tie.dao.v1.RoadStationSensorRepository.WEATHER_STATION_TYPE;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.StationSensorConverter;
import fi.livi.digitraffic.tie.converter.exception.NonPublicRoadStationException;
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
        final WeatherStationFeatureCollection collection = new WeatherStationFeatureCollection(lastUpdated, dataLastCheckedTime);
        final Map<Long, List<Long>> sensorMap = stationSensorConverter.createPublishableSensorMap(WEATHER_STATION_TYPE);

        for(final WeatherStation rws : stations) {
            try {
                collection.add(convert(sensorMap, rws));
            } catch (final NonPublicRoadStationException nprse) {
                //Skip non public roadstation
                log.warn("Skipping: " + nprse.getMessage());
            }
        }
        return collection;
    }

    private WeatherStationFeature convert(final Map<Long, List<Long>> sensorMap, final WeatherStation rws) throws NonPublicRoadStationException {
        final WeatherStationFeature f = new WeatherStationFeature();
        if (log.isDebugEnabled()) {
            log.debug("Convert: " + rws);
        }
        f.setId(rws.getRoadStationNaturalId());

        final WeatherStationProperties properties = f.getProperties();

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
        setRoadStationProperties(properties, rs);

        setCoordinates(f, rs);

        return f;
    }
}
