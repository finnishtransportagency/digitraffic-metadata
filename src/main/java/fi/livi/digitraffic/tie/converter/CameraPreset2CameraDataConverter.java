package fi.livi.digitraffic.tie.converter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.feature.AbstractMetadataToFeatureConverter;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraPresetDataDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraStationDataDto;
import fi.livi.digitraffic.tie.helper.DataValidityHelper;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;

@ConditionalOnWebApplication
@Component
public final class CameraPreset2CameraDataConverter extends AbstractMetadataToFeatureConverter {
    private static final Logger log = LoggerFactory.getLogger( CameraPreset2CameraDataConverter.class );

    private final String weathercamBaseurl;

    @Autowired
    public CameraPreset2CameraDataConverter(@Value("${weathercam.baseUrl}") final
                                            String weathercamBaseurl,
                                            final CoordinateConverter coordinateConverter) {
        super(coordinateConverter);
        this.weathercamBaseurl = weathercamBaseurl;
    }

    public CameraRootDataObjectDto convert(final List<CameraPreset> cameraPresets,
                                           final ZonedDateTime updated) {
        final List<CameraStationDataDto> collection = new ArrayList<>();

        // Cameras mapped with cameraId
        final Map<String, CameraStationDataDto> cameraStationMap = new HashMap<>();

        for(final CameraPreset cp : cameraPresets) {
            // CameraPreset contains camera and preset informations and
            // camera info is duplicated on every preset db line
            // So we take camera only once
            if ( cp.isPublic() ) {
                CameraStationDataDto cameraStationFeature = cameraStationMap.get(cp.getCameraId());
                if (cameraStationFeature == null) {
                    cameraStationFeature = convert(cp);
                    cameraStationMap.put(cp.getCameraId(), cameraStationFeature);
                    collection.add(cameraStationFeature);
                }
                final CameraPresetDataDto preset = convertPreset(cp);
                cameraStationFeature.addPreset(preset);
            }
        }

        return new CameraRootDataObjectDto(collection, updated);
    }

    private CameraPresetDataDto convertPreset(final CameraPreset cp) {
        final CameraPresetDataDto dto = new CameraPresetDataDto();
        dto.setMeasuredTime(cp.getPictureLastModified());
        dto.setId(cp.getPresetId());
        dto.setPresentationName(DataValidityHelper.nullifyUnknownValue(cp.getPresetName1()));
        dto.setImageUrl(StringUtils.appendIfMissing(weathercamBaseurl, "/") + cp.getPresetId() + ".jpg");
        return dto;
    }

    private static CameraStationDataDto convert(final CameraPreset cp) {
        final CameraStationDataDto c = new CameraStationDataDto();
        if (log.isDebugEnabled()) {
            log.debug("Convert: " + cp);
        }
        c.setId(cp.getCameraId());
        c.setRoadStationId(cp.getRoadStationNaturalId());
        c.setNearestWeatherStationId(cp.getNearestWeatherStationNaturalId());
        return c;
    }
}
