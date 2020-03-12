package fi.livi.digitraffic.tie.dto.v1;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeatureCollection;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Weather forecast sections")
@JsonPropertyOrder({ "dataUpdatedTime", "dataLastCheckedTime", "forecastSections" })
public class ForecastSectionsMetadata extends RootMetadataObjectDto {

    @ApiModelProperty(value = "Weather forecast sections", required = true)
    private final ForecastSectionFeatureCollection forecastSections;

    public ForecastSectionsMetadata(final ForecastSectionFeatureCollection forecastSections, final ZonedDateTime lastUpdated, final ZonedDateTime dataLastCheckedTime) {
        super(lastUpdated, dataLastCheckedTime);
        this.forecastSections = forecastSections;
    }

    public ForecastSectionFeatureCollection getForecastSections() {
        return forecastSections;
    }
}
