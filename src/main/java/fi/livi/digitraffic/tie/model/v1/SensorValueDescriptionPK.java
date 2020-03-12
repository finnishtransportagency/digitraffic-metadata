package fi.livi.digitraffic.tie.model.v1;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class SensorValueDescriptionPK implements Serializable {

    @Column(nullable = false)
    private long sensorId;

    @Column(nullable = false)
    private Double sensorValue;

    public long getSensorId() {
        return sensorId;
    }

    public void setSensorId(final long sensorId) {
        this.sensorId = sensorId;
    }

    public Double getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(final Double sensorValue) {
        this.sensorValue = sensorValue;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final SensorValueDescriptionPK that = (SensorValueDescriptionPK) o;

        return new EqualsBuilder()
                .append(sensorId, that.sensorId)
                .append(sensorValue, that.sensorValue)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(sensorId)
                .append(sensorValue)
                .toHashCode();
    }
}
