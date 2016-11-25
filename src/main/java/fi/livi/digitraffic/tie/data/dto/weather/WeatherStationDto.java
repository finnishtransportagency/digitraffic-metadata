package fi.livi.digitraffic.tie.data.dto.weather;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.tms.AbstractStationWithSensorsDto;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "WeatherStationData", description = "Weather station with sensor values", parent = AbstractStationWithSensorsDto.class)
@JsonPropertyOrder( value = {"id", "measuredTime", "sensorValues"})
public class WeatherStationDto extends AbstractStationWithSensorsDto {


}
