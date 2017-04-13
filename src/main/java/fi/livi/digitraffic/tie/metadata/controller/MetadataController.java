package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.metadata.service.location.LocationService.LATEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.annotation.ConditionalOnControllersEnabled;
import fi.livi.digitraffic.tie.metadata.converter.NonPublicRoadStationException;
import fi.livi.digitraffic.tie.metadata.dto.ForecastSectionsMetadata;
import fi.livi.digitraffic.tie.metadata.dto.RoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.metadata.dto.location.LocationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.dto.location.LocationTypesMetadata;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.location.LocationVersion;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionService;
import fi.livi.digitraffic.tie.metadata.service.location.LocationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationService;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "metadata", description = "Metadata for Digitraffic services")
@RestController
@RequestMapping(API_V1_BASE_PATH + API_METADATA_PART_PATH)
@ConditionalOnControllersEnabled
public class MetadataController {
    private static final Logger log = LoggerFactory.getLogger(MetadataController.class);

    public static final String TMS_STATIONS_PATH = "/tms-stations";
    public static final String TMS_STATIONS_AVAILABLE_SENSORS_PATH = "/tms-sensors";
    public static final String CAMERA_STATIONS_PATH = "/camera-stations";
    public static final String WEATHER_STATIONS_PATH = "/weather-stations";
    public static final String WEATHER_STATIONS_AVAILABLE_SENSORS_PATH = "/weather-sensors";

    public static final String FORECAST_SECTIONS_PATH = "/forecast-sections";
    public static final String LOCATIONS_PATH = "/locations";
    public static final String LOCATION_VERSIONS_PATH = "/location-versions";
    public static final String LOCATION_TYPES_PATH = "/location-types";

    private static final String REQUEST_LOG_PREFIX = "Metadata REST request path: ";

    private final CameraPresetService cameraPresetService;
    private final TmsStationService tmsStationService;
    private final WeatherStationService weatherStationService;
    private final RoadStationSensorService roadStationSensorService;
    private final ForecastSectionService forecastSectionService;
    private final LocationService locationService;

    @Autowired
    public MetadataController(final CameraPresetService cameraPresetService,
                              final TmsStationService tmsStationService,
                              final WeatherStationService weatherStationService,
                              final RoadStationSensorService roadStationSensorService,
                              final ForecastSectionService forecastSectionService,
                              final LocationService locationService) {
        this.cameraPresetService = cameraPresetService;
        this.tmsStationService = tmsStationService;
        this.weatherStationService = weatherStationService;
        this.roadStationSensorService = roadStationSensorService;
        this.forecastSectionService = forecastSectionService;
        this.locationService = locationService;
    }

    @ApiOperation("The static information of TMS stations (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({     @ApiResponse(code = 200, message = "Successful retrieval of TMS Station Feature Collections"),
                        @ApiResponse(code = 500, message = "Internal server error")})
    public TmsStationFeatureCollection listTmsStations(
                @ApiParam("If parameter is given result will only contain update status.")
                @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
                boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + TMS_STATIONS_PATH);
        return tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(lastUpdated);
    }

    @ApiOperation("The static information of obsolete TMS stations (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_PATH + "/obsolete", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({     @ApiResponse(code = 200, message = "Successful retrieval of TMS Station Feature Collections"),
                        @ApiResponse(code = 500, message = "Internal server error")})
    public TmsStationFeatureCollection listObsoleteTmsStations(
        @ApiParam("If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + TMS_STATIONS_PATH);
        return tmsStationService.findAllPublicObsoleteTmsStationsAsFeatureCollection(lastUpdated);
    }

    @ApiOperation("The static information of one TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({     @ApiResponse(code = 200, message = "Successful retrieval of TMS Station Feature Collections"),
                        @ApiResponse(code = 404, message = "Vessel metadata not found"),
                        @ApiResponse(code = 500, message = "Internal server error")})
    public TmsStationFeature listTmsStation(
        @PathVariable("id") final Long id) throws NonPublicRoadStationException {
        log.info(REQUEST_LOG_PREFIX + TMS_STATIONS_PATH);
        return tmsStationService.findTmsStationById(id);
    }

    @ApiOperation("The static information of available sensors of TMS stations (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_AVAILABLE_SENSORS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({     @ApiResponse(code = 200, message = "Successful retrieval of TMS Station Sensors"),
                        @ApiResponse(code = 500, message = "Internal server error") })
    public RoadStationsSensorsMetadata listNonObsoleteTmsStationSensors(
            @ApiParam(value = "If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
                    boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + TMS_STATIONS_AVAILABLE_SENSORS_PATH);
        return roadStationSensorService.findRoadStationsSensorsMetadata(RoadStationType.TMS_STATION, lastUpdated);
    }

    @ApiOperation("The static information of weather camera presets")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_STATIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Camera Preset Feature Collections"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public CameraStationFeatureCollection listNonObsoleteCameraPresets(
                    @ApiParam("If parameter is given result will only contain update status.")
                    @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
                    boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + CAMERA_STATIONS_PATH);
        return cameraPresetService.findAllPublishableCameraStationsAsFeatureCollection(lastUpdated);
    }

    @ApiOperation("The static information of weather stations")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_STATIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Weather Feature Collections"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public WeatherStationFeatureCollection listNonObsoleteWeatherStations(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + WEATHER_STATIONS_PATH);
        return weatherStationService.findAllPublishableWeatherStationAsFeatureCollection(lastUpdated);
    }

    @ApiOperation("The static information of available sensors of weather stations")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_STATIONS_AVAILABLE_SENSORS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Weather Station Sensors"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public RoadStationsSensorsMetadata listNonObsoleteWeatherStationSensors(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + WEATHER_STATIONS_AVAILABLE_SENSORS_PATH);
        return roadStationSensorService.findRoadStationsSensorsMetadata(RoadStationType.WEATHER_STATION, lastUpdated);
    }

    @ApiOperation("List available location versions")
    @RequestMapping(method = RequestMethod.GET, path = LOCATION_VERSIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of locations"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public List<LocationVersion> listLocationVersions () {
        log.info(REQUEST_LOG_PREFIX + LOCATION_VERSIONS_PATH);
        return locationService.findLocationVersions();
    }

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("The static information of weather forecast sections")
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Forecast Sections"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public ForecastSectionsMetadata listForecastSections(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
            boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + FORECAST_SECTIONS_PATH);
        return forecastSectionService.findForecastSectionsMetadata(lastUpdated);
    }

    @ApiOperation("The static information of locations")
    @RequestMapping(method = RequestMethod.GET, path = LOCATIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of locations"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public LocationFeatureCollection listLocations (
            @ApiParam("If parameter is given use this version.")
            @RequestParam(value = "version", required = false, defaultValue = LATEST)
            String version,

            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
                    boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + LOCATIONS_PATH);
        return locationService.findLocationsMetadata(lastUpdated, version);
    }

    @ApiOperation("The static information of location types and locationsubtypes")
    @RequestMapping(method = RequestMethod.GET, path = LOCATION_TYPES_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of location types and location subtypes"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public LocationTypesMetadata listaLocationTypes (
            @ApiParam("If parameter is given use this version.")
            @RequestParam(value = "version", required = false, defaultValue = LATEST)
                    String version,

            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
                    boolean lastUpdated) {
        log.info(REQUEST_LOG_PREFIX + LOCATION_TYPES_PATH);
        return locationService.findLocationSubtypes(lastUpdated, version);
    }

    @ApiOperation("The static information of one location")
    @RequestMapping(method = RequestMethod.GET, path = LOCATIONS_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of location"),
                    @ApiResponse(code = 500, message = "Internal server error") })
    public LocationFeatureCollection getLocation (
            @ApiParam("If parameter is given use this version.")
            @RequestParam(value = "version", required = false, defaultValue = LATEST)
                    String version,

            @PathVariable("id") final int id) {
        log.info(REQUEST_LOG_PREFIX + LOCATIONS_PATH + "/" + id);
        return locationService.findLocation(id, version);
    }
}
