package fi.livi.digitraffic.tie.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RoadStationType {
    TMS_STATION(1),
    WEATHER_STATION(2),
    CAMERA_STATION(3);

    private static final Logger LOG = LoggerFactory.getLogger(RoadStationType.class);

    private final int typeNumber;

    private static final Map<Integer, RoadStationType> lookup = new HashMap<>();

    RoadStationType(final int typeNumber) {
        this.typeNumber = typeNumber;
    }

    static{
        for (final RoadStationType rst : EnumSet.allOf(RoadStationType.class)) {
            lookup.put(rst.getTypeNumber(), rst);
        }
    }

    public static RoadStationType fromTypeNumber(final int typeNumber) {
        final RoadStationType value = lookup.get(typeNumber);
        if (value == null) {
            LOG.error("RoadStationType not found for typeNumber " + typeNumber);
        }
        return value;
    }

    public int getTypeNumber() {
        return typeNumber;
    }
}
