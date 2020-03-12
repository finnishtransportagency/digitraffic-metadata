package fi.livi.digitraffic.tie.model.v1.forecastsection;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class ForecastSectionCoordinatePK implements Serializable {

    @Column(name = "forecast_section_id", nullable = false)
    private Long forecastSectionId;

    @Column(name = "list_order_number", nullable = false)
    private Long listOrderNumber;

    @Column(name = "order_number", nullable = false)
    private Long orderNumber;

    public ForecastSectionCoordinatePK() {
    }

    public ForecastSectionCoordinatePK(Long forecastSectionId, Long listOrderNumber, Long orderNumber) {
        this.forecastSectionId = forecastSectionId;
        this.listOrderNumber = listOrderNumber;
        this.orderNumber = orderNumber;
    }

    public Long getForecastSectionId() {
        return forecastSectionId;
    }

    public Long getListOrderNumber() {
        return listOrderNumber;
    }

    public Long getOrderNumber() {
        return orderNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ForecastSectionCoordinatePK that = (ForecastSectionCoordinatePK) o;

        return new EqualsBuilder()
            .append(forecastSectionId, that.forecastSectionId)
            .append(listOrderNumber, that.listOrderNumber)
            .append(orderNumber, that.orderNumber)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(forecastSectionId)
            .append(listOrderNumber)
            .append(orderNumber)
            .toHashCode();
    }
}
