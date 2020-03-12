package fi.livi.digitraffic.tie.dto.v1.weather;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.tms.AbstractStationWithSensorsDto;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "WeatherStationData", description = "Weather station with sensor values", parent = AbstractStationWithSensorsDto.class)
@JsonPropertyOrder( value = {"id", "measuredTime", "sensorValues"})
public class WeatherStationDto extends AbstractStationWithSensorsDto {


}
