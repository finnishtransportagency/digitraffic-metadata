package fi.livi.digitraffic.tie.metadata.geojson.forecastsection;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON ForecastSectionFeature Object
 */
@ApiModel(description = "GeoJSON Feature Object", value = "ForecastSectionFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class ForecastSectionFeature {

    @ApiModelProperty(value = "\"Feature\": GeoJSON Feature Object", required = true, position = 1)
    @JsonPropertyOrder(value = "1")
    private final String type = "Feature";

    // TODO: Remove this from next version as it is just db id and real id is in properties
    @ApiModelProperty(value = "Forecast section id", required = true, position = 2)
    @JsonPropertyOrder(value = "2")
    private long id;

    @ApiModelProperty(value = "GeoJSON LineString Geometry Object. Points represent the road.", required = true, position = 3)
    @JsonPropertyOrder(value = "3")
    private LineString geometry;

    @ApiModelProperty(value = "Forecast section properties", required = true, position = 4)
    @JsonPropertyOrder(value = "4")
    private ForecastSectionProperties properties = new ForecastSectionProperties();

    public ForecastSectionFeature(long id, LineString geometry, ForecastSectionProperties properties) {
        this.id = id;
        this.geometry = geometry;
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public LineString getGeometry() {
        return geometry;
    }

    public void setGeometry(final LineString geometry) {
        this.geometry = geometry;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public ForecastSectionProperties getProperties() {
        return properties;
    }

    public void setProperties(final ForecastSectionProperties properties) {
        this.properties = properties;
    }

}
