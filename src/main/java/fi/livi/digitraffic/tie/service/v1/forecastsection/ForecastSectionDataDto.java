package fi.livi.digitraffic.tie.service.v1.forecastsection;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForecastSectionDataDto {

    public final Timestamp messageTimestamp;

    public final List<ForecastSectionWeatherDto> forecastSectionWeatherList;

    public ForecastSectionDataDto(@JsonProperty("messageTimestamp") final Timestamp messageTimestamp,
                                  @JsonProperty("roadConditions") final List<ForecastSectionWeatherDto> forecastSectionWeatherList) {
        this.messageTimestamp = messageTimestamp;
        this.forecastSectionWeatherList = forecastSectionWeatherList;
    }
}