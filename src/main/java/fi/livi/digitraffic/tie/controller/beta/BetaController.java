package fi.livi.digitraffic.tie.controller.beta;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.CAMERA_HISTORY_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.TRAFFIC_DATEX2_PATH;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.COORD_FORMAT_WGS84;
import static java.time.temporal.ChronoUnit.HOURS;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.controller.TmsState;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryChangesDto;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTask;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTaskCategory;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTaskOperation;
import fi.livi.digitraffic.tie.helper.EnumConverter;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.service.v1.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryDataService;
import fi.livi.digitraffic.tie.service.v1.tms.TmsStationDatex2Service;
import fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2DataService;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "beta")
@RestController
@Validated
@RequestMapping(API_BETA_BASE_PATH)
@ConditionalOnWebApplication
public class BetaController {
    public static final String TMS_STATIONS_DATEX2_PATH = "/tms-stations-datex2";
    public static final String TMS_DATA_DATEX2_PATH = "/tms-data-datex2";
    public static final String MAINTENANCE_REALIZATIONS_PATH = "/maintenance/realizations";
    public static final String MAINTENANCE_REALIZATIONS_TASKS_PATH = "/tasks";
    public static final String MAINTENANCE_REALIZATIONS_OPERATIONS_PATH = "/operations";
    public static final String MAINTENANCE_REALIZATIONS_CATEGORIES_PATH = "/categories";

    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;
    private final V2MaintenanceRealizationDataService maintenanceRealizationDataService;
    private final V2Datex2DataService v2Datex2DataService;
    private final CameraPresetHistoryDataService cameraPresetHistoryDataService;

    @Autowired
    public BetaController(final TmsStationDatex2Service tmsStationDatex2Service,
                          final TmsDataDatex2Service tmsDataDatex2Service,
                          final V2Datex2DataService v2Datex2DataService,
                          final V2MaintenanceRealizationDataService maintenanceRealizationDataService,
                          final CameraPresetHistoryDataService cameraPresetHistoryDataService) {
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
        this.maintenanceRealizationDataService = maintenanceRealizationDataService;
        this.v2Datex2DataService = v2Datex2DataService;
        this.cameraPresetHistoryDataService = cameraPresetHistoryDataService;
    }

    @ApiOperation(value = "Active Datex2 JSON messages for traffic-incident, roadwork, weight-restriction -types")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DATEX2_PATH + "/{datex2MessageType}.json", produces = { APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of JSON traffic Datex2-messages"))
    public TrafficAnnouncementFeatureCollection datex2Json(
        @ApiParam(value = "Datex2 Message type.", required = true, allowableValues = "traffic-incident, roadwork, weight-restriction")
        @PathVariable
        final Datex2MessageType datex2MessageType,
        @ApiParam(value = "Return datex2 messages from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours) {
        return v2Datex2DataService.findActiveJson(inactiveHours, datex2MessageType);
    }

    @ApiOperation(value = "Active Datex2 messages for traffic-incident, roadwork, weight-restriction -types")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DATEX2_PATH + "/{datex2MessageType}.xml", produces = { APPLICATION_XML_VALUE })
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of traffic disorders"))
    public D2LogicalModel datex2(
        @ApiParam(value = "Datex2 Message type.", required = true, allowableValues = "traffic-incident, roadwork, weight-restriction")
        @PathVariable
        final Datex2MessageType datex2MessageType,
        @ApiParam(value = "Return datex2 messages from given amount of hours in the past.")
        @RequestParam(defaultValue = "0")
        @Range(min = 0)
        final int inactiveHours) {
        return v2Datex2DataService.findActive(inactiveHours, datex2MessageType);
    }

    @ApiOperation(value = "Datex2 JSON messages history by situation id for traffic-incident, roadwork, weight-restriction -types")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DATEX2_PATH + "/{datex2MessageType}/{situationId}.json", produces = { APPLICATION_JSON_VALUE})
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of datex2 messages"),
                    @ApiResponse(code = 404, message = "Situation id not found") })
    public TrafficAnnouncementFeatureCollection datex2JsonBySituationId(
        @ApiParam(value = "Datex2 Message type.", required = true, allowableValues = "traffic-incident, roadwork, weight-restriction")
        @PathVariable
        final Datex2MessageType datex2MessageType,
        @ApiParam(value = "Datex2 situation id.", required = true)
        @PathVariable
        final String situationId) {
        return v2Datex2DataService.findAllBySituationIdJson(situationId, datex2MessageType);
    }

    @ApiOperation(value = "Datex2 messages history by situation id for traffic-incident, roadwork, weight-restriction -types")
    @RequestMapping(method = RequestMethod.GET, path = TRAFFIC_DATEX2_PATH + "/{datex2MessageType}/{situationId}.xml", produces = { APPLICATION_XML_VALUE })
    @ApiResponses({ @ApiResponse(code = 200, message = "Successful retrieval of datex2 messages"),
                    @ApiResponse(code = 404, message = "Situation id not found") })
    public D2LogicalModel datex2BySituationId(
        @ApiParam(value = "Datex2 Message type.", required = true, allowableValues = "traffic-incident, roadwork, weight-restriction")
        @PathVariable
        final Datex2MessageType datex2MessageType,
        @ApiParam(value = "Datex2 situation id.", required = true)
        @PathVariable
        final String situationId) {
        return v2Datex2DataService.findAllBySituationId(situationId, datex2MessageType);
    }

    @ApiOperation("The static information of TMS stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE})
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of TMS Stations Datex2 metadata"))
    public D2LogicalModel tmsStationsDatex2(
        @ApiParam(value = "Return TMS stations of given state.", allowableValues = "active,removed,all")
        @RequestParam(value = "state", required = false, defaultValue = "active")
        final String stateString) {

        final TmsState state = EnumConverter.parseState(TmsState.class, stateString);

        return tmsStationDatex2Service.findAllPublishableTmsStationsAsDatex2(state);
    }

    @ApiOperation("Current data of TMS Stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_VALUE })
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of TMS Stations Datex2 data"))
    public D2LogicalModel tmsDataDatex2() {
        return tmsDataDatex2Service.findPublishableTmsDataDatex2();
    }

    private static final String RANGE_X_TXT = "Values between 19.0 and 32.0.";
    private static final String RANGE_Y_TXT = "Values between 59.0 and 72.0.";
    private static final String RANGE_X = "range[19.0, 32.0]";
    private static final String RANGE_Y = "range[59.0, 72.0]";
    @ApiOperation(value = "Road maintenance realizations data")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_REALIZATIONS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance realizations data"))
    public MaintenanceRealizationFeatureCollection findMaintenanceRealizations(

            @ApiParam(value = "Return realization data received after given time in ISO date time format. Default is -1h from now.", defaultValue = "2020-01-01T12:00Z")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final ZonedDateTime from,

            @ApiParam(value = "Return realization data received before given time in ISO date time format. Default is now.", defaultValue = "2020-01-01T13:00Z")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final ZonedDateTime to,

            @ApiParam(allowableValues = RANGE_X, value = "Minimum x coordinate (longitude) " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT, required = true)
            @RequestParam(defaultValue = "19.0")
            @DecimalMin("19.0")
            @DecimalMax("32.0")
            final double xMin,

            @ApiParam(allowableValues = RANGE_Y, value = "Minimum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT, required = true)
            @RequestParam(defaultValue = "59.0")
            @DecimalMin("59.0")
            @DecimalMax("72.0")
            final double yMin,

            @ApiParam(allowableValues = RANGE_X, value = "Maximum x coordinate (longitude). " + COORD_FORMAT_WGS84 + " " + RANGE_X_TXT, required = true)
            @RequestParam(defaultValue = "32")
            @DecimalMin("19.0")
            @DecimalMax("32.0")
            final double xMax,

            @ApiParam(allowableValues = RANGE_Y, value = "Maximum y coordinate (latitude). " + COORD_FORMAT_WGS84 + " " + RANGE_Y_TXT, required = true)
            @RequestParam(defaultValue = "72.0")
            @DecimalMin("59.0")
            @DecimalMax("72.0")
            final double yMax,

            @ApiParam(value = "Task ids to include")
            @RequestParam(value = "taskId", required = false)
            final List<Long> taskIds) {

        // Make sure newest is also fetched
        final Instant now = Instant.now().plusSeconds(1);
        final Instant fromParam = from != null ? from.toInstant() : now.minus(1, HOURS);
                                                              // Just to be sure all events near now in future will be fetched
        final Instant toParam = to != null ? to.toInstant() : now.plus(1, HOURS);

        if (fromParam.isAfter(toParam)) {
            throw new IllegalArgumentException("Time from must be before to");
        } else if (fromParam.plus(24, HOURS).isBefore(toParam)) {
            throw new IllegalArgumentException("Time between from and to must be less or equal to 24 h");
        }
        return maintenanceRealizationDataService.findMaintenanceRealizations(fromParam, toParam, xMin, yMin, xMax, yMax, taskIds);
    }

    @ApiOperation(value = "Road maintenance realizations tasks")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_REALIZATIONS_PATH + MAINTENANCE_REALIZATIONS_TASKS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance realizations tasks"))
    public List<MaintenanceRealizationTask> findMaintenanceRealizationsTasks() {
        return maintenanceRealizationDataService.findAllRealizationsTasks();
    }

    @ApiOperation(value = "Road maintenance realizations task operations")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_REALIZATIONS_PATH + MAINTENANCE_REALIZATIONS_OPERATIONS_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance realizations task operations"))
    public List<MaintenanceRealizationTaskOperation> findMaintenanceRealizationsTaskOperations() {
        return maintenanceRealizationDataService.findAllRealizationsTaskOperations();
    }

    @ApiOperation(value = "Road maintenance realizations task categories")
    @RequestMapping(method = RequestMethod.GET, path = MAINTENANCE_REALIZATIONS_PATH + MAINTENANCE_REALIZATIONS_CATEGORIES_PATH, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of maintenance realizations task categories"))
    public List<MaintenanceRealizationTaskCategory> findMaintenanceRealizationsTaskCategories() {
        return maintenanceRealizationDataService.findAllRealizationsTaskCategories();
    }

    @ApiOperation("Weather camera history changes after given time. Result is in ascending order by presetId and lastModified -fields.")
    @RequestMapping(method = RequestMethod.GET, path = CAMERA_HISTORY_PATH + "/changes", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of camera history changes"))
    public CameraHistoryChangesDto getCameraOrPresetHistoryChanges(

        @ApiParam(value = "Camera or preset id(s)")
        @RequestParam(value = "id", required = false)
        final List<String> cameraOrPresetIds,

        @ApiParam(value = "Return changes int the history after given time", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @RequestParam
        final ZonedDateTime after) {

        return cameraPresetHistoryDataService.findCameraOrPresetHistoryChangesAfter(after, cameraOrPresetIds == null ? Collections.emptyList() : cameraOrPresetIds);
    }
}