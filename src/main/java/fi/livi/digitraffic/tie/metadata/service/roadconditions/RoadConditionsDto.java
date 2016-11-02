package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

public class RoadConditionsDto {

    public final Date messageTimestamp;

    public final List<ForecastSectionWeatherDto> forecastSectionWeatherList;

    public RoadConditionsDto(@JsonProperty("messageTimestamp") Date messageTimestamp,
                             @JsonProperty("roadConditions") List<ForecastSectionWeatherDto> forecastSectionWeatherList) {
        this.messageTimestamp = messageTimestamp;
        this.forecastSectionWeatherList = forecastSectionWeatherList;
    }
}