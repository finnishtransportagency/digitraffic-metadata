package fi.livi.digitraffic.tie.model.v1.forecastsection;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class RoadSegmentPK implements Serializable {

    @Column(name = "forecast_section_id", nullable = false)
    private Long forecastSectionId;

    @Column(name = "order_number", nullable = false)
    private Long orderNumber;

    public RoadSegmentPK() {
    }

    public RoadSegmentPK(Long forecastSectionId, Long orderNumber) {
        this.forecastSectionId = forecastSectionId;
        this.orderNumber = orderNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RoadSegmentPK that = (RoadSegmentPK) o;

        return new EqualsBuilder()
            .append(forecastSectionId, that.forecastSectionId)
            .append(orderNumber, that.orderNumber)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(forecastSectionId)
            .append(orderNumber)
            .toHashCode();
    }
}
