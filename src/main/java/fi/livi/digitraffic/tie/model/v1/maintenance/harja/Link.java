
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
    "id",
    "mValue"
})
public class Link implements Serializable {

    @JsonProperty(required = true)
    private Integer id;

    @JsonProperty(required = true)
    private Integer mValue;

    @JsonCreator
    public Link(final Integer id, final Integer mValue) {
        this.id = id;
        this.mValue = mValue;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMValue() {
        return mValue;
    }

    public void setMValue(Integer mValue) {
        this.mValue = mValue;
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

        if (!(o instanceof Link)) {
            return false;
        }

        Link link = (Link) o;

        return new EqualsBuilder()
            .append(getId(), link.getId())
            .append(mValue, link.mValue)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getId())
            .append(mValue)
            .toHashCode();
    }
}
