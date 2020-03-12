package fi.livi.digitraffic.tie.model.v1.forecastsection;

import io.swagger.annotations.ApiModel;

@ApiModel("The state of the road")
public enum RoadCondition {

    DRY(1),
    MOIST(2),
    WET(3),
    SLUSH(4),
    FROST(5),
    PARTLY_ICY(6),
    ICE(7),
    SNOW(8);

    private final int value;

    RoadCondition(final int value) {
        this.value = value;
    }

    public static RoadCondition of(final Integer value) {
        if (value == null) {
            return null;
        }
        for (RoadCondition roadCondition : values()) {
            if (roadCondition.value == value) {
                return roadCondition;
            }
        }
        return null;
    }
}
