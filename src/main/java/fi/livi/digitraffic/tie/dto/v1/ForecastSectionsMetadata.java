package fi.livi.digitraffic.tie.dto.v1;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeatureCollection;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Weather forecast sections")
@JsonPropertyOrder({ "dataUpdatedTime", "dataLastCheckedTime", "forecastSections" })
public class ForecastSectionsMetadata extends RootDataObjectDto {

    @ApiModelProperty(value = "Data last checked date time", required = true)
    public final ZonedDateTime dataLastCheckedTime;

    @ApiModelProperty(value = "Weather forecast sections", required = true)
    public final ForecastSectionFeatureCollection forecastSections;

    public ForecastSectionsMetadata(final ForecastSectionFeatureCollection forecastSections, final ZonedDateTime lastUpdated, final ZonedDateTime dataLastCheckedTime) {
        super(lastUpdated);
        this.dataLastCheckedTime = dataLastCheckedTime;
        this.forecastSections = forecastSections;
    }
}
