package fi.livi.digitraffic.tie.dto.v1;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
@ApiModel(value = "SensorValue", description = "Sensor value")
@JsonPropertyOrder({"id", "roadStationId", "name", "oldName", "shortName", "sensorValueId", "sensorValue", "sensorUnit", "timeWindowStart", "timeWindowEnd", "measuredTime"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorValueDto {

    @Id
    @JsonIgnore
    private Long sensorValueId;

    @ApiModelProperty(value = "Measured sensor value", required = true, position = 3)
    private double sensorValue;

    @ApiModelProperty(value = "Measured sensor value unit", required = true, position = 4)
    private String sensorUnit;

    @JsonProperty("roadStationId")
    private Long roadStationNaturalId;

    @ApiModelProperty(value = "Sensor type id (naturalId)", required = true, position = 2)
    @JsonProperty(value = "id")
    private Long sensorNaturalId;

    /** @deprecated */
    @Deprecated
    @ApiModelProperty(value = "Sensor old name. For new sensors will equal name. Will deprecate in future version.", position = 1, notes = "noteja")
    @JsonProperty("oldName")
    private String sensorNameOld;

    @ApiModelProperty(value = "Sensor name", position = 1, required = true)
    @JsonProperty(value = "name")
    private String sensorNameFi;

    @ApiModelProperty(value = "Sensor short name", position = 2, required = true)
    @JsonProperty(value = "shortName")
    private String sensorShortNameFi;

    @ApiModelProperty(value = "Additional information of sensor value [fi]")
    private String sensorValueDescriptionFi;

    @ApiModelProperty(value = "Additional information of sensor value [en]")
    private String sensorValueDescriptionEn;

    @JsonIgnore
    private ZonedDateTime stationLatestMeasuredTime;

    @ApiModelProperty(value = "Measurement time window start time (only for fixed time window sensors)")
    private ZonedDateTime timeWindowStart;

    @ApiModelProperty(value = "Measurement time window end time (only for fixed time window sensors)")
    private ZonedDateTime timeWindowEnd;

    /** Db's timestamp */
    @JsonIgnore
    private ZonedDateTime updatedTime;

    @ApiModelProperty(value = "Measurement time", position = 5)
    private ZonedDateTime measuredTime;

    public Long getRoadStationNaturalId() {
        return roadStationNaturalId;
    }

    public void setRoadStationNaturalId(final long roadStationNaturalId) {
        this.roadStationNaturalId = roadStationNaturalId;
    }

    public Long getSensorNaturalId() {
        return sensorNaturalId;
    }

    public void setSensorNaturalId(final Long sensorNaturalId) {
        this.sensorNaturalId = sensorNaturalId;
    }

    public Long getSensorValueId() {
        return sensorValueId;
    }

    public double getSensorValue() {
        return sensorValue;
    }

    public void setSensorValueId(final Long sensorValueId) {
        this.sensorValueId = sensorValueId;
    }

    public String getSensorNameOld() {
        return sensorNameOld;
    }

    public String getSensorUnit() {
        return sensorUnit;
    }

    public String getSensorValueDescriptionFi() {
        return sensorValueDescriptionFi;
    }

    public String getSensorValueDescriptionEn() {
        return sensorValueDescriptionEn;
    }

    public String getSensorNameFi() {
        return sensorNameFi;
    }

    public String getSensorShortNameFi() {
        return sensorShortNameFi;
    }

    public void setSensorShortNameFi(final String sensorShortNameFi) {
        this.sensorShortNameFi = sensorShortNameFi;
    }

    public ZonedDateTime getStationLatestMeasuredTime() {
        return stationLatestMeasuredTime;
    }

    public ZonedDateTime getTimeWindowStart() {
        return timeWindowStart;
    }

    public ZonedDateTime getTimeWindowEnd() {
        return timeWindowEnd;
    }

    public ZonedDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(final ZonedDateTime updated) {
        this.updatedTime = updated;
    }

    public ZonedDateTime getMeasuredTime() {
        return measuredTime;
    }

    public void setMeasuredTime(ZonedDateTime measuredTime) {
        this.measuredTime = measuredTime;
    }

    public static ZonedDateTime getStationLatestMeasurement(final List<SensorValueDto> sensorValues) {
        if (sensorValues != null && !sensorValues.isEmpty()) {
            return sensorValues.get(0).getStationLatestMeasuredTime();
        }
        return null;
    }
}
