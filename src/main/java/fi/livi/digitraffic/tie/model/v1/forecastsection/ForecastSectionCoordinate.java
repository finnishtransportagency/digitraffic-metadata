package fi.livi.digitraffic.tie.model.v1.forecastsection;

import java.math.BigDecimal;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
public class ForecastSectionCoordinate {

    @EmbeddedId
    private ForecastSectionCoordinatePK forecastSectionCoordinatePK;

    @NotNull
    private BigDecimal longitude;

    @NotNull
    private BigDecimal latitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({@JoinColumn(name="forecast_section_id", referencedColumnName = "forecast_section_id", nullable = false, insertable = false, updatable = false),
                  @JoinColumn(name="list_order_number", referencedColumnName = "order_number", nullable = false, insertable = false, updatable = false)})
    @Fetch(FetchMode.JOIN)
    private ForecastSectionCoordinateList forecastSectionCoordinateList;

    public ForecastSectionCoordinate() {
    }

    public ForecastSectionCoordinate(final ForecastSectionCoordinatePK forecastSectionCoordinatePK,
                                     final @NotNull BigDecimal longitude, final @NotNull BigDecimal latitude) {
        this.forecastSectionCoordinatePK = forecastSectionCoordinatePK;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Long getForecastSectionId() {
        return forecastSectionCoordinatePK.getForecastSectionId();
    }

    public Long getListOrderNumber() {
        return forecastSectionCoordinatePK.getListOrderNumber();
    }

    public Long getOrderNumber() {
        return forecastSectionCoordinatePK.getOrderNumber();
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public void removeCoordinate() {
        this.forecastSectionCoordinateList = null;
    }
}
