package fi.livi.digitraffic.tie.controller.integrations;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_INTEGRATIONS_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_WORK_MACHINE_PART_PATH;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.external.harja.ReittitoteumanKirjausRequestSchema;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.service.v1.MaintenanceDataService;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceRealizationUpdateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "maintenance", description = "Maintenance controller")
@RestController
@Validated
@RequestMapping(API_INTEGRATIONS_BASE_PATH + API_WORK_MACHINE_PART_PATH + "/v2")
@ConditionalOnWebApplication
public class V2RoadMaintenanceController {

    private static final Logger log = LoggerFactory.getLogger(V2RoadMaintenanceController.class);

    public static final String TRACKINGS_PATH = "/trackings";
    public static final String REALIZATIONS_PATH = "/realizations";

    final private MaintenanceDataService maintenanceDataService;
    private final V2MaintenanceRealizationUpdateService v2MaintenanceDataService;
    private final ObjectMapper objectMapper;

    @Autowired
    public V2RoadMaintenanceController(final MaintenanceDataService maintenanceDataService,
                                       final V2MaintenanceRealizationUpdateService v2MaintenanceDataService,
                                       final ObjectMapper objectMapper) {
        this.maintenanceDataService = maintenanceDataService;
        this.v2MaintenanceDataService = v2MaintenanceDataService;
        this.objectMapper = objectMapper;
    }

    @ApiOperation("Posting of real-time work machine tracking information for a work machine from HARJA")
    @RequestMapping(method = RequestMethod.POST, path = TRACKINGS_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful post of real-time work machine tracking information for a work machine from HARJA"))
    public ResponseEntity<Void> postWorkMachineTracking(@RequestBody TyokoneenseurannanKirjausRequestSchema tyokoneenseurannanKirjaus)
        throws JsonProcessingException {

        log.debug("method=postWorkMachineTracking JSON=\n{}",
                 objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tyokoneenseurannanKirjaus));

        maintenanceDataService.saveWorkMachineTrackingData(tyokoneenseurannanKirjaus);

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Posting of work machine tracking realization information for a work machine from HARJA")
    @RequestMapping(method = RequestMethod.POST, path = REALIZATIONS_PATH + "/{jobId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful post of work machine tracking realization information for a work machine from HARJA"))
    public ResponseEntity<Void> postWorkMachineRealization(
        @PathVariable("jobId") final Long jobId,
        @RequestBody ReittitoteumanKirjausRequestSchema reittitoteumanKirjaus)
        throws JsonProcessingException {

        log.debug("method=postWorkMachineRealization harjaJobId={} JSON=\n{}", jobId,
                 objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(reittitoteumanKirjaus));

        v2MaintenanceDataService.saveNewWorkMachineRealization(jobId, reittitoteumanKirjaus);

        return ResponseEntity.ok().build();
    }
}
