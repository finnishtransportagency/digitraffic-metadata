package fi.livi.digitraffic.tie.service.v1.forecastsection.dto;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Coordinate {

    public final BigDecimal longitude;

    public final BigDecimal latitude;

    public Coordinate(final List<BigDecimal> coordinate) {
        if (coordinate.size() >= 2) {
            this.longitude = coordinate.get(0);
            this.latitude = coordinate.get(1);
        } else if (coordinate.size() == 1) {
            this.longitude = coordinate.get(0);
            this.latitude = null;
        } else {
            this.longitude = null;
            this.latitude = null;
        }
    }

    public Coordinate(final BigDecimal longitude, final BigDecimal latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public boolean isValid() {
        return this.longitude != null && this.latitude != null;
    }

    @Override
    public String toString() {
        return "Coordinate{" +
               "longitude=" + longitude +
               ", latitude=" + latitude +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Coordinate that = (Coordinate) o;

        return new EqualsBuilder()
                .append(longitude, that.longitude)
                .append(latitude, that.latitude)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(longitude)
                .append(latitude)
                .toHashCode();
    }
}
