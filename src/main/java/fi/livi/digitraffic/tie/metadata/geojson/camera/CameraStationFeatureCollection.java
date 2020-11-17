package fi.livi.digitraffic.tie.metadata.geojson.camera;

import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootFeatureCollectionDto;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "GeoJSON Feature Collection of Cameras with presets", value = "CameraStationFeatureCollection")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class CameraStationFeatureCollection extends RootFeatureCollectionDto<CameraStationFeature> {

    public CameraStationFeatureCollection(final ZonedDateTime dataUpdatedTime, final ZonedDateTime dataLastCheckedTime,
                                          final List<CameraStationFeature> features) {
        super(dataUpdatedTime, dataLastCheckedTime, features);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final CameraStationFeatureCollection that = (CameraStationFeatureCollection) o;

        return new EqualsBuilder()
                .append(getType(), that.getType())
                .append(getFeatures(), that.getFeatures())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getType())
                .append(getFeatures())
                .toHashCode();
    }
}
