package fi.livi.digitraffic.tie.model.v1.maintenance.harja;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Lane {

    LANE_1("1"),
    LANE_11("11"),
    LANE_12("12"),
    LANE_13("13"),
    LANE_14("14"),
    LANE_15("15"),
    LANE_16("16"),
    LANE_17("17"),
    LANE_18("18"),
    LANE_19("19"),
    LANE_21("21"),
    LANE_22("22"),
    LANE_23("23"),
    LANE_24("24"),
    LANE_25("25"),
    LANE_26("26"),
    LANE_27("27"),
    LANE_28("28"),
    LANE_29("29");

    private final String value;
    private final static Map<String, Lane> CONSTANTS = new HashMap<>();

    static {
        for (Lane c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private Lane(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static Lane fromValue(final String value) {
        Lane constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }
}
