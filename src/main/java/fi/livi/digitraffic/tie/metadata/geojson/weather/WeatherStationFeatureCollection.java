package fi.livi.digitraffic.tie.metadata.geojson.weather;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootFeatureCollectionDto;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "GeoJSON Feature Collection of Weather Stations", value = "WeatherStationFeatureCollection")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class WeatherStationFeatureCollection extends RootFeatureCollectionDto<WeatherStationFeature> {

    public WeatherStationFeatureCollection(final ZonedDateTime dataUpdatedTime, final ZonedDateTime dataLastCheckedTime,
                                           final List<WeatherStationFeature> features) {
        super(dataUpdatedTime, dataLastCheckedTime, features);
    }
}
