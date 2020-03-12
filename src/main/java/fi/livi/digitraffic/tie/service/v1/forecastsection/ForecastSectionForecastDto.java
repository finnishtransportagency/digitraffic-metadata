package fi.livi.digitraffic.tie.service.v1.forecastsection;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.model.v1.forecastsection.OverallRoadCondition;
import fi.livi.digitraffic.tie.model.v1.forecastsection.Reliability;

public class ForecastSectionForecastDto {

    public final String forecastName;

    public final ZonedDateTime time;

    public final Boolean daylight;

    public final OverallRoadCondition overallRoadCondition;

    public final Reliability reliability;

    public final String roadTemperature;

    public final String temperature;

    public final String weatherSymbol;

    public final Integer windDirection;

    public final Double windSpeed;

    public final ForecastSectionWeatherReasonDto conditionReason;

    public ForecastSectionForecastDto(@JsonProperty("forecastName") final String forecastName,
                                      @JsonProperty("Time") final ZonedDateTime time,
                                      @JsonProperty("daylight") final Boolean daylight,
                                      @JsonProperty("overallRoadCondition") final Integer overallRoadCondition,
                                      @JsonProperty("reliability") final Integer reliability,
                                      @JsonProperty("roadTemperature") final String roadTemperature,
                                      @JsonProperty("temperature") final String temperature,
                                      @JsonProperty("weatherSymbol") final String weatherSymbol,
                                      @JsonProperty("windDirection") final Integer windDirection,
                                      @JsonProperty("windSpeed") final Double windSpeed,
                                      @JsonProperty("conditionReason") final ForecastSectionWeatherReasonDto conditionReason) {
        this.forecastName = forecastName;
        this.time = time;
        this.daylight = daylight;
        this.overallRoadCondition = OverallRoadCondition.of(overallRoadCondition);
        this.reliability = Reliability.of(reliability);
        this.roadTemperature = roadTemperature;
        this.temperature = temperature;
        this.weatherSymbol = weatherSymbol;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.conditionReason = conditionReason;
    }
}