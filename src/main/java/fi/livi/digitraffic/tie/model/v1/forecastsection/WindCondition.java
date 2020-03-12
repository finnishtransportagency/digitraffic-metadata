package fi.livi.digitraffic.tie.model.v1.forecastsection;

import io.swagger.annotations.ApiModel;

@ApiModel("The strength of wind")
public enum WindCondition {

    WEAK(1),
    MEDIUM(2),
    STRONG(3);

    private final int value;

    WindCondition(final int value) {
        this.value = value;
    }

    public static WindCondition of(final Integer value) {
        if (value == null) {
            return null;
        }
        for (WindCondition windCondition : values()) {
            if (windCondition.value == value) {
                return windCondition;
            }
        }
        return null;
    }
}
