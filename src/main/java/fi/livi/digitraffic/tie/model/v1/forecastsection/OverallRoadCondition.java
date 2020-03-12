package fi.livi.digitraffic.tie.model.v1.forecastsection;

import io.swagger.annotations.ApiModel;

@ApiModel("Overall road condition")
public enum OverallRoadCondition {

    NORMAL_CONDITION(1),
    POOR_CONDITION(2),
    EXTREMELY_POOR_CONDITION(3),
    CONDITION_COULD_NOT_BE_RESOLVED(-1);

    private final int value;

    OverallRoadCondition(final int value) {
        this.value = value;
    }

    public static OverallRoadCondition of(final Integer value) {
        if (value == null) {
            return null;
        }
        for (OverallRoadCondition overallRoadCondition : values()) {
            if (overallRoadCondition.value == value) {
                return overallRoadCondition;
            }
        }
        return null;
    }
}
