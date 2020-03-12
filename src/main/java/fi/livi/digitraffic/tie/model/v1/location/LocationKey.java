package fi.livi.digitraffic.tie.model.v1.location;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class LocationKey implements Serializable {
    private Integer locationCode;

    private String version;

    public LocationKey() {}

    public LocationKey(final String version, final Integer locationCode) {
        this.version = version;
        this.locationCode = locationCode;
    }

    public Integer getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(Integer locationCode) {
        this.locationCode = locationCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final LocationKey that = (LocationKey) o;

        return new EqualsBuilder()
                .append(locationCode, that.locationCode)
                .append(version, that.version)
                .isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(locationCode)
                .append(version)
                .toHashCode();
    }
}
