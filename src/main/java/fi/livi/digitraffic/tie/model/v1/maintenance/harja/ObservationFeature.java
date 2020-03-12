package fi.livi.digitraffic.tie.model.v1.maintenance.harja;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON ObservationFeature Object
 */
@ApiModel(description = "GeoJSON Feature Object", value = "ObservationFeature")
@JsonPropertyOrder({ "type", "geometry", "properties" })
public class ObservationFeature implements Feature<Geometry<?>>, Serializable {

    @ApiModelProperty(value = "\"Feature\": GeoJSON Feature Object", required = true, position = 1, allowableValues = "Feature")
    @JsonPropertyOrder(value = "1")
    private final String type = "Feature";

    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where the machine was located", required = true, position = 3)
    @JsonPropertyOrder(value = "3")
    private Geometry<?> geometry;

    @ApiModelProperty(value = "TMS station properties", required = true, position = 4)
    @JsonPropertyOrder(value = "4")
    private ObservationProperties properties;

    @JsonCreator
    public ObservationFeature(final Geometry<?> geometry, final ObservationProperties properties) {
        this.geometry = geometry;
        if (properties == null) {
            throw new IllegalArgumentException("ObservationProperties cannot be null");
        }
        this.properties = properties;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Geometry<?> getGeometry() {
        return geometry;
    }

    @Override
    public void setGeometry(final Geometry<?> geometry) {
        this.geometry = geometry;
    }

    public ObservationProperties getProperties() {
        return properties;
    }

    public void setProperties(final ObservationProperties properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    @JsonIgnore
    public Pair<Integer, Integer> getHarjaTyokoneUrakkaIdPair() {
        return Pair.of(getProperties().getWorkMachine().getId(), getProperties().getContractId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ObservationFeature)) {
            return false;
        }

        ObservationFeature that = (ObservationFeature) o;

        return new EqualsBuilder()
            .append(getType(), that.getType())
            .append(getGeometry(), that.getGeometry())
            .append(getProperties(), that.getProperties())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getType())
            .append(getGeometry())
            .append(getProperties())
            .toHashCode();
    }
}
