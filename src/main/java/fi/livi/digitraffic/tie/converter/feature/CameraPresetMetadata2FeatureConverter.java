package fi.livi.digitraffic.tie.converter.feature;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.exception.NonPublicRoadStationException;
import fi.livi.digitraffic.tie.helper.DataValidityHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.dao.v1.CameraPresetRepository;
import fi.livi.digitraffic.tie.dto.v1.NearestRoadStation;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraPresetDto;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraProperties;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.WeatherStation;

@ConditionalOnWebApplication
@Component
public class CameraPresetMetadata2FeatureConverter extends AbstractMetadataToFeatureConverter {
    private static final Logger log = LoggerFactory.getLogger( CameraPresetMetadata2FeatureConverter.class );

    private final CameraPresetRepository cameraPresetRepository;

    private final String weathercamBaseurl;

    @Autowired
    public CameraPresetMetadata2FeatureConverter(@Value("${weathercam.baseUrl}") final String weathercamBaseurl,
        final CoordinateConverter coordinateConverter, final CameraPresetRepository cameraPresetRepository) {
        super(coordinateConverter);
        this.weathercamBaseurl = weathercamBaseurl;
        this.cameraPresetRepository = cameraPresetRepository;
    }

    public CameraStationFeatureCollection convert(final List<CameraPreset> cameraPresets, final ZonedDateTime lastUpdated, final ZonedDateTime dataLastCheckedTime) {
        final CameraStationFeatureCollection collection = new CameraStationFeatureCollection(lastUpdated, dataLastCheckedTime);

        // Cameras mapped with cameraId
        final Map<String, CameraStationFeature> cameraStationMap = new HashMap<>();
        final Map<Long, Long> nearestMap = getNearestMap(cameraPresets);

        for(final CameraPreset cp : cameraPresets) {
            // CameraPreset contains camera and preset informations and
            // camera info is duplicated on every preset db line
            // So we take camera only once
            CameraStationFeature cameraStationFeature = cameraStationMap.get(cp.getCameraId());
            if (cameraStationFeature == null) {
                try {
                    cameraStationFeature = convert(nearestMap, cp);
                    cameraStationMap.put(cp.getCameraId(), cameraStationFeature);
                    collection.add(cameraStationFeature);
                } catch (final NonPublicRoadStationException nprse) {
                    //Skip non public roadstation
                    log.warn("Skipping: " + nprse.getMessage());
                    continue;
                }
            }
            cameraStationFeature.getProperties().addPreset(convertPreset(cp));
        }

        return collection;
    }

    private Map<Long, Long> getNearestMap(final List<CameraPreset> cameraPresets) {
        final Set<Long> wsIdList = cameraPresets.stream()
            .map(cp -> cp.getNearestWeatherStation())
            .filter(Objects::nonNull)
            .map(WeatherStation::getId)
            .collect(Collectors.toSet());
        final List<NearestRoadStation> nsList = wsIdList.isEmpty() ? Collections.emptyList() : cameraPresetRepository
            .findAllRoadStationNaturalIds(wsIdList);

        return nsList.stream().collect(Collectors.toMap(NearestRoadStation::getWeatherStationId, NearestRoadStation::getNearestNaturalId));
    }

    private CameraPresetDto convertPreset(final CameraPreset cp) {
        final CameraPresetDto dto = new CameraPresetDto();
        dto.setCameraId(cp.getCameraId());
        dto.setPresetId(cp.getPresetId());
        dto.setPresentationName(DataValidityHelper.nullifyUnknownValue(cp.getPresetName1()));
        dto.setResolution(cp.getResolution());
        dto.setDirectionCode(cp.getDirection());
        dto.setLotjuId(cp.getLotjuId());
        dto.setCameraLotjuId(cp.getCameraLotjuId());
        dto.setId(cp.getId());
        dto.setInCollection(cp.isInCollection());
        dto.setImageUrl(StringUtils.appendIfMissing(weathercamBaseurl, "/") + cp.getPresetId() + ".jpg");
        return dto;
    }

    private CameraStationFeature convert(final Map<Long, Long> nearestMap, final CameraPreset cp) throws NonPublicRoadStationException {
            final CameraStationFeature f = new CameraStationFeature();
            if (log.isDebugEnabled()) {
                log.debug("Convert: " + cp);
            }
            f.setId(ToStringHelper.nullSafeToString(cp.getRoadStationNaturalId().toString()));

            final CameraProperties properties = f.getProperties();

            // Camera properties
            properties.setId(cp.getId());

            properties.setLotjuId(cp.getCameraLotjuId());
            properties.setCameraId(cp.getCameraId());
            properties.setCameraType(cp.getCameraType());

            if(cp.getNearestWeatherStation() != null) {
                properties.setNearestWeatherStationNaturalId(nearestMap.get(cp.getNearestWeatherStation().getId()));
            }

            // RoadStation properties
            final RoadStation rs = cp.getRoadStation();
            setRoadStationProperties(properties, rs);

            setCoordinates(f, rs);

            return f;
    }
}
