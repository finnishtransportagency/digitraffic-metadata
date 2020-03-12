package fi.livi.digitraffic.tie.model.v1.forecastsection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("Visibility")
public enum VisibilityCondition {

    @ApiModelProperty("400 m")
    FAIRLY_POOR(1),
    @ApiModelProperty("200 m")
    POOR(2);

    private final int value;

    VisibilityCondition(final int value) {
        this.value = value;
    }

    public static VisibilityCondition of(final Integer value) {
        if (value == null) {
            return null;
        }
        for (VisibilityCondition visibilityCondition : values()) {
            if (visibilityCondition.value == value) {
                return visibilityCondition;
            }
        }
        return null;
    }
}
