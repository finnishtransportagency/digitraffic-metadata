package fi.livi.digitraffic.tie.controller;

public final class ApiPaths {
    public static final String API_V1_BASE_PATH = "/api/v1";
    public static final String API_V2_BASE_PATH = "/api/v2";
    public static final String API_V3_BASE_PATH = "/api/v3";
    public static final String API_BETA_BASE_PATH = "/api/beta";
    public static final String API_INTEGRATIONS_BASE_PATH = "/api/integrations";

    public static final String API_METADATA_PART_PATH = "/metadata";
    public static final String API_DATA_PART_PATH = "/data";
    public static final String API_MAINTENANCE_PART_PATH = "/maintenance";
    public static final String API_WORK_MACHINE_PART_PATH = "/work-machine";
    public static final String API_VARIABLE_SIGN_UPDATE_PART_PATH = "/variable-sign-update";
    public static final String VARIABLE_SIGNS_PATH = "/variable-signs";
    public static final String VARIABLE_SIGNS_CODE_DESCRIPTIONS = VARIABLE_SIGNS_PATH + "/code-descriptions";

    public static final String WEATHERCAM_PATH = "/weathercam";

    public static final String TMS_STATIONS_PATH = "/tms-stations";
    public static final String TMS_STATIONS_TMS_NUMBER_PATH = TMS_STATIONS_PATH + "/tms-number";
    public static final String TMS_STATIONS_ROAD_NUMBER_PATH = TMS_STATIONS_PATH + "/road-number";
    public static final String TMS_STATIONS_ROAD_STATION_ID_PATH = TMS_STATIONS_PATH + "/road-station-id";

    public static final String TMS_STATIONS_AVAILABLE_SENSORS_PATH = "/tms-sensors";
    public static final String CAMERA_STATIONS_PATH = "/camera-stations";
    public static final String WEATHER_STATIONS_PATH = "/weather-stations";
    public static final String WEATHER_STATIONS_AVAILABLE_SENSORS_PATH = "/weather-sensors";

    public static final String FORECAST_SECTIONS_PATH = "/forecast-sections";
    public static final String LOCATIONS_PATH = "/locations";
    public static final String LOCATION_VERSIONS_PATH = "/location-versions";
    public static final String LOCATION_TYPES_PATH = "/location-types";

    public static final String CAMERA_DATA_PATH = "/camera-data";
    public static final String TMS_DATA_PATH = "/tms-data";
    public static final String WEATHER_DATA_PATH = "/weather-data";
    public static final String TMS_SENSOR_CONSTANTS = "/tms-sensor-constants";
    public static final String FREE_FLOW_SPEEDS_PATH = "/free-flow-speeds";
    public static final String FORECAST_SECTION_WEATHER_DATA_PATH = "/road-conditions";

    public static final String CAMERA_HISTORY_PATH = "/camera-history";

    // datex2
    public static final String TRAFFIC_DATEX2_PATH = "/traffic-datex2";
    public static final String TRAFFIC_DISORDERS_DATEX2_PATH = "/traffic-disorders-datex2";
    public static final String ROADWORKS_DATEX2_PATH = "/roadworks-datex2";
    public static final String WEIGHT_RESTRICTIONS_DATEX2_PATH = "/weight-restrictions-datex2";

    private ApiPaths() {}
}
