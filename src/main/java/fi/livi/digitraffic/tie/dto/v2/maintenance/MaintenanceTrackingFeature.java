package fi.livi.digitraffic.tie.dto.v2.maintenance;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Object.", value = "MaintenanceTrackingFeature")
@JsonPropertyOrder({ "type", "properties", "geometry" })
public class MaintenanceTrackingFeature extends Feature<Geometry<?>, MaintenanceTrackingProperties> {

    public MaintenanceTrackingFeature(final Geometry<?> geometry, final MaintenanceTrackingProperties properties) {
        super(geometry, properties);
    }

    @ApiModelProperty(value = "GeoJSON Feature Object", required = true, position = 1, allowableValues = "Feature")
    @Override
    public String getType() {
        return "Feature";
    }

    @ApiModelProperty(value = "GeoJSON Point or LineString Geometry Object containing route point(s)", required = true, position = 3)
    @Override
    public Geometry<?> getGeometry() {
        return super.getGeometry();
    }
}
