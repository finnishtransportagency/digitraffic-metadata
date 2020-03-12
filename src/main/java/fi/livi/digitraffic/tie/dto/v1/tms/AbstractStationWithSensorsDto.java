package fi.livi.digitraffic.tie.dto.v1.tms;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.dto.v1.SensorValueDto;
import io.swagger.annotations.ApiModelProperty;

@Immutable
public abstract class AbstractStationWithSensorsDto {

    @ApiModelProperty(value = "Road station id", required = true)
    @JsonProperty(value = "id")
    private long roadStationNaturalId;

    @ApiModelProperty(value = "Measured sensor values of the Weather Station", required = true)
    private List<SensorValueDto> sensorValues = new ArrayList<>();

    @ApiModelProperty(value = "Date and time of the sensor's measurement")
    private ZonedDateTime measuredTime;

    public long getRoadStationNaturalId() {
        return roadStationNaturalId;
    }

    public void setRoadStationNaturalId(final long roadStationNaturalId) {
        this.roadStationNaturalId = roadStationNaturalId;
    }

    public void addSensorValue(final SensorValueDto sensorValue) {
        sensorValues.add(sensorValue);
    }

    public List<SensorValueDto> getSensorValues() {
        return sensorValues;
    }

    public void setSensorValues(final List<SensorValueDto> sensorValues) {
        this.sensorValues = sensorValues;
    }

    public ZonedDateTime getMeasuredTime() {
        return measuredTime;
    }

    public void setMeasuredTime(final ZonedDateTime measuredTime) {
        this.measuredTime = measuredTime;
    }
}
