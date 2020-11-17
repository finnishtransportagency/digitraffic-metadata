package fi.livi.digitraffic.tie.metadata.geojson.weather;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON WeatherStation Feature Object
 */
@ApiModel(description = "GeoJSON Feature Object of Weather Station", value = "WeatherStationFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class WeatherStationFeature extends Feature<Point, WeatherStationProperties> {

    // TODO: Remove this from next version as it is duplicated in properties
    @ApiModelProperty(value = "Road station id, same as WeatherStationProperties.roadStationId", required = true, position = 2)
    private long id;

    public WeatherStationFeature(final Point geometry, final WeatherStationProperties properties, final long id) {
        super(geometry, properties);
        this.id = id;
    }

    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true, position = 3)
    @Override
    public Point getGeometry() {
        return super.getGeometry();
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }
}
