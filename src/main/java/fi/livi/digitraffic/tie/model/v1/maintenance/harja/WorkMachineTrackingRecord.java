
package fi.livi.digitraffic.tie.model.v1.maintenance.harja;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "caption",
    "observationFeatureCollection"
})
public class WorkMachineTrackingRecord implements Serializable {

    @JsonProperty("caption")
    private Caption caption;

    @JsonProperty(required = true)
    private ObservationFeatureCollection observationFeatureCollection;

    @JsonCreator
    public WorkMachineTrackingRecord(final Caption caption,
                                     final ObservationFeatureCollection observationFeatureCollection) {
        this.caption = caption;
        this.observationFeatureCollection = observationFeatureCollection;
    }

    public Caption getCaption() {
        return caption;
    }

    public void setCaption(final Caption caption) {
        this.caption = caption;
    }

    public ObservationFeatureCollection getObservationFeatureCollection() {
        return observationFeatureCollection;
    }

    public void setObservationFeatureCollection(ObservationFeatureCollection observationFeatureCollection) {
        this.observationFeatureCollection = observationFeatureCollection;
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

        if (!(o instanceof WorkMachineTrackingRecord)) {
            return false;
        }

        WorkMachineTrackingRecord that = (WorkMachineTrackingRecord) o;

        return new EqualsBuilder()
            .append(getCaption(), that.getCaption())
            .append(getObservationFeatureCollection(), that.getObservationFeatureCollection())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getCaption())
            .append(getObservationFeatureCollection())
            .toHashCode();
    }
}
