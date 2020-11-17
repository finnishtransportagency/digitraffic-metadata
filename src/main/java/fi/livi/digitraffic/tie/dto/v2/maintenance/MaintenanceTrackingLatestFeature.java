package fi.livi.digitraffic.tie.dto.v2.maintenance;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Object.", value = "MaintenanceTrackingLatestFeature")
@JsonPropertyOrder({ "type", "properties", "geometry" })
public class MaintenanceTrackingLatestFeature extends Feature<Geometry<?>, MaintenanceTrackingLatestProperties> {

    public MaintenanceTrackingLatestFeature(final Geometry<?> geometry, final MaintenanceTrackingLatestProperties properties) {
        super(geometry, properties);
    }

    @ApiModelProperty(value = "GeoJSON Point or LineString Geometry Object containing route point(s)", required = true, position = 3)
    public Geometry<?> getGeometry() {
        return super.getGeometry();
    }
}
