package fi.livi.digitraffic.tie.dto.v2.maintenance;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Object.", value = "MaintenanceRealizationFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class MaintenanceRealizationFeature implements Feature<LineString> {

    @ApiModelProperty(value = "GeoJSON LineString Geometry Object containing route points", required = true, position = 3)
    private LineString geometry;

    @ApiModelProperty(value = "Camera preset properties.", required = true, position = 4)
    private final MaintenanceRealizationProperties properties;

    public MaintenanceRealizationFeature(final LineString geometry, final MaintenanceRealizationProperties properties) {
        this.geometry = geometry;
        this.properties = properties;
    }

    @ApiModelProperty(value = "\"Feature\": GeoJSON Feature Object", required = true, position = 1, allowableValues = "Feature")
    @Override
    public String getType() {
        return "Feature";
    }


    @Override
    public LineString getGeometry() {
        return geometry;
    }

    @Override
    public void setGeometry(final LineString geometry) {
        this.geometry = geometry;
    }

    public MaintenanceRealizationProperties getProperties() {
        return properties;
    }
}
