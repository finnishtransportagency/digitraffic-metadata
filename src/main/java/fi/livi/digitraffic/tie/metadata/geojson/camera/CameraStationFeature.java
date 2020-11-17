package fi.livi.digitraffic.tie.metadata.geojson.camera;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON CameraPresetFeature Object
 */
@ApiModel(description = "GeoJSON Feature Object.", value = "CameraStationFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class CameraStationFeature extends Feature<Point, CameraProperties> {

    // TODO: Remove this from next version as it is duplicated in properties
    @ApiModelProperty(value = "Road station id, same as CameraStationProperties.roadStationId", required = true, position = 2)
    private String id;

    public CameraStationFeature(final Point geometry, final CameraProperties properties) {
        super(geometry, properties);
        this.id = ToStringHelper.nullSafeToString(properties.getNaturalId());
    }

    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true, position = 3, allowableValues = "Point")
    @Override
    public Point getGeometry() {
        return super.getGeometry();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final CameraStationFeature that = (CameraStationFeature) o;

        return new EqualsBuilder()
            .append(getType(), that.getType())
            .append(id, that.id)
            .append(getGeometry(), that.getGeometry())
            .append(getProperties(), that.getProperties())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getType())
            .append(id)
            .append(getGeometry())
            .append(getProperties())
            .toHashCode();
    }
}
