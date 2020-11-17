package fi.livi.digitraffic.tie.metadata.geojson.tms;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootFeatureCollectionDto;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "GeoJSON Feature Collection of TMS stations", value = "TmsStationFeatureCollection")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class TmsStationFeatureCollection extends RootFeatureCollectionDto<TmsStationFeature> {

    public TmsStationFeatureCollection(final ZonedDateTime dataUpdatedTime, final ZonedDateTime dataLastCheckedTime,
                                       final List<TmsStationFeature> features) {
        super(dataUpdatedTime, dataLastCheckedTime, features);
    }
}
