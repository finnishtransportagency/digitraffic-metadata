package fi.livi.digitraffic.tie.model.v1.forecastsection;

import io.swagger.annotations.ApiModel;

@ApiModel("The quality of precipitation")
public enum PrecipitationCondition {
    NO_DATA_AVAILABLE(0),
    NO_RAIN_DRY_WEATHER(1),
    LIGHT_RAIN(2),
    RAIN(3),
    HEAVY_RAIN(4),
    LIGHT_SNOWFALL(5),
    SNOWFALL(6),
    HEAVY_SNOWFALL(7);

    private final int value;

    PrecipitationCondition(final int value) {
        this.value = value;
    }

    public static PrecipitationCondition of(final Integer value) {
        if (value == null) {
            return null;
        }
        for (PrecipitationCondition precipitationCondition : values()) {
            if (precipitationCondition.value == value) {
                return precipitationCondition;
            }
        }
        return null;
    }
}
