package fi.livi.digitraffic.tie.service.v1.forecastsection;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForecastSectionWeatherDto {

    public final String naturalId;

    public final ForecastSectionObservationDto observation;

    public final List<ForecastSectionForecastDto> forecast;

    public ForecastSectionWeatherDto(@JsonProperty("roadId") final String naturalId,
                                     @JsonProperty("observation") final ForecastSectionObservationDto observation,
                                     @JsonProperty("forecast") final List<ForecastSectionForecastDto> forecast) {
        this.naturalId = naturalId;
        this.observation = observation;
        this.forecast = forecast;
    }
}