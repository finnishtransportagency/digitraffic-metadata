package fi.livi.digitraffic.tie.metadata.controller;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration;
import fi.livi.digitraffic.tie.data.dto.trafficsigns.TrafficSignHistory;
import fi.livi.digitraffic.tie.data.service.TmsDataDatex2Service;
import fi.livi.digitraffic.tie.data.service.VariableSignDatex2Service;
import fi.livi.digitraffic.tie.data.service.VariableSignService;
import fi.livi.digitraffic.tie.helper.EnumConverter;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TmsDataDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TmsStationDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsDataDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsTableDatex2Response;
import fi.livi.digitraffic.tie.metadata.geojson.variablesigns.VariableSignFeatureCollection;
import fi.livi.digitraffic.tie.metadata.service.tms.TmsStationDatex2Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "beta", description = "Beta apis")
@RestController
@Validated
@RequestMapping(RoadWebApplicationConfiguration.API_BETA_BASE_PATH)
@ConditionalOnWebApplication
public class BetaController {
    public static final String TMS_STATIONS_DATEX2_PATH = "/tms-stations-datex2";
    public static final String TMS_DATA_DATEX2_PATH = "/tms-data-datex2";
    public static final String VARIABLE_SIGNS_DATA_PATH = "/variable-signs";

    public static final String VARIABLE_SIGNS_TABLE_DATEX2_PATH = "/variable-signs-table-datex2";
    public static final String VARIABLE_SIGNS_DATA_DATEX2_PATH = "/variable-signs-data-datex2";

    private final VariableSignService variableSignService;
    private final VariableSignDatex2Service variableSignDatex2Service;

    private final TmsStationDatex2Service tmsStationDatex2Service;
    private final TmsDataDatex2Service tmsDataDatex2Service;

    @Autowired
    public BetaController(final VariableSignService variableSignService, final VariableSignDatex2Service variableSignDatex2Service, final TmsStationDatex2Service tmsStationDatex2Service,
        final TmsDataDatex2Service tmsDataDatex2Service) {
        this.variableSignService = variableSignService;
        this.variableSignDatex2Service = variableSignDatex2Service;
        this.tmsStationDatex2Service = tmsStationDatex2Service;
        this.tmsDataDatex2Service = tmsDataDatex2Service;
    }

    @ApiOperation("The static information of TMS stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_STATIONS_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE})
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of TMS Stations Datex2 metadata"))
    public TmsStationDatex2Response tmsStationsDatex2(
        @ApiParam(value = "Return TMS stations of given state.", allowableValues = "active,removed,all")
        @RequestParam(value = "state", required = false, defaultValue = "active")
        final String stateString) {

        final TmsState state = EnumConverter.parseState(TmsState.class, stateString);

        return tmsStationDatex2Service.findAllPublishableTmsStationsAsDatex2(state);
    }

    @ApiOperation("Current data of TMS Stations in Datex2 format (Traffic Measurement System / LAM)")
    @RequestMapping(method = RequestMethod.GET, path = TMS_DATA_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE })
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of TMS Stations Datex2 data"))
    public TmsDataDatex2Response tmsDataDatex2() {
        return tmsDataDatex2Service.findPublishableTmsDataDatex2();
    }

    @ApiOperation("Current data of Variable Signs in Datex2 format")
    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_DATA_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE })
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of Variable Signs Datex2 data"))
    public VmsDataDatex2Response variableSignsDataDatex2() {
        return variableSignDatex2Service.findVariableSignData();
    }

    @ApiOperation("Current metadatadata of Variable Signs in Datex2 format")
    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_TABLE_DATEX2_PATH, produces = { APPLICATION_XML_VALUE, APPLICATION_JSON_UTF8_VALUE })
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of Variable Signs Datex2 metadata"))
    public VmsTableDatex2Response variableSignsTableDatex2() {
        return variableSignDatex2Service.findVariableSignMetadata();
    }

    @ApiOperation("List the latest data of variable signs")
    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_DATA_PATH, produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of Traffic Sign data"))
    public VariableSignFeatureCollection variableSigns() {
        return variableSignService.listLatestValues();
    }

    @ApiOperation("List the latest value of a variable sign")
    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_DATA_PATH + "/{deviceId}", produces = APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of Variable sign data"))
    public VariableSignFeatureCollection trafficSign(@PathVariable("deviceId") final String deviceId) {
        return variableSignService.listLatestValue(deviceId);
    }

    @ApiOperation("List the history of variable sign data")
    @RequestMapping(method = RequestMethod.GET, path = VARIABLE_SIGNS_DATA_PATH + "/history/{deviceId}", produces =
        APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful retrieval of Variable sign history"))
    public List<TrafficSignHistory> trafficSigns(@PathVariable("deviceId") final String deviceId) {
        return variableSignService.listVariableSignHistory(deviceId);
    }
}