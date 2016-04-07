package fi.livi.digitraffic.tie.metadata.geojson.roadweather;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON RoadWeatherStation Feature Object
 */
@ApiModel(description = "GeoJSON Feature Object of Road Weather Station", value = "Feature")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class RoadWeatherStationFeature {

    @ApiModelProperty(value = "\"Feature\"", required = true, position = 1)
    private final String type = "Feature";

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "Unique identifier for Road Weather Station", required = true, position = 2)
    private String id;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true, position = 3)
    private Point geometry;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "Road weather station properties", required = true, position = 4)
    private RoadWeatherStationProperties properties = new RoadWeatherStationProperties();

    public String getType() {
        return type;
    }

    public Point getGeometry() {
        return geometry;
    }

    public void setGeometry(final Point geometry) {
        this.geometry = geometry;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public RoadWeatherStationProperties getProperties() {
        return properties;
    }

    public void setProperties(final RoadWeatherStationProperties properties) {
        this.properties = properties;
    }

}
