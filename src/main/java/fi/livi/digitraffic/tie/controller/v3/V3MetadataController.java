package fi.livi.digitraffic.tie.controller.v3;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V3_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.CAMERA_STATIONS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.FORECAST_SECTIONS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.LOCATIONS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.LOCATION_TYPES_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.LOCATION_VERSIONS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_STATIONS_AVAILABLE_SENSORS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_STATIONS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_STATIONS_ROAD_NUMBER_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_STATIONS_ROAD_STATION_ID_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_STATIONS_TMS_NUMBER_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.VARIABLE_SIGNS_CODE_DESCRIPTIONS;
import static fi.livi.digitraffic.tie.controller.ApiPaths.WEATHER_STATIONS_AVAILABLE_SENSORS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.WEATHER_STATIONS_PATH;
import static fi.livi.digitraffic.tie.controller.MediaTypes.MEDIA_TYPE_APPLICATION_GEO_JSON;
import static fi.livi.digitraffic.tie.controller.MediaTypes.MEDIA_TYPE_APPLICATION_VND_GEO_JSON;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;
import static fi.livi.digitraffic.tie.service.v1.location.LocationService.LATEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.tie.converter.exception.NonPublicRoadStationException;
import fi.livi.digitraffic.tie.helper.EnumConverter;
import fi.livi.digitraffic.tie.controller.TmsState;
import fi.livi.digitraffic.tie.dto.v1.ForecastSectionsMetadata;
import fi.livi.digitraffic.tie.dto.v1.TmsRoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.dto.v1.WeatherRoadStationsSensorsMetadata;
import fi.livi.digitraffic.tie.dto.v1.location.LocationFeatureCollection;
import fi.livi.digitraffic.tie.dto.v1.location.LocationTypesMetadata;
import fi.livi.digitraffic.tie.metadata.geojson.camera.CameraStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2FeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeature;
import fi.livi.digitraffic.tie.metadata.geojson.tms.TmsStationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.weather.WeatherStationFeatureCollection;
import fi.livi.digitraffic.tie.model.v1.location.LocationVersion;
import fi.livi.digitraffic.tie.service.v1.camera.CameraWebService;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionV1MetadataService;
import fi.livi.digitraffic.tie.service.v2.forecastsection.V2ForecastSectionMetadataService;
import fi.livi.digitraffic.tie.service.v1.location.LocationService;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationService;
import fi.livi.digitraffic.tie.service.v1.weather.WeatherStationService;
import fi.livi.digitraffic.tie.model.v3.V3VariableSignDescriptions;
import fi.livi.digitraffic.tie.service.v3.V3VariableSignService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Metadata v3")
@RestController
@RequestMapping(API_V3_BASE_PATH + API_METADATA_PART_PATH)
@ConditionalOnWebApplication
public class V3MetadataController {
    private final V2ForecastSectionMetadataService v2ForecastSectionMetadataService;
    private final V3VariableSignService v3VariableSignService;
    private final TmsStationService tmsStationService;
    private final RoadStationSensorService roadStationSensorService;
    private final CameraWebService cameraWebService;
    private final WeatherStationService weatherStationService;
    private final LocationService locationService;
    private final ForecastSectionV1MetadataService forecastSectionService;

    @Autowired
    public V3MetadataController(final V2ForecastSectionMetadataService v2ForecastSectionMetadataService,
        final V3VariableSignService v3VariableSignService, final TmsStationService tmsStationService,
        final RoadStationSensorService roadStationSensorService, final CameraWebService cameraWebService,
        final WeatherStationService weatherStationService, final LocationService locationService,
        final ForecastSectionV1MetadataService forecastSectionService) {
        this.v2ForecastSectionMetadataService = v2ForecastSectionMetadataService;
        this.v3VariableSignService = v3VariableSignService;
        this.tmsStationService = tmsStationService;
        this.roadStationSensorService = roadStationSensorService;
        this.cameraWebService = cameraWebService;
        this.weatherStationService = weatherStationService;
        this.locationService = locationService;
        this.forecastSectionService = forecastSectionService;
    }

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH, produces = { APPLICATION_JSON_VALUE,
                                                                                            MEDIA_TYPE_APPLICATION_GEO_JSON,
                                                                                            MEDIA_TYPE_APPLICATION_VND_GEO_JSON })
    @ApiOperation("The static information of weather forecast sections")
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Forecast Sections") })
    public ForecastSectionV2FeatureCollection forecastSections(
        @ApiParam("If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated,
        @ApiParam(value = "List of forecast section indices")
        @RequestParam(value = "naturalIds", required = false)
        final List<String> naturalIds) {
        return v2ForecastSectionMetadataService.getForecastSectionV2Metadata(lastUpdated, null, null, null,
            null,null, naturalIds);
    }

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH + "/{roadNumber}", produces = { APPLICATION_JSON_VALUE,
                                                                                                              MEDIA_TYPE_APPLICATION_GEO_JSON,
                                                                                                              MEDIA_TYPE_APPLICATION_VND_GEO_JSON })
    @ApiOperation("The static information of weather forecast sections by road number")
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Forecast Sections") })
    public ForecastSectionV2FeatureCollection forecastSections(
        @PathVariable("roadNumber") final int roadNumber) {
        return v2ForecastSectionMetadataService.getForecastSectionV2Metadata(false, roadNumber, null, null,
            null, null, null);
    }

    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTIONS_PATH + "/{minLongitude}/{minLatitude}/{maxLongitude}/{maxLatitude}", produces = { APPLICATION_JSON_VALUE,
                                                                                                                                                           MEDIA_TYPE_APPLICATION_GEO_JSON,
                                                                                                                                                           MEDIA_TYPE_APPLICATION_VND_GEO_JSON })
    @ApiOperation("The static information of weather forecast sections by bounding box")
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Forecast Sections") })
    public ForecastSectionV2FeatureCollection forecastSections(
        @ApiParam(value = "Minimum longitude. " + COORD_FORMAT_WGS84)
        @PathVariable("minLongitude") final double minLongitude,
        @ApiParam(value = "Minimum latitude. " + COORD_FORMAT_WGS84)
        @PathVariable("minLatitude") final double minLatitude,
        @ApiParam(value = "Maximum longitude. " + COORD_FORMAT_WGS84)
        @PathVariable("maxLongitude") final double maxLongitude,
        @ApiParam(value = "Minimum latitude. " + COORD_FORMAT_WGS84)
        @PathVariable("maxLatitude") final double maxLatitude) {
        return v2ForecastSectionMetadataService.getForecastSectionV2Metadata(false, null, minLongitude, minLatitude,
            maxLongitude, maxLatitude, null);
    }

    @ApiOperation("Return all code descriptions.")
    @GetMapping(path = VARIABLE_SIGNS_CODE_DESCRIPTIONS, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public V3VariableSignDescriptions listCodeDescriptions() {
        return new V3VariableSignDescriptions(v3VariableSignService.listVariableSignTypes());
    }

    @ApiOperation("The static information of TMS stations (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_PATH, produces = { APPLICATION_JSON_VALUE,
        MEDIA_TYPE_APPLICATION_GEO_JSON,
        MEDIA_TYPE_APPLICATION_VND_GEO_JSON })
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
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_TMS_NUMBER_PATH + "/{number}", produces = { APPLICATION_JSON_VALUE,
        MEDIA_TYPE_APPLICATION_GEO_JSON,
        MEDIA_TYPE_APPLICATION_VND_GEO_JSON })
    @ApiResponses({     @ApiResponse(code = 200, message = "Successful retrieval of TMS Station Feature Collections") })
    public TmsStationFeature tmsStationsByTmsNumber(
        @PathVariable("number") final Long tmsNumber) throws NonPublicRoadStationException {
        return tmsStationService.getTmsStationByLamId(tmsNumber);
    }

    @ApiOperation("The static information of TMS stations of given road (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_ROAD_NUMBER_PATH + "/{number}", produces = { APPLICATION_JSON_VALUE,
        MEDIA_TYPE_APPLICATION_GEO_JSON,
        MEDIA_TYPE_APPLICATION_VND_GEO_JSON })
    @ApiResponses({     @ApiResponse(code = 200, message = "Successful retrieval of TMS Station Feature Collections"),
        @ApiResponse(code = 404, message = "Road number not found") })
    public TmsStationFeatureCollection tmsStationsByRoadNumber(
        @PathVariable("number") final Integer roadNumber,
        @ApiParam(value = "Return TMS stations of given state.", allowableValues = "active,removed,all")
        @RequestParam(value = "state", required = false, defaultValue = "active")
        final String stateString) {

        final TmsState state = EnumConverter.parseState(TmsState.class, stateString);

        return tmsStationService.listTmsStationsByRoadNumber(roadNumber, state);
    }

    @ApiOperation("The static information of one TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_ROAD_STATION_ID_PATH + "/{id}", produces = { APPLICATION_JSON_VALUE,
        MEDIA_TYPE_APPLICATION_GEO_JSON,
        MEDIA_TYPE_APPLICATION_VND_GEO_JSON })
    @ApiResponses({     @ApiResponse(code = 200, message = "Successful retrieval of TMS Station Feature Collections"),
        @ApiResponse(code = 404, message = "Road Station not found") })
    public TmsStationFeature tmsStationsByRoadStationId(
        @PathVariable("id") final Long id) throws NonPublicRoadStationException {
        return tmsStationService.getTmsStationByRoadStationId(id);
    }

    @ApiOperation("The static information of available sensors of TMS stations (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_AVAILABLE_SENSORS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses({     @ApiResponse(code = 200, message = "Successful retrieval of TMS Station Sensors") })
    public TmsRoadStationsSensorsMetadata tmsSensors(
        @ApiParam("If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return roadStationSensorService.findTmsRoadStationsSensorsMetadata(lastUpdated);
    }

    @ApiOperation("The static information of weather camera presets")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_STATIONS_PATH, produces = { APPLICATION_JSON_VALUE,
        MEDIA_TYPE_APPLICATION_GEO_JSON,
        MEDIA_TYPE_APPLICATION_VND_GEO_JSON })
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Camera Preset Feature Collections") })
    public CameraStationFeatureCollection cameraStations(
        @ApiParam("If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return cameraWebService.findAllPublishableCameraStationsAsFeatureCollection(lastUpdated);
    }

    @ApiOperation("The static information of weather stations")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_STATIONS_PATH, produces = { APPLICATION_JSON_VALUE,
        MEDIA_TYPE_APPLICATION_GEO_JSON,
        MEDIA_TYPE_APPLICATION_VND_GEO_JSON })
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Weather Feature Collections") })
    public WeatherStationFeatureCollection weatherStations(
        @ApiParam("If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return weatherStationService.findAllPublishableWeatherStationAsFeatureCollection(lastUpdated);
    }

    @ApiOperation("The static information of available sensors of weather stations")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_STATIONS_AVAILABLE_SENSORS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of Weather Station Sensors") })
    public WeatherRoadStationsSensorsMetadata weatherSensors(
        @ApiParam("If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated) {
        return roadStationSensorService.findWeatherRoadStationsSensorsMetadata(lastUpdated);
    }

    @ApiOperation("List available location versions")
    @RequestMapping(method = RequestMethod.GET, path = LOCATION_VERSIONS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of location versions") })
    public List<LocationVersion> locationVersions () {
        return locationService.findLocationVersions();
    }

    @ApiOperation("The static information of locations")
    @RequestMapping(method = RequestMethod.GET, path = LOCATIONS_PATH, produces = { APPLICATION_JSON_VALUE,
        MEDIA_TYPE_APPLICATION_GEO_JSON,
        MEDIA_TYPE_APPLICATION_VND_GEO_JSON })
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of locations") })
    public LocationFeatureCollection locations (
        @ApiParam("If parameter is given use this version.")
        @RequestParam(value = "version", required = false, defaultValue = LATEST)
        final String version,

        @ApiParam("If parameter is given result will only contain update status.")
        @RequestParam(value = "lastUpdated", required = false, defaultValue = "false")
        final boolean lastUpdated) throws JsonProcessingException {

        return locationService.findLocationsMetadata(lastUpdated, version);
    }

    @ApiOperation("The static information of location types and locationsubtypes")
    @RequestMapping(method = RequestMethod.GET, path = LOCATION_TYPES_PATH, produces = APPLICATION_JSON_VALUE)
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
    @RequestMapping(method = RequestMethod.GET, path = LOCATIONS_PATH + "/{id}", produces = { APPLICATION_JSON_VALUE,
        MEDIA_TYPE_APPLICATION_GEO_JSON,
        MEDIA_TYPE_APPLICATION_VND_GEO_JSON })
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of location") })
    public LocationFeatureCollection locationsById(
        @ApiParam("If parameter is given use this version.")
        @RequestParam(value = "version", required = false, defaultValue = LATEST)
        final String version,

        @PathVariable("id") final int id) {
        return locationService.findLocation(id, version);
    }

}