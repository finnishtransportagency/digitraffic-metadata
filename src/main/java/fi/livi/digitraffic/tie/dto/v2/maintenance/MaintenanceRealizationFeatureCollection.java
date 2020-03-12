package fi.livi.digitraffic.tie.dto.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootMetadataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of Maintenance Realizations", value = "MaintenanceRealizationFeatureCollection")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class MaintenanceRealizationFeatureCollection extends RootMetadataObjectDto {

    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    public final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    public final List<MaintenanceRealizationFeature> features;

    public MaintenanceRealizationFeatureCollection(final ZonedDateTime dataUpdatedTime, final ZonedDateTime dataLastCheckedTime, final List<MaintenanceRealizationFeature> features) {
        super(dataUpdatedTime, dataLastCheckedTime);
        this.features = features;
    }

    @Override
    public String toString() {
        return "MaintenanceRealizationFeatureCollection{" + "features=" + features + '}';
    }
}
