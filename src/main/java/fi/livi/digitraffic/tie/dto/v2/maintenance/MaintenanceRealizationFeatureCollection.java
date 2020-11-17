package fi.livi.digitraffic.tie.dto.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootFeatureCollectionDto;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "GeoJSON Feature Collection of Maintenance Realizations", value = "MaintenanceRealizationFeatureCollection")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class MaintenanceRealizationFeatureCollection extends RootFeatureCollectionDto<MaintenanceRealizationFeature> {

    public MaintenanceRealizationFeatureCollection(final ZonedDateTime dataUpdatedTime, final ZonedDateTime dataLastCheckedTime, final List<MaintenanceRealizationFeature> features) {
        super(dataUpdatedTime, dataLastCheckedTime, features);
    }
}
