package fi.livi.digitraffic.tie.model.v1.location;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class LocationSubtypeKey implements Serializable {
    private String subtypeCode;

    private String version;

    public LocationSubtypeKey() {}

    public LocationSubtypeKey(final String version, final String subtypeCode) {
        this.version = version;
        this.subtypeCode = subtypeCode;
    }

    public String getSubtypeCode() {
        return subtypeCode;
    }

    public void setSubtypeCode(String subtypeCode) {
        this.subtypeCode = subtypeCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean validate() {
        return isNotEmpty(subtypeCode) && isNotEmpty(version);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final LocationSubtypeKey that = (LocationSubtypeKey) o;

        return new EqualsBuilder()
                .append(subtypeCode, that.subtypeCode)
                .append(version, that.version)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(subtypeCode)
                .append(version)
                .toHashCode();
    }

}
