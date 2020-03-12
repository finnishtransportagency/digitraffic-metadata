package fi.livi.digitraffic.tie.dto.v1.forecast;

import java.time.ZonedDateTime;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.model.v1.forecastsection.OverallRoadCondition;
import fi.livi.digitraffic.tie.model.v1.forecastsection.Reliability;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({ "time", "type", "forecastName", "daylight", "roadTemperature", "temperature", "windSpeed",
    "windDirection", "overallRoadCondition", "weatherSymbol", "reliability", "forecastConditionReason" })
@ApiModel(value = "RoadCondition")
public class RoadConditionDto {
    private final String forecastName;

    @ApiModelProperty("Observation or forecast time depending on type")
    private final ZonedDateTime time;

    @ApiModelProperty("Tells if there is daylight: true/false")
    private final Boolean daylight;

    @Enumerated(EnumType.STRING)
    private final OverallRoadCondition overallRoadCondition;

    @Enumerated(EnumType.STRING)
    private final Reliability reliability;

    @ApiModelProperty("Road temperature at given time. If not available value is not set")
    private final String roadTemperature;

    @ApiModelProperty("Air temperature")
    private final String temperature;

    @ApiModelProperty("Weather symbol code http://corporate.foreca.com/en/products/foreca-symbols")
    private final String weatherSymbol;

    @ApiModelProperty("Wind direction in degrees. 0 when there is no wind or the direction is variable. 90 degrees is arrow to the east " +
        "(count clockwise)")
    private final Integer windDirection;

    @ApiModelProperty("Wind speed in m/s")
    private final Double windSpeed;

    @ApiModelProperty("Tells if object is an observation or a forecast: OBSERVATION / FORECAST")
    private final String type;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final ForecastConditionReasonDto forecastConditionReason;

    public RoadConditionDto(final String forecastName, final ZonedDateTime time, final Boolean daylight, final OverallRoadCondition overallRoadCondition,
        final Reliability reliability, final String roadTemperature, final String temperature, final String weatherSymbol, final Integer windDirection, final Double windSpeed, final String type,
        final ForecastConditionReasonDto forecastConditionReason) {
        this.forecastName = forecastName;
        this.time = time;
        this.daylight = daylight;
        this.overallRoadCondition = overallRoadCondition;
        this.reliability = reliability;
        this.roadTemperature = roadTemperature;
        this.temperature = temperature;
        this.weatherSymbol = weatherSymbol;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.type = type;
        this.forecastConditionReason = forecastConditionReason;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public Boolean getDaylight() {
        return daylight;
    }

    public OverallRoadCondition getOverallRoadCondition() {
        return overallRoadCondition;
    }

    public Reliability getReliability() {
        return reliability;
    }

    public String getRoadTemperature() {
        return roadTemperature;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getWeatherSymbol() {
        return weatherSymbol;
    }

    public Integer getWindDirection() {
        return windDirection;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public String getType() {
        return type;
    }

    public ForecastConditionReasonDto getForecastConditionReason() {
        return forecastConditionReason;
    }

    public String getForecastName() {
        return forecastName;
    }
}
