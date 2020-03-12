package fi.livi.digitraffic.tie.dto.v1.forecast;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({ "naturalId", "roadConditions" })
@ApiModel(value = "ForecastSectionWeatherData")
public class ForecastSectionWeatherDataDto {

    @ApiModelProperty(
        "VERSION 1: Forecast section identifier 15 characters ie. 00004_112_000_0: \n" +
        "1. Road number 5 characters ie. 00004, \n" +
        "2. Road section 3 characters ie. 112, \n" +
        "3. Road section version 3 characters ie. 000, \n" +
        "4. Reserved for future needs 1 characters default 0 \n\n" +
        "VERSION 2: Forecast section identifier ie. 00004_342_01435_0_274.569: \n" +
        "1. Road number 5 characters ie. 00004, \n" +
        "2. Road section 3 characters ie. 342, \n" +
        "3. Start distance 5 characters ie. 000, \n" +
        "4. Carriageway 1 character, \n" +
        "5. Measure value of link start point. Varying number of characters ie. 274.569, \n" +
        "Refers to Digiroad content at https://aineistot.vayla.fi/digiroad/")
    @JsonProperty("id")
    public final String naturalId;

    @ApiModelProperty("Road conditions data forecast section")
    public final List<RoadConditionDto> roadConditions;

    public ForecastSectionWeatherDataDto(final String naturalId, final List<RoadConditionDto> roadConditions) {
        this.naturalId = naturalId;
        this.roadConditions = roadConditions;
    }
}
