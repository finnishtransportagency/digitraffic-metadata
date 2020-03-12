package fi.livi.digitraffic.tie.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RoadStationCondition {

    /** 0 = OK */
    OK(0),
    /** 1 = Problems or suspected malfunction */
    PROBLEMS_OR_SUSPECTED_MALFUNCTION(1),
    /** 2 = confirmed malfunction */
    CONFIRMED_MALFUNCTION(2),
    /** 3 = confirmed malfunction, repair not planned in near future */
    CONFIRMED_MALFUNCTION_REPAIR_NOT_PLANNED(3),
    /** 4 = repair request sent */
    REPAIR_REQUEST_SENT(4),
    /** 5 = repair work done */
    REPAIR_WORK_DONE(5),
    /** 6 = repair work interrupted */
    REPAIR_WORK_INTERRUPTED(6);

    private static final Logger log = LoggerFactory.getLogger(RoadStationCondition.class);

    private final int typeNumber;

    private static final Map<Integer, RoadStationCondition> lookup = new HashMap<>();

    RoadStationCondition(final int typeNumber) {
        this.typeNumber = typeNumber;
    }

    static{
        for (final RoadStationCondition rst : EnumSet.allOf(RoadStationCondition.class)) {
            lookup.put(rst.getConditionCode(), rst);
        }
    }

    public static RoadStationCondition fromConditionCode(final Integer conditionCode) {
        if (conditionCode == null) {
            return null;
        }
        final RoadStationCondition value = lookup.get(conditionCode);
        if (value == null) {
            log.error("RoadStationCondition not found for typeNumber " + conditionCode);
        }
        return value;
    }

    public int getConditionCode() {
        return typeNumber;
    }
}
