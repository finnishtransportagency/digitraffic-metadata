package fi.livi.digitraffic.tie.controller.v1;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.CAMERA_DATA_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.FORECAST_SECTION_WEATHER_DATA_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.FREE_FLOW_SPEEDS_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.ROADWORKS_DATEX2_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_DATA_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TMS_SENSOR_CONSTANTS;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_DISORDERS_DATEX2_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.WEATHER_DATA_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.WEIGHT_RESTRICTIONS_DATEX2_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.dto.v1.camera.CameraRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.forecast.ForecastSectionWeatherRootDto;
import fi.livi.digitraffic.tie.dto.v1.freeflowspeed.FreeFlowSpeedRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsRootDataObjectDto;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsSensorConstantRootDto;
import fi.livi.digitraffic.tie.dto.v1.weather.WeatherRootDataObjectDto;
import fi.livi.digitraffic.tie.service.v1.camera.CameraDataService;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2DataService;
import fi.livi.digitraffic.tie.service.v1.ForecastSectionDataService;
import fi.livi.digitraffic.tie.service.v1.FreeFlowSpeedService;
import fi.livi.digitraffic.tie.service.v1.MaintenanceDataService;
import fi.livi.digitraffic.tie.service.v1.TmsDataService;
import fi.livi.digitraffic.tie.service.v1.WeatherService;
import fi.livi.digitraffic.tie.datex2.response.RoadworksDatex2Response;
import fi.livi.digitraffic.tie.datex2.response.TrafficDisordersDatex2Response;
import fi.livi.digitraffic.tie.datex2.response.WeightRestrictionsDatex2Response;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionApiVersion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Data v1", description = "Data of Digitraffic services (Api version 1)")
@RestController
@Validated
@RequestMapping(API_V1_BASE_PATH + API_DATA_PART_PATH)
@ConditionalOnWebApplication
public class DataController {

    public static final String LAST_UPDATED_PARAM = "lastUpdated";

    private final TmsDataService tmsDataService;
    private final FreeFlowSpeedService freeFlowSpeedService;
    private final WeatherService weatherService;
    private final CameraDataService cameraDataService;
    private final ForecastSectionDataService forecastSectionDataService;
    private final Datex2DataService datex2DataService;
    private final MaintenanceDataService maintenanceDataService;

    @Autowired
    public DataController(final TmsDataService tmsDataService,
                          final FreeFlowSpeedService freeFlowSpeedService,
                          final WeatherService weatherService,
                          final CameraDataService cameraDataService,
                          final ForecastSectionDataService forecastSectionDataService,
                          final Datex2DataService datex2DataService,
                          final MaintenanceDataService maintenanceDataService) {
        this.tmsDataService = tmsDataService;
        this.freeFlowSpeedService = freeFlowSpeedService;
        this.weatherService = weatherService;
        this.cameraDataService = cameraDataService;
        this.forecastSectionDataService = forecastSectionDataService;
        this.datex2DataService = datex2DataService;
        this.maintenanceDataService = maintenanceDataService;
    }

    @ApiOperation("Current free flow speeds")
    @RequestMapping(method = RequestMethod.GET, path = FREE_FLOW_SPEEDS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of free flow speeds"))
    public FreeFlowSpeedRootDataObjectDto freeFlowSpeeds(
            @ApiParam("If parameter is given result will only contain update status")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        return freeFlowSpeedService.listLinksPublicFreeFlowSpeeds(lastUpdated);
    }

    @ApiOperation("Current free flow speeds of TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = FREE_FLOW_SPEEDS_PATH + "/tms/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of free flow speeds"))
    public FreeFlowSpeedRootDataObjectDto freeFlowSpeedsOfTmsById(
            @ApiParam(value = "TMS station id", required = true)
            @PathVariable
            final long id) {
        return freeFlowSpeedService.listTmsPublicFreeFlowSpeeds(id);
    }

    @ApiOperation("Current data of cameras")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_DATA_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of camera station data"))
    public CameraRootDataObjectDto cameraData(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        return cameraDataService.findPublishableCameraStationsData(lastUpdated);
    }

    @ApiOperation("Current data of camera")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_DATA_PATH + "/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of camera station data"))
    public CameraRootDataObjectDto cameraDataById(
            @ApiParam(value = "Camera id", required = true)
            @PathVariable
            final String id) {
        return cameraDataService.findPublishableCameraStationsData(id);
    }

    @ApiOperation("Current data of TMS Stations (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of TMS Station data"))
    public TmsRootDataObjectDto tmsData(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        return tmsDataService.findPublishableTmsData(lastUpdated);
    }

    @ApiOperation("Current data of TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_PATH + "/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of weather station data"))
    public TmsRootDataObjectDto tmsDataById(
            @ApiParam(value = "TMS Station id", required = true)
            @PathVariable
            final long id) {
        return tmsDataService.findPublishableTmsData(id);
    }

    @ApiOperation("Current data of Weather Stations")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_DATA_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of Weather Station data"))
    public WeatherRootDataObjectDto weatherData(
            @ApiParam("If parameter is given result will only contain update status.")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        return weatherService.findPublishableWeatherData(lastUpdated);
    }

    @ApiOperation("Current data of Weather Station")
    @RequestMapping(method = RequestMethod.GET, path = WEATHER_DATA_PATH + "/{id}", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of weather station data"))
    public WeatherRootDataObjectDto weatherDataById(
            @ApiParam(value = "Weather Station id", required = true)
            @PathVariable
            final long id) {
        return weatherService.findPublishableWeatherData(id);
    }

    @ApiOperation("Current data of Weather Forecast Sections")
    @RequestMapping(method = RequestMethod.GET, path = FORECAST_SECTION_WEATHER_DATA_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of Weather Forecast Section data"))
    public ForecastSectionWeatherRootDto roadConditions(
            @ApiParam("If parameter is given result will only contain update status")
            @RequestParam(value=LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
            boolean lastUpdated) {
        return forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V1, lastUpdated, null,
                                                                        null, null, null, null,
                                                                        null);
    }

    @ApiOperation(value = "Active traffic disorders Datex2 messages")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DISORDERS_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of traffic disorders"))
    public TrafficDisordersDatex2Response trafficDisordersDatex2(
        @ApiParam(value = "Return traffic disorders from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours) {
        return datex2DataService.findActiveTrafficDisorders(inactiveHours);
    }

    @ApiOperation(value = "Traffic disorder Datex2 messages by situation id")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DISORDERS_DATEX2_PATH + "/{situationId}", produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of traffic disorders"),
                    @ApiResponse(code = 404, message = "Situation id not found") })
    public TrafficDisordersDatex2Response trafficDisordersDatex2BySituationId(
            @ApiParam(value = "Situation id.", required = true)
            @PathVariable final String situationId) {
        return datex2DataService.getAllTrafficDisordersBySituationId(situationId);
    }

    @ApiOperation(value = "Traffic disorder Datex2 messages disorders history")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DISORDERS_DATEX2_PATH + "/history", produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses(      {   @ApiResponse(code = 200, message = "Successful retrieval of traffic disorders"),
                            @ApiResponse(code = 400, message = "Invalid parameter"),
                            @ApiResponse(code = 404, message = "Situation id not found") })
    public TrafficDisordersDatex2Response trafficDisordersDatex2OfHistory(
            @ApiParam("Situation id")
            @RequestParam(required = false)
            final String situationId,
            @ApiParam(value = "Year (>2014)", required = true)
            @RequestParam @Min(2015) @Max(9999)
            final int year,
            @ApiParam(value = "Month (1-12)", required = true)
            @RequestParam @Range(min = 1, max = 12)
            final int month) {
        return datex2DataService.findTrafficDisorders(situationId, year, month);
    }

    @ApiOperation(value = "Active roadwork Datex2 messages")
    @RequestMapping(method = RequestMethod.GET, path = ROADWORKS_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of roadworks"))
    public RoadworksDatex2Response roadworksDatex2(
        @ApiParam(value = "Return roadworks from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours) {
        return datex2DataService.findActiveRoadworks(inactiveHours);
    }

    @ApiOperation(value = "Roadwork Datex2 messages by situation id")
    @RequestMapping(method = RequestMethod.GET, path = ROADWORKS_DATEX2_PATH + "/{situationId}", produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of traffic disorders"),
        @ApiResponse(code = 404, message = "Situation id not found") })
    public RoadworksDatex2Response roadworksDatex2BySituationId(
        @ApiParam(value = "Situation id.", required = true)
        @PathVariable final String situationId) {
        return datex2DataService.getAllRoadworksBySituationId(situationId);
    }

    @ApiOperation(value = "Roadwork Datex2 messages history")
    @RequestMapping(method = RequestMethod.GET, path = ROADWORKS_DATEX2_PATH + "/history", produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of traffic disorders"),
        @ApiResponse(code = 400, message = "Invalid parameter"),
        @ApiResponse(code = 404, message = "Situation id not found")})
    public RoadworksDatex2Response roadworksDatex2OfHistory(
        @ApiParam(value = "Situation id")
        @RequestParam(required = false)
        final String situationId,
        @ApiParam(value = "Year (>2014)", required = true)
        @RequestParam @Valid @Min(2014)
        final int year,
        @ApiParam(value = "Month (1-12)", required = true)
        @RequestParam @Valid @Range(min = 1, max = 12)
        final int month) {
        return datex2DataService.findRoadworks(situationId, year, month);
    }

    @ApiOperation(value = "Active weight restrictions Datex2 messages")
    @RequestMapping(method = RequestMethod.GET, path = WEIGHT_RESTRICTIONS_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of weight restrictions"))
    public WeightRestrictionsDatex2Response weightRestrictionsDatex2(
        @ApiParam(value = "Return weight restrictions from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours) {
        return datex2DataService.findActiveWeightRestrictions(inactiveHours);
    }

    @ApiOperation(value = "Weight restrictions Datex2 messages by situation id")
    @RequestMapping(method = RequestMethod.GET, path = WEIGHT_RESTRICTIONS_DATEX2_PATH + "/{situationId}", produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of weight restrictions"),
        @ApiResponse(code = 404, message = "Situation id not found") })
    public WeightRestrictionsDatex2Response weightRestrictionsDatex2BySituationId(
        @ApiParam(value = "Situation id.", required = true)
        @PathVariable final String situationId) {
        return datex2DataService.getAllWeightRestrictionsBySituationId(situationId);
    }

    @ApiOperation(value = "Weight restriction Datex2 messages history")
    @RequestMapping(method = RequestMethod.GET, path = WEIGHT_RESTRICTIONS_DATEX2_PATH + "/history", produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of weight restrictions"),
        @ApiResponse(code = 400, message = "Invalid parameter"),
        @ApiResponse(code = 404, message = "Situation id not found")})
    public WeightRestrictionsDatex2Response weightRestrictionsDatex2OfHistory(
        @ApiParam(value = "Situation id")
        @RequestParam(required = false)
        final String situationId,
        @ApiParam(value = "Year (>2014)", required = true)
        @RequestParam @Valid @Min(2014)
        final int year,
        @ApiParam(value = "Month (1-12)", required = true)
        @RequestParam @Valid @Range(min = 1, max = 12)
        final int month) {
        return datex2DataService.findWeightRestrictions(situationId, year, month);
    }

    @ApiOperation("Current sensor constants and values of TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_SENSOR_CONSTANTS, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of sensor constants and values"))
    public TmsSensorConstantRootDto tmsSensorConstants(
        @ApiParam("If parameter is given result will only contain update status")
        @RequestParam(value=DataController.LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
        boolean lastUpdated) {
        return tmsDataService.findPublishableSensorConstants(lastUpdated);
    }

//    // TODO FIXME only for testing, must be removed
//    @ApiOperation(value = "Work Machine Tracking data")
//    @RequestMapping(method = RequestMethod.GET, path = "/work-machine-tracking", produces = { APPLICATION_JSON_VALUE })
//    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of work machine tracking data")})
//    public List<WorkMachineTracking> workMachineTrackings() {
//        return maintenanceDataService.findAll();
//    }
//
//    // TODO FIXME only for testing, must be removed
//    @ApiOperation(value = "Work Machine Observation data")
//    @RequestMapping(method = RequestMethod.GET, path = "/work-machine-observation", produces = { APPLICATION_JSON_VALUE })
//    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of work machine tracking data")})
//    public List<WorkMachineObservation> workMachineObservations() {
//        return maintenanceDataService.finAllWorkMachineObservations();
//    }
}
