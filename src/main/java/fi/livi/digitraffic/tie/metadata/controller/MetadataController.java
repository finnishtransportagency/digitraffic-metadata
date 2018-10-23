package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.metadata.service.location.LocationService.LATEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.annotation.ConditionalOnControllersEnabled;
import fi.livi.digitraffic.tie.helper.EnumConverter;
import fi.livi.digitraffic.tie.metadata.converter.NonPublicRoadStationException;
import fi.livi.digitraffic.tie.metadata.dto.ForecastSectionsMetadata;
import fi.livi.digitraffic.tie.metadata.dto.TmsRoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.metadata.dto.WeatherRoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.metadata.dto.location.LocationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.dto.location.LocationTypesMetadata;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.traveltime.LinkFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.location.LocationVersion;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionService;
import fi.livi.digitraffic.tie.metadata.service.location.LocationService;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationService;
import fi.livi.digitraffic.tie.metadata.service.traveltime.TravelTimeLinkMetadataService;
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

    public static final String TMS_STATIONS_PATH = "/tms-stations";
    private static final String TMS_STATIONS_TMS_NUMBER_PATH = TMS_STATIONS_PATH + "/tms-number";
    private static final String TMS_STATIONS_ROAD_NUMBER_PATH = TMS_STATIONS_PATH + "/road-number";
    private static final String TMS_STATIONS_ROAD_STATION_ID_PATH = TMS_STATIONS_PATH + "/road-station-id";

    static final String TMS_STATIONS_AVAILABLE_SENSORS_PATH = "/tms-sensors";
    static final String CAMERA_STATIONS_PATH = "/camera-stations";
    static final String WEATHER_STATIONS_PATH = "/weather-stations";
    static final String WEATHER_STATIONS_AVAILABLE_SENSORS_PATH = "/weather-sensors";

    private static final String FORECAST_SECTIONS_PATH = "/forecast-sections";
    static final String LOCATIONS_PATH = "/locations";
    private static final String LOCATION_VERSIONS_PATH = "/location-versions";
    private static final String LOCATION_TYPES_PATH = "/location-types";
    private static final String TRAVEL_TIME_LINKS_PATH = "/travel-time-links";

    private final CameraPresetService cameraPresetService;
    private final TmsStationService tmsStationService;
    private final WeatherStationService weatherStationService;
    private final RoadStationSensorService roadStationSensorService;
    private final ForecastSectionService forecastSectionService;
    private final LocationService locationService;
    private final TravelTimeLinkMetadataService travelTimeLinkMetadataService;

    @Autowired
    public MetadataController(final CameraPresetService cameraPresetService,
                              final TmsStationService tmsStationService,
                              final WeatherStationService weatherStationService,
                              final RoadStationSensorService roadStationSensorService,
                              final ForecastSectionService forecastSectionService,
                              final LocationService locationService,
                              final TravelTimeLinkMetadataService travelTimeLinkMetadataService) {
        this.cameraPresetService = cameraPresetService;
        this.tmsStationService = tmsStationService;
        this.weatherStationService = weatherStationService;
        this.roadStationSensorService = roadStationSensorService;
        this.forecastSectionService = forecastSectionService;
        this.locationService = locationService;
        this.travelTimeLinkMetadataService = travelTimeLinkMetadataService;
    }

    @ApiOperation("The static information of TMS stations (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({     @ApiResponse(code = 200, message = "Successful retrieval of TMS Station Feature Collections") })
    public TmsStationFeatureCollection tmsStations(
                @ApiParam("If parameter is given result will only contain update status.")
                @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
                final boolean lastUpdated,
        @ApiParam(value = "Return TMS stations of given state.", allowableValues = "active,removed,all")
        @RequestParam(value = "state", required = false, defaultValue = "active")
        final String stateString) {

        final TmsState state = EnumConverter.parseState(TmsState.class, stateString);

        return tmsStationService.findAllPublishableTmsStationsAsFeatureCollection(lastUpdated, state);
    }

    @ApiOperation("The static information of one TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_TMS_NUMBER_PATH + "/{number}", produces =
        APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({     @ApiResponse(code = 200, message = "Successful retrieval of TMS Station Feature Collections") })
    public TmsStationFeature tmsStationsByTmsNumber(
        @PathVariable("number") final Long tmsNumber) throws NonPublicRoadStationException {
        return tmsStationService.getTmsStationByLamId(tmsNumber);
    }

    @ApiOperation("The static information of TMS stations of given road (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_ROAD_NUMBER_PATH + "/{number}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({     @ApiResponse(code = 200, message = "Successful retrieval of TMS Station Feature Collections"),
                        @ApiResponse(code = 404, message = "Road number not found") })
    public TmsStationFeatureCollection tmsStationsByRoadNumber(
        @PathVariable("number") final Integer roadNumber,
        @ApiParam(value = "Return TMS stations of given state.", allowableValues = "active,removed,all")
        @RequestParam(value = "state", required = false, defaultValue = "active")
        final String stateString) throws NonPublicRoadStationException {

        final TmsState state = EnumConverter.parseState(TmsState.class, stateString);

        return tmsStationService.listTmsStationsByRoadNumber(roadNumber, state);
    }

    @ApiOperation("The static information of one TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_ROAD_STATION_ID_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({     @ApiResponse(code = 200, message = "Successful retrieval of TMS Station Feature Collections"),
                        @ApiResponse(code = 404, message = "Road Station not found") })
    public TmsStationFeature tmsStationsByRoadStationId(
        @PathVariable("id") final Long id) throws NonPublicRoadStationException {
        return tmsStationService.getTmsStationByRoadStationId(id);
    }

    @ApiOperation("The static information of available sensors of TMS stations (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_AVAILABLE_SENSORS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({     @ApiResponse(code = 200, message = "Successful retrieval of TMS Station Sensors") })
    public TmsRoadStationsSensorsMetadata tmsSensors(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
                    final boolean lastUpdated) {
        return roadStationSensorService.findTmsRoadStationsSensorsMetadata(lastUpdated);
    }

    @ApiOperation("The static information of weather camera presets")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_STATIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Camera Preset Feature Collections") })
    public CameraStationFeatureCollection cameraStations(
                    @ApiParam("If parameter is given result will only contain update status.")
                    @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
                    final boolean lastUpdated) {
        return cameraPresetService.findAllPublishableCameraStationsAsFeatureCollection(lastUpdated);
    }

    @ApiOperation("The static information of weather stations")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_STATIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Weather Feature Collections") })
    public WeatherStationFeatureCollection weatherStations(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
            final boolean lastUpdated) {
        return weatherStationService.findAllPublishableWeatherStationAsFeatureCollection(lastUpdated);
    }

    @ApiOperation("The static information of available sensors of weather stations")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_STATIONS_AVAILABLE_SENSORS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Weather Station Sensors") })
    public WeatherRoadStationsSensorsMetadata weatherSensors(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
            final boolean lastUpdated) {
        return roadStationSensorService.findWeatherRoadStationsSensorsMetadata(lastUpdated);
    }

    @ApiOperation("List available location versions")
    @RequestMapping(method = RequestMethod.GET, path = LOCATION_VERSIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of location versions") })
    public List<LocationVersion> locationVersions () {
        return locationService.findLocationVersions();
    }

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation("The static information of weather forecast sections")
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Forecast Sections") })
    public ForecastSectionsMetadata forecastSections(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
            final boolean lastUpdated) {
        return forecastSectionService.findForecastSectionsMetadata(lastUpdated);
    }

    @ApiOperation("The static information of locations")
    @RequestMapping(method = RequestMethod.GET, path = LOCATIONS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of locations") })
    public LocationFeatureCollection locations (
            @ApiParam("If parameter is given use this version.")
            @RequestParam(value = "version", required = false, defaultValue = LATEST)
            final String version,

            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
                    final boolean lastUpdated) {
        return locationService.findLocationsMetadata(lastUpdated, version);
    }

    @ApiOperation("The static information of location types and locationsubtypes")
    @RequestMapping(method = RequestMethod.GET, path = LOCATION_TYPES_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of location types and location subtypes") })
    public LocationTypesMetadata locationTypes(
            @ApiParam("If parameter is given use this version.")
            @RequestParam(value = "version", required = false, defaultValue = LATEST)
                    final String version,

            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
                    final boolean lastUpdated) {
        return locationService.findLocationSubtypes(lastUpdated, version);
    }

    @ApiOperation("The static information of one location")
    @RequestMapping(method = RequestMethod.GET, path = LOCATIONS_PATH + "/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of location") })
    public LocationFeatureCollection locationsById(
            @ApiParam("If parameter is given use this version.")
            @RequestParam(value = "version", required = false, defaultValue = LATEST)
                    final String version,

            @PathVariable("id") final int id) {
        return locationService.findLocation(id, version);
    }

    @ApiOperation("The static information of metropolitan area travel time links")
    @RequestMapping(method = RequestMethod.GET, path = TRAVEL_TIME_LINKS_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of travel time links") })
    public LinkFeatureCollection travelTimeLinks() {
        return travelTimeLinkMetadataService.getLinkMetadata();
    }
}
