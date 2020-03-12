package fi.livi.digitraffic.tie.model.v1.location;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class LocationTypeKey implements Serializable{
    private String typeCode;

    private String version;

    public LocationTypeKey() {}

    public LocationTypeKey(final String version, final String typeCode) {
        this.version = version;
        this.typeCode = typeCode;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean validate() {
        return isNotEmpty(typeCode) && isNotEmpty(version);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final LocationTypeKey that = (LocationTypeKey) o;

        return new EqualsBuilder()
                .append(typeCode, that.typeCode)
                .append(version, that.version)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(typeCode)
                .append(version)
                .toHashCode();
    }
}
