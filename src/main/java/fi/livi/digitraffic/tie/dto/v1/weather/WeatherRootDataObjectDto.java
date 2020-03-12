package fi.livi.digitraffic.tie.dto.v1.weather;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "WeatherData", description = "Latest measurement data from Weather Stations", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataUpdatedTime", "weatherStations"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherRootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Weather Stations data")
    private final List<WeatherStationDto> weatherStations;

    public WeatherRootDataObjectDto(final List<WeatherStationDto> weatherStations, final ZonedDateTime updated) {
        super(updated);
        this.weatherStations = weatherStations;
    }

    public WeatherRootDataObjectDto(final ZonedDateTime updated) {
        this(null, updated);
    }

    public List<WeatherStationDto> getWeatherStations() {
        return weatherStations;
    }

}
