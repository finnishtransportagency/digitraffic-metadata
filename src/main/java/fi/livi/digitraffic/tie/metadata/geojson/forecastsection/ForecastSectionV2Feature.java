package fi.livi.digitraffic.tie.metadata.geojson.forecastsection;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Object", value = "ForecastSectionFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class ForecastSectionV2Feature {

    @ApiModelProperty(value = "\"Feature\": GeoJSON Feature Object", required = true, position = 1)
    @JsonPropertyOrder(value = "1")
    private final String type = "Feature";

    // TODO: Remove this from next version as it is just db id and real id is in properties
    @ApiModelProperty(value = "Forecast section id", required = true, position = 2)
    @JsonPropertyOrder(value = "2")
    private long id;

    @ApiModelProperty(value = "GeoJSON MultiLineString Geometry Object. Points represent the road.", required = true, position = 3)
    @JsonPropertyOrder(value = "3")
    private MultiLineString geometry;

    @ApiModelProperty(value = "Forecast section properties", required = true, position = 4)
    @JsonPropertyOrder(value = "4")
    private ForecastSectionV2Properties properties = new ForecastSectionV2Properties();

    public ForecastSectionV2Feature(long id, final MultiLineString geometry, final ForecastSectionV2Properties properties) {
        this.id = id;
        this.geometry = geometry;
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public MultiLineString getGeometry() {
        return geometry;
    }

    public void setGeometry(final MultiLineString geometry) {
        this.geometry = geometry;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public ForecastSectionV2Properties getProperties() {
        return properties;
    }

    public void setProperties(final ForecastSectionV2Properties properties) {
        this.properties = properties;
    }

}
