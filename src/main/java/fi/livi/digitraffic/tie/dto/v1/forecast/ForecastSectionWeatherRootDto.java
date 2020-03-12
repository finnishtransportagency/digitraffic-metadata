package fi.livi.digitraffic.tie.dto.v1.forecast;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.livi.digitraffic.tie.dto.v1.RootDataObjectDto;
import io.swagger.annotations.ApiModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value = "ForecastSectionWeatherRoot")
public class ForecastSectionWeatherRootDto extends RootDataObjectDto {
    public final List<ForecastSectionWeatherDataDto> weatherData;

    public ForecastSectionWeatherRootDto(final ZonedDateTime timestamp) {
        this(timestamp, null);
    }

    public ForecastSectionWeatherRootDto(final ZonedDateTime timestamp, final List<ForecastSectionWeatherDataDto> weatherData) {
        super(timestamp);
        this.weatherData = weatherData;
    }
}
