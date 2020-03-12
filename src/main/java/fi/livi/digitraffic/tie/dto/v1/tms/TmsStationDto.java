package fi.livi.digitraffic.tie.dto.v1.tms;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Immutable
@ApiModel(value = "TmsStationData", description = "TMS station with sensor values", parent = AbstractStationWithSensorsDto.class)
@JsonPropertyOrder( value = {"id", "tmsNumber", "measuredTime", "sensorValues"})
public class TmsStationDto extends AbstractStationWithSensorsDto {

    @ApiModelProperty(value = "TMS station number", required = true)
    @JsonProperty(value = "tmsNumber")
    private long tmsStationNaturalId;

    public long getTmsStationNaturalId() {
        return tmsStationNaturalId;
    }

    public void setTmsStationNaturalId(long tmsStationNaturalId) {
        this.tmsStationNaturalId = tmsStationNaturalId;
    }
}
