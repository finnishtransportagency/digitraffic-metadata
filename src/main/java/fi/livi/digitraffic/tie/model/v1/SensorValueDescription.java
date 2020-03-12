package fi.livi.digitraffic.tie.model.v1;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Immutable;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(description = "Additional information of sensor values")
@Entity
@DynamicUpdate
@Immutable
public class SensorValueDescription implements Comparable<SensorValueDescription> {

    @EmbeddedId
    SensorValueDescriptionPK sensorValueDescriptionPK;

    @ApiModelProperty(value = "Sensor description [en]", position = 2)
    private String descriptionEn;

    @ApiModelProperty(value = "Sensor description [fi]")
    private String descriptionFi;

    @ApiModelProperty(value = "Sensor value")
    public Double getSensorValue() {
        return sensorValueDescriptionPK.getSensorValue();
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(final String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getDescriptionFi() {
        return descriptionFi;
    }

    public void setDescriptionFi(final String descriptionFi) {
        this.descriptionFi = descriptionFi;
    }

    @Override
    public String toString() {
        return new ToStringHelper(this)
                .appendField("sensorId", sensorValueDescriptionPK.getSensorId())
                .appendField("sensorValue", this.getSensorValue())
                .appendField("descriptionEn", getDescriptionEn())
                .appendField("descriptionFi", getDescriptionFi())
                .toString();
    }

    @Override
    public int compareTo(SensorValueDescription o) {
        if (getSensorValue() == null && o.getSensorValue() == null) {
            return 0;
        } else if (o.getSensorValue() == null) {
            return -1;
        } else if (getSensorValue() == null) {
            return 1;
        }
        return getSensorValue().compareTo(o.getSensorValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SensorValueDescription that = (SensorValueDescription) o;

        return new EqualsBuilder()
            .append(sensorValueDescriptionPK, that.sensorValueDescriptionPK)
            .append(descriptionEn, that.descriptionEn)
            .append(descriptionFi, that.descriptionFi)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(sensorValueDescriptionPK)
            .append(descriptionEn)
            .append(descriptionFi)
            .toHashCode();
    }
}
