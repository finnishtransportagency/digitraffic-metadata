package fi.livi.digitraffic.tie.model.v1.maintenance.harja;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of Observations", value = "ObservationFeatureCollection")
@JsonPropertyOrder({ "type", "features" })
public class ObservationFeatureCollection implements Serializable {

    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    private final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    private final List<ObservationFeature> features;

    @JsonCreator
    public ObservationFeatureCollection(final List<ObservationFeature> features) {
        this.features = features;
    }

    public String getType() {
        return type;
    }

    public List<ObservationFeature> getFeatures() {
        return features;
    }

    public void addAll(final Collection<ObservationFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ObservationFeatureCollection)) {
            return false;
        }

        ObservationFeatureCollection that = (ObservationFeatureCollection) o;

        return new EqualsBuilder()
            .append(getType(), that.getType())
            .append(getFeatures(), that.getFeatures())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getType())
            .append(getFeatures())
            .toHashCode();
    }
}
