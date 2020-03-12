package fi.livi.digitraffic.tie.service;

import fi.livi.digitraffic.tie.model.v1.RoadAddress;
import fi.livi.digitraffic.tie.model.v1.RoadStation;

public class AbstractRoadStationAttributeUpdater {

    public static boolean setRoadAddressIfNotSet(final RoadStation rs) {
        if (rs.getRoadAddress() == null) {
            rs.setRoadAddress(new RoadAddress());
            return true;
        }
        return false;
    }
}
