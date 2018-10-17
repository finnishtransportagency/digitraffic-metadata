
package fi.livi.digitraffic.tie.data.model.json.maintenance;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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

    public Link() {
    }

    public Link(Integer id, Integer mValue) {
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

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Link link = (Link) o;

        return new EqualsBuilder()
            .append(id, link.id)
            .append(mValue, link.mValue)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .append(mValue)
            .toHashCode();
    }
}
