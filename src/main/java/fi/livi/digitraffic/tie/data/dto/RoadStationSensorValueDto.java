package fi.livi.digitraffic.tie.data.dto;

import java.time.LocalDateTime;

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
@ApiModel(value = "RoadStationSensorValue", description = "Road wather station sensor value")
@JsonPropertyOrder(value = { "sensorNameFi", "sensorNameOld", "sensorShortNameFi", "sensorValueId", "sensorValue", "sensorUnit", "sensorValueMeasuredLocalTime", "conditionUpdatedUtc"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoadStationSensorValueDto /*implements MeasuredDataObjectDto*/ {

    @Id
    @JsonIgnore
    private long sensorValueId;

    @ApiModelProperty(value = "Measured sensor value", required = true, position = 3)
    private double sensorValue;

    @ApiModelProperty(value = "Measured sensor value unit", required = true, position = 4)
    private String sensorUnit;

    @JsonIgnore
    private long roadStationNaturalId;

    @JsonIgnore
    private long roadStationId;

    @ApiModelProperty(value = "Sensor type id (naturalId)", required = true, position = 2)
    @JsonProperty(value = "id")
    private long sensorNaturalId;

    @JsonIgnore
    private long sensorId;

    @ApiModelProperty(value = "Sensor old name. For new sensors will equal sensorNameFi. Will deprecate in future.", position = 1, notes = "noteja")
    private String sensorNameOld;

    @ApiModelProperty(value = "Sensor name [fi]", position = 1, required = true)
    private String sensorNameFi;

    @ApiModelProperty(value = "Sensor short name [fi]", position = 1, required = true)
    private String sensorShortNameFi;

    @ApiModelProperty(value = "Additional information of sensor value [fi]")
    private String sensorValueDescriptionFi;

    @ApiModelProperty(value = "Additional information of sensor value [en]")
    private String sensorValueDescriptionEn;

    @JsonIgnore
    private LocalDateTime measured;

    @JsonIgnore
    private LocalDateTime stationLatestMeasured;

    public long getRoadStationNaturalId() {
        return roadStationNaturalId;
    }

    public void setRoadStationNaturalId(final long roadStationNaturalId) {
        this.roadStationNaturalId = roadStationNaturalId;
    }

    public long getRoadStationId() {
        return roadStationId;
    }

    public void setRoadStationId(final long roadStationId) {
        this.roadStationId = roadStationId;
    }

    public long getSensorNaturalId() {
        return sensorNaturalId;
    }

    public void setSensorNaturalId(final long sensorNaturalId) {
        this.sensorNaturalId = sensorNaturalId;
    }

    public long getSensorId() {
        return sensorId;
    }

    public void setSensorId(final long sensorId) {
        this.sensorId = sensorId;
    }

    public long getSensorValueId() {
        return sensorValueId;
    }

    public void setSensorValueId(final long sensorValueId) {
        this.sensorValueId = sensorValueId;
    }

    public double getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(final double sensorValue) {
        this.sensorValue = sensorValue;
    }

    public String getSensorNameOld() {
        return sensorNameOld;
    }

    public void setSensorNameOld(final String sensorNameOld) {
        this.sensorNameOld = sensorNameOld;
    }

    public String getSensorUnit() {
        return sensorUnit;
    }

    public void setSensorUnit(final String sensorUnit) {
        this.sensorUnit = sensorUnit;
    }

    public String getSensorValueDescriptionFi() {
        return sensorValueDescriptionFi;
    }

    public void setSensorValueDescriptionFi(final String sensorValueDescriptionFi) {
        this.sensorValueDescriptionFi = sensorValueDescriptionFi;
    }

    public String getSensorValueDescriptionEn() {
        return sensorValueDescriptionEn;
    }

    public void setSensorValueDescriptionEn(final String sensorValueDescriptionEn) {
        this.sensorValueDescriptionEn = sensorValueDescriptionEn;
    }

    public String getSensorNameFi() {
        return sensorNameFi;
    }

    public void setSensorNameFi(final String sensorNameFi) {
        this.sensorNameFi = sensorNameFi;
    }

    public String getSensorShortNameFi() {
        return sensorShortNameFi;
    }

    public void setSensorShortNameFi(final String sensorShortNameFi) {
        this.sensorShortNameFi = sensorShortNameFi;
    }

    public LocalDateTime getMeasured() {
        return measured;
    }

    public void setMeasured(final LocalDateTime measured) {
        this.measured = measured;
    }

    public LocalDateTime getStationLatestMeasured() {
        return stationLatestMeasured;
    }

    public void setStationLatestMeasured(final LocalDateTime stationLatestMeasured) {
        this.stationLatestMeasured = stationLatestMeasured;
    }
}
