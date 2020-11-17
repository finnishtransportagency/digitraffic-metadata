package fi.livi.digitraffic.tie.metadata.geojson;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public abstract class Feature<G extends Geometry<?>, P extends Properties>  extends GeoJsonObject {

    @ApiModelProperty(value = "Type of GeoJSON Object",
                      allowableValues = "Feature",
                      required = true,
                      example = "Feature",
                      position = 2)
    private final String type = "Feature";

    @ApiModelProperty(value = "GeoJSON Geometry Object",
                      required = true,
                      position = 3)
    @JsonProperty("geometry")
    private G geometry;

    @ApiModelProperty(value = "GeoJSON Properties Object",
                      required = true,
                      position = 4)
    private P properties;

    public Feature() {
    }

    public Feature(final P properties) {
        this.properties = properties;
    }

    public Feature(final G geometry, final P properties) {
        this.geometry = geometry;
        this.properties = properties;
    }

    @Override
    public String getType() {
        return type;
    }

    public G getGeometry() {
        return geometry;
    }

    public void setGeometry(final G geometry) {
        this.geometry = geometry;
    }

    public P getProperties() {
        return properties;
    }

    public void setProperties(final P properties) {
        this.properties = properties;
    }
}