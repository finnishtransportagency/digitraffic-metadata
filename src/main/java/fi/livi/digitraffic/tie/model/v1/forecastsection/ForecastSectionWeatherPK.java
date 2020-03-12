package fi.livi.digitraffic.tie.model.v1.forecastsection;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class ForecastSectionWeatherPK implements Serializable {

    @Column(name = "forecast_section_id", nullable = false)
    private Long forecastSectionId;

    @Column(name = "forecast_name", nullable = false)
    private char[] forecastName;

    public long getForecastSectionId() {
        return forecastSectionId;
    }

    public char[] getForecastName() {
        return forecastName;
    }

    public ForecastSectionWeatherPK() {
    }

    public ForecastSectionWeatherPK(long forecastSectionId, String forecastName) {
        this.forecastSectionId = forecastSectionId;
        this.forecastName = forecastName.toCharArray();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ForecastSectionWeatherPK that = (ForecastSectionWeatherPK) o;

        return new EqualsBuilder()
                .append(forecastSectionId, that.forecastSectionId)
                .append(forecastName, that.forecastName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(forecastSectionId)
                .append(forecastName)
                .toHashCode();
    }
}
