package fi.livi.digitraffic.tie.metadata.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration;
import fi.livi.digitraffic.tie.data.controller.DataController;
import fi.livi.digitraffic.tie.data.dto.tms.TmsSensorConstantRootDto;
import fi.livi.digitraffic.tie.data.service.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.data.service.TmsDataService;
import fi.livi.digitraffic.tie.helper.EnumConverter;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TmsDataDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TmsStationDatex2Response;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationDatex2Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "beta", description = "Beta apis")
@RestController
@Validated
@RequestMapping(RoadApplicationConfiguration.API_BETA_BASE_PATH)
@ConditionalOnWebApplication
public class BetaController {
    public static final String TMS_STATIONS_DATEX2_PATH = "/tms-stations-datex2";
    public static final String TMS_DATA_DATEX2_PATH = "/tms-data-datex2";
    public static final String TMS_SENSOR_CONSTANTS = "/tms-sensor-constants";

    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;
    private final TmsDataService tmsDataService;

    @Autowired
    public BetaController(final TmsStationDatex2Service tmsStationDatex2Service, final TmsDataDatex2Service tmsDataDatex2Service,
                          final TmsDataService tmsDataService) {
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
        this.tmsDataService = tmsDataService;
    }

    @ApiOperation("The static information of TMS stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE})
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of TMS Station Feature Collections"))
    public TmsStationDatex2Response tmsStationsDatex2(
        @ApiParam(value = "Return TMS stations of given state.", allowableValues = "active,removed,all")
        @RequestParam(value = "state", required = false, defaultValue = "active")
        final String stateString) {

        final TmsState state = EnumConverter.parseState(TmsState.class, stateString);

        return tmsStationDatex2Service.findAllPublishableTmsStationsAsDatex2(state);
    }

    @ApiOperation("Current data of TMS Stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE })
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of TMS Station data"))
    public TmsDataDatex2Response tmsDataDatex2() {
        return tmsDataDatex2Service.findPublishableTmsDataDatex2();
    }

    @ApiOperation("Current sensor constants and values of TMS station (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_SENSOR_CONSTANTS, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful retrieval of sensor constants and values"))
    public TmsSensorConstantRootDto tmsSensorConstants(
        @ApiParam("If parameter is given result will only contain update status")
        @RequestParam(value=DataController.LAST_UPDATED_PARAM, required = false, defaultValue = "false") final
        boolean lastUpdated) {
        return tmsDataService.findPublishableSensorConstants(lastUpdated);
    }



}