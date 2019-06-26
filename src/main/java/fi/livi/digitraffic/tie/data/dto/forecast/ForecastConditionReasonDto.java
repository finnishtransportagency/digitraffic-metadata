package fi.livi.digitraffic.tie.data.dto.forecast;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.FrictionCondition;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.PrecipitationCondition;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.RoadCondition;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.VisibilityCondition;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.WindCondition;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForecastConditionReasonDto {
    @Enumerated(EnumType.STRING)
    @ApiModelProperty("Precipitation condition: " +
        "0 = no data available, " +
        "1 = rain intensity < 0.2 mm/h, " +
        "2 = rain intensity >= 0.2 mm/h, " +
        "3 = rain intensity >= 2.5 mm/h, " +
        "4 = rain intensity >= 7.6 mm/h, " +
        "5 = snowing intensity >= 0.2 cm/h, " +
        "6 = snowing intensity >= 1 cm/h, " +
        "7 = snowing intensity >= 3 cm/h")
    private final PrecipitationCondition precipitationCondition;

    @Enumerated(EnumType.STRING)
    private final RoadCondition roadCondition;

    @Enumerated(EnumType.STRING)
    private final WindCondition windCondition;

    @ApiModelProperty("Tells if there is freezing rain: true/false")
    private final Boolean freezingRainCondition;

    @ApiModelProperty("Tells if it is slippery: true/false")
    private final Boolean winterSlipperiness;

    @Enumerated(EnumType.STRING)
    private final VisibilityCondition visibilityCondition;

    @Enumerated(EnumType.STRING)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final FrictionCondition frictionCondition;

    public ForecastConditionReasonDto(final PrecipitationCondition precipitationCondition, final RoadCondition roadCondition,
        final WindCondition windCondition, final Boolean freezingRainCondition, final Boolean winterSlipperiness,
        final VisibilityCondition visibilityCondition, final FrictionCondition frictionCondition) {
        this.precipitationCondition = precipitationCondition;
        this.roadCondition = roadCondition;
        this.windCondition = windCondition;
        this.freezingRainCondition = freezingRainCondition;
        this.winterSlipperiness = winterSlipperiness;
        this.visibilityCondition = visibilityCondition;
        this.frictionCondition = frictionCondition;
    }

    public PrecipitationCondition getPrecipitationCondition() {
        return precipitationCondition;
    }

    public RoadCondition getRoadCondition() {
        return roadCondition;
    }

    public WindCondition getWindCondition() {
        return windCondition;
    }

    public Boolean getFreezingRainCondition() {
        return freezingRainCondition;
    }

    public Boolean getWinterSlipperiness() {
        return winterSlipperiness;
    }

    public VisibilityCondition getVisibilityCondition() {
        return visibilityCondition;
    }

    public FrictionCondition getFrictionCondition() {
        return frictionCondition;
    }
}
