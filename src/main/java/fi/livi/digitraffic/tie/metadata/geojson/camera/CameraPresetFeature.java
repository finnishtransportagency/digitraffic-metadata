package fi.livi.digitraffic.tie.metadata.geojson.camera;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON CameraPresetFeature Object
 */
@ApiModel(description = "GeoJSON CameraPresetFeature Object.", value = "CameraPresetFeature")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class CameraPresetFeature {

    @ApiModelProperty(value = "\"Feature\": GeoJSON Feature Object", required = true, position = 1)
    private final String type = "Feature";

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "Same as presetId in CameraPresetProperties (Camera preset id)", required = true, position = 2)
    private String id;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true, position = 3)
    private Point geometry;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "Camera preset properties.", required = true, position = 4)
    private CameraPresetProperties properties = new CameraPresetProperties();

    public String getType() {
        return type;
    }

    public Point getGeometry() {
        return geometry;
    }

    public void setGeometry(final Point geometry) {
        this.geometry = geometry;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public CameraPresetProperties getProperties() {
        return properties;
    }

    public void setProperties(final CameraPresetProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        CameraPresetFeature rhs = (CameraPresetFeature) obj;
        return new EqualsBuilder()
                .append(this.type, rhs.type)
                .append(this.id, rhs.id)
                .append(this.geometry, rhs.geometry)
                .append(this.properties, rhs.properties)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(type)
                .append(id)
                .append(geometry)
                .append(properties)
                .toHashCode();
    }
}
