package fi.livi.digitraffic.tie.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RoadStationCollectionStatus {

    /** -1 = station data is not collected */
    DATA_IS_NOT_COLLECTED(-1),
    /** 0 = station data collection and sensor calculation successful */
    OK(0),
    /** 1 = problems in sensor calculation (sensor value not received) */
    PROBLEMS_IN_SENSOR_CALCULATION(1),
    /** 2 = data collection failed due to missing communication device */
    DATA_COLLECTION_FAILED(2),
    /** 3 = no connection to station */
    NO_CONNECTION_TO_STATION(3),
    /** 4 = line busy */
    LINE_BUSY(4),
    /** 5 = connection successful, but not data available */
    NO_DATA_AVAILABLE(5);

    private static final Logger log = LoggerFactory.getLogger(RoadStationCollectionStatus.class);

    private final int typeNumber;

    private static final Map<Integer, RoadStationCollectionStatus> lookup = new HashMap<>();

    RoadStationCollectionStatus(final int typeNumber) {
        this.typeNumber = typeNumber;
    }

    static{
        for (final RoadStationCollectionStatus rst : EnumSet.allOf(RoadStationCollectionStatus.class)) {
            lookup.put(rst.getCollectionStatusCode(), rst);
        }
    }

    public static RoadStationCollectionStatus fromCollectionStatusCode(final Integer collectionStatusCode) {
        if (collectionStatusCode == null) {
            return null;
        }
        final RoadStationCollectionStatus value = lookup.get(collectionStatusCode);
        if (value == null) {
            log.error("RoadStationCollectionStatus not found for typeNumber " + collectionStatusCode);
        }
        return value;
    }

    public int getCollectionStatusCode() {
        return typeNumber;
    }
}
