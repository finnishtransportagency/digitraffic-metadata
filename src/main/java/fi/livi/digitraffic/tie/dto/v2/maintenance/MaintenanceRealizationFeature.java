package fi.livi.digitraffic.tie.dto.v2.maintenance;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Object.", value = "MaintenanceRealizationFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class MaintenanceRealizationFeature extends Feature<LineString, MaintenanceRealizationProperties> {

    public MaintenanceRealizationFeature(final LineString geometry, final MaintenanceRealizationProperties properties) {
        super(geometry, properties);
    }

    @ApiModelProperty(value = "Maintenance realization properties preset properties.", required = true, position = 4)
    @Override
    public MaintenanceRealizationProperties getProperties() {
        return super.getProperties();
    }
}
