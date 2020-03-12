package fi.livi.digitraffic.tie.metadata.geojson.camera;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON CameraPresetFeature Object
 */
@ApiModel(description = "GeoJSON Feature Object.", value = "CameraStationFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class CameraStationFeature implements Feature<Point> {

    @ApiModelProperty(value = "\"Feature\": GeoJSON Feature Object", required = true, position = 1, allowableValues = "Feature")
    private final String type = "Feature";

    @ApiModelProperty(value = "Road station id, same as CameraStationProperties.roadStationId", required = true, position = 2)
    private String id;

    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true, position = 3)
    private Point geometry;

    @ApiModelProperty(value = "Camera preset properties.", required = true, position = 4)
    private CameraProperties properties = new CameraProperties();

    public String getType() {
        return type;
    }

    @Override
    public Point getGeometry() {
        return geometry;
    }

    @Override
    public void setGeometry(final Point geometry) {
        this.geometry = geometry;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public CameraProperties getProperties() {
        return properties;
    }

    public void setProperties(final CameraProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final CameraStationFeature that = (CameraStationFeature) o;

        return new EqualsBuilder()
            .append(type, that.type)
            .append(id, that.id)
            .append(geometry, that.geometry)
            .append(properties, that.properties)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(type)
            .append(id)
            .append(geometry)
            .append(properties)
            .toHashCode();
    }
}
