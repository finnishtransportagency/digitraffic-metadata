package fi.livi.digitraffic.tie.service;

public abstract class AbstractRoadStationSensorUpdater {

    protected final RoadStationSensorService roadStationSensorService;

    public AbstractRoadStationSensorUpdater(RoadStationSensorService roadStationSensorService) {
        this.roadStationSensorService = roadStationSensorService;
    }
}
