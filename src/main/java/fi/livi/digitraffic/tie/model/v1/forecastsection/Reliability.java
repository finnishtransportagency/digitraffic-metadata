package fi.livi.digitraffic.tie.model.v1.forecastsection;

import io.swagger.annotations.ApiModel;

@ApiModel("Forecast reliability")
public enum Reliability {

    SUCCESSFUL(0),
    NO_DATA_FROM_ROADSTATION(1),
    FAILED(2);

    private final int value;

    Reliability(int value) {
        this.value = value;
    }

    public static Reliability of(final Integer value) {
        if (value == null) {
            return null;
        }
        for (Reliability reliability : values()) {
            if (reliability.value == value) {
                return reliability;
            }
        }
        return null;
    }
}
