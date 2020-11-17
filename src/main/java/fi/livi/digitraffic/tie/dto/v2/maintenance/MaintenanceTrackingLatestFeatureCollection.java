package fi.livi.digitraffic.tie.dto.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootFeatureCollectionDto;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "GeoJSON Feature Collection of maintenance trackings latest values", value = "MaintenanceTrackingLatestFeatureCollection")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class MaintenanceTrackingLatestFeatureCollection extends RootFeatureCollectionDto<MaintenanceTrackingLatestFeature> {

    public MaintenanceTrackingLatestFeatureCollection(final ZonedDateTime dataUpdatedTime, final ZonedDateTime dataLastCheckedTime,
                                                      final List<MaintenanceTrackingLatestFeature> features) {
        super(dataUpdatedTime, dataLastCheckedTime, features);
    }
}
