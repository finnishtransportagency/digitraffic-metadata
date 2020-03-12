package fi.livi.digitraffic.tie.converter.feature;

import static fi.livi.digitraffic.tie.dao.v1.RoadStationSensorRepository.TMS_STATION_TYPE;

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
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationProperties;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.TmsStation;

@Component
public final class TmsStationMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {
    private static final Logger log = LoggerFactory.getLogger( TmsStationMetadata2FeatureConverter.class);

    private final StationSensorConverter stationSensorConverter;

    @Autowired
    private TmsStationMetadata2FeatureConverter(final CoordinateConverter coordinateConverter, final StationSensorConverter stationSensorConverter) {
        super(coordinateConverter);
        this.stationSensorConverter = stationSensorConverter;
    }

    public TmsStationFeatureCollection convert(final List<TmsStation> stations, final ZonedDateTime lastUpdated, final ZonedDateTime dataLastCheckedTime) {
        final TmsStationFeatureCollection collection = new TmsStationFeatureCollection(lastUpdated, dataLastCheckedTime);
        final Map<Long, List<Long>> sensorMap = stationSensorConverter.createPublishableSensorMap(TMS_STATION_TYPE);

        for(final TmsStation tms : stations) {
            try {
                collection.add(convert(sensorMap, tms));
            } catch (final NonPublicRoadStationException nprse) {
                //Skip non public roadstation
                log.warn("Skipping: " + nprse.getMessage());
            }
        }
        return collection;
    }

    public TmsStationFeature convert(final TmsStation tms) throws NonPublicRoadStationException {
        final Map<Long, List<Long>> sensorMap = stationSensorConverter.createPublishableSensorMap(tms.getRoadStationId(), TMS_STATION_TYPE);

        return convert(sensorMap, tms);
    }

    public TmsStationFeature convert(final Map<Long, List<Long>> sensorMap, final TmsStation tms) throws NonPublicRoadStationException {
        final TmsStationFeature f = new TmsStationFeature();
        if (log.isDebugEnabled()) {
            log.debug("Convert: " + tms);
        }
        f.setId(tms.getRoadStationNaturalId());

        final TmsStationProperties properties = f.getProperties();

        // Tms station properties
        properties.setId(tms.getId());
        properties.setLotjuId(tms.getLotjuId());
        properties.setTmsNaturalId(tms.getNaturalId());
        properties.setDirection1Municipality(tms.getDirection1Municipality());
        properties.setDirection1MunicipalityCode(tms.getDirection1MunicipalityCode());
        properties.setDirection2Municipality(tms.getDirection2Municipality());
        properties.setDirection2MunicipalityCode(tms.getDirection2MunicipalityCode());
        properties.setTmsStationType(tms.getTmsStationType());
        properties.setCalculatorDeviceType(tms.getCalculatorDeviceType());
        properties.setName(tms.getName());

        if (tms.getRoadStation() != null) {
            final List<Long> sensorList = sensorMap.get(tms.getRoadStationId());
            properties.setStationSensors(ObjectUtils.firstNonNull(sensorList, Collections.emptyList()));
        }
        // RoadStation properties
        final RoadStation rs = tms.getRoadStation();
        setRoadStationProperties(properties, rs);

        setCoordinates(f, rs);

        return f;
    }
}
