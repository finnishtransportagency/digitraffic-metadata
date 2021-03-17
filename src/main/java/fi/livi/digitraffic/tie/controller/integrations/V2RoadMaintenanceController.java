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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.service.v2.maintenance.V2MaintenanceTrackingUpdateService;
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

    private final V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService;
    private final ObjectMapper objectMapper;

    @Autowired
    public V2RoadMaintenanceController(final V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService,
                                       final ObjectMapper objectMapper) {
        this.v2MaintenanceTrackingUpdateService = v2MaintenanceTrackingUpdateService;
        this.objectMapper = objectMapper;
    }

    @ApiOperation("Posting of real-time work machine tracking information for a work machine from HARJA")
    @RequestMapping(method = RequestMethod.POST, path = TRACKINGS_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful post of real-time work machine tracking information for a work machine from HARJA"))
    public ResponseEntity<Void> postWorkMachineTracking(@RequestBody TyokoneenseurannanKirjausRequestSchema tyokoneenseurannanKirjaus)
        throws JsonProcessingException {

        if(log.isDebugEnabled()) {
            log.debug("method=postWorkMachineTracking JSON=\n{}",
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tyokoneenseurannanKirjaus));
        }
        v2MaintenanceTrackingUpdateService.saveMaintenanceTrackingData(tyokoneenseurannanKirjaus);

        return ResponseEntity.ok().build();
    }
}
