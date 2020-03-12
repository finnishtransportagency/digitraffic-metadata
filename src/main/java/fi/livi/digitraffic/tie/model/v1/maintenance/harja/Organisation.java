
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
    "name",
    "businessId"
})
public class Organisation implements Serializable
{
    private String name;

    @JsonProperty(required = true)
    private String businessId;

    @JsonCreator
    public Organisation(final String name, final String businessId) {
        this.name = name;
        this.businessId = businessId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(final String businessId) {
        this.businessId = businessId;
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

        if (!(o instanceof Organisation)) {
            return false;
        }

        Organisation that = (Organisation) o;

        return new EqualsBuilder()
            .append(getName(), that.getName())
            .append(getBusinessId(), that.getBusinessId())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getName())
            .append(getBusinessId())
            .toHashCode();
    }
}
