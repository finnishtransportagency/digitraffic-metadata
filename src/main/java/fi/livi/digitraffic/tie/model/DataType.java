package fi.livi.digitraffic.tie.model;

import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;

public enum DataType {

    CAMERA_STATION_METADATA,
    CAMERA_STATION_METADATA_CHECK,
    FORECAST_SECTION_METADATA,
    FORECAST_SECTION_METADATA_CHECK,
    FORECAST_SECTION_WEATHER_DATA,
    FORECAST_SECTION_V2_METADATA,
    FORECAST_SECTION_V2_METADATA_CHECK,
    FORECAST_SECTION_V2_WEATHER_DATA,

    LOCATIONS_METADATA,
    LOCATIONS_METADATA_CHECK,
    LOCATION_TYPES_METADATA,
    LOCATION_TYPES_METADATA_CHECK,
    MAINTENANCE_REALIZATION_DATA,
    MAINTENANCE_REALIZATION_DATA_CHECKED,
    TRAVEL_TIME_MEDIANS_DATA,
    TMS_FREE_FLOW_SPEEDS_DATA,

    TMS_SENSOR_CONSTANT_METADATA,
    TMS_SENSOR_CONSTANT_METADATA_CHECK,
    TMS_SENSOR_CONSTANT_VALUE_DATA,
    TMS_SENSOR_CONSTANT_VALUE_DATA_CHECK,
    TMS_SENSOR_VALUE_MEASURED_DATA,
    TMS_SENSOR_VALUE_UPDATED_DATA,

    TMS_STATION_METADATA,
    TMS_STATION_METADATA_CHECK,
    TMS_STATION_SENSOR_CONSTANT_METADATA,
    TMS_STATION_SENSOR_CONSTANT_METADATA_CHECK,
    TMS_STATION_SENSOR_METADATA,
    TMS_STATION_SENSOR_METADATA_CHECK,

    TRAVEL_TIME_MEASUREMENTS_DATA,
    TRAVEL_TIME_LINKS_METADATA,
    TRAVEL_TIME_LINKS_METADATA_CHECK,

    WEATHER_STATION_METADATA,
    WEATHER_STATION_METADATA_CHECK,
    WEATHER_STATION_SENSOR_METADATA,
    WEATHER_STATION_SENSOR_METADATA_CHECK,
    WEATHER_SENSOR_VALUE_MEASURED_DATA,
    WEATHER_SENSOR_VALUE_UPDATED_DATA,

    // Datex2 types
    TRAFFIC_INCIDENT,
    WEIGHT_RESTRICTION,
    ROADWORK
    ;


    public static DataType getSensorMetadataTypeForRoadStationType(final RoadStationType roadStationType) {
        if (RoadStationType.TMS_STATION == roadStationType) {
            return TMS_STATION_SENSOR_METADATA;
        } else if (RoadStationType.WEATHER_STATION == roadStationType) {
            return WEATHER_STATION_SENSOR_METADATA;
        }
        throw new IllegalArgumentException("No metadata type for " + roadStationType);
    }

    public static DataType getSensorMetadataCheckTypeForRoadStationType(RoadStationType roadStationType) {
        if (RoadStationType.TMS_STATION == roadStationType) {
            return TMS_STATION_SENSOR_METADATA_CHECK;
        } else if (RoadStationType.WEATHER_STATION == roadStationType) {
            return WEATHER_STATION_SENSOR_METADATA_CHECK;
        }
        throw new IllegalArgumentException("No metadata type for " + roadStationType);
    }

    public static DataType getSensorValueMeasuredDataType(final RoadStationType roadStationType) {
        if (roadStationType.equals(RoadStationType.TMS_STATION)) {
            return TMS_SENSOR_VALUE_MEASURED_DATA;
        } else if (roadStationType.equals(RoadStationType.WEATHER_STATION)) {
            return WEATHER_SENSOR_VALUE_MEASURED_DATA;
        }
        throw new IllegalArgumentException(String.format("Allowed RoadStationTypes are %s and %s",
                                           RoadStationType.TMS_STATION, RoadStationType.WEATHER_STATION));
    }

    public static DataType getSensorValueUpdatedDataType(final RoadStationType roadStationType) {
        if (roadStationType.equals(RoadStationType.TMS_STATION)) {
            return TMS_SENSOR_VALUE_UPDATED_DATA;
        } else if (roadStationType.equals(RoadStationType.WEATHER_STATION)) {
            return WEATHER_SENSOR_VALUE_UPDATED_DATA;
        }
        throw new IllegalArgumentException(String.format("Allowed RoadStationTypes are %s and %s",
            RoadStationType.TMS_STATION, RoadStationType.WEATHER_STATION));
    }

    public static DataType typeFor(final Datex2MessageType messageType) {
        switch (messageType) {
            case TRAFFIC_INCIDENT:
                return TRAFFIC_INCIDENT;
            case WEIGHT_RESTRICTION:
                return WEIGHT_RESTRICTION;
            case ROADWORK:
                return ROADWORK;
            default:
                throw new IllegalArgumentException("No mapping for " + messageType);
        }
    }
}
