package fi.livi.digitraffic.tie.dto.v1.tms;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "TmsFreeFlowSpeedData")
@Entity
@Immutable
public class TmsFreeFlowSpeedDto {

    @Id
    @JsonProperty("id")
    @ApiModelProperty(value = "TMS station identifier (naturalId)", required = true)
    private long roadStationNaturalId;

    @JsonProperty("tmsNumber")
    @ApiModelProperty(value = "TMS station number", required = true)
    private long tmsNaturalId;

    @ApiModelProperty(value = "Free flow speed to direction 1 [km/h]", required = true)
    private double freeFlowSpeed1;

    @ApiModelProperty(value = "Free flow speed to direction 2 [km/h]", required = true)
    private double freeFlowSpeed2;

    public double getFreeFlowSpeed1() {
        return freeFlowSpeed1;
    }

    public void setFreeFlowSpeed1(final double freeFlowSpeed1) {
        this.freeFlowSpeed1 = freeFlowSpeed1;
    }

    public double getFreeFlowSpeed2() {
        return freeFlowSpeed2;
    }

    public void setFreeFlowSpeed2(final double freeFlowSpeed2) {
        this.freeFlowSpeed2 = freeFlowSpeed2;
    }

    public long getRoadStationNaturalId() {
        return roadStationNaturalId;
    }

    public void setRoadStationNaturalId(long roadStationNaturalId) {
        this.roadStationNaturalId = roadStationNaturalId;
    }

    public long getTmsNaturalId() {
        return tmsNaturalId;
    }

    public void setTmsNaturalId(long tmsNaturalId) {
        this.tmsNaturalId = tmsNaturalId;
    }
}
