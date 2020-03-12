
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
    "system",
    "organisation"
})
public class Sender implements Serializable
{
    @JsonProperty(required = true)
    private String system;

    private Organisation organisation;

    @JsonCreator
    public Sender(final String system, final Organisation organisation) {
        this.system = system;
        this.organisation = organisation;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(final String system) {
        this.system = system;
    }

    @JsonProperty("organisation")
    public Organisation getOrganisation() {
        return organisation;
    }

    @JsonProperty("organisation")
    public void setOrganisation(final Organisation organisation) {
        this.organisation = organisation;
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

        if (!(o instanceof Sender)) {
            return false;
        }

        Sender sender = (Sender) o;

        return new EqualsBuilder()
            .append(getSystem(), sender.getSystem())
            .append(getOrganisation(), sender.getOrganisation())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getSystem())
            .append(getOrganisation())
            .toHashCode();
    }
}
