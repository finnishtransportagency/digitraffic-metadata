package fi.livi.digitraffic.tie.controller.integrations;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_MAINTENANCE_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;

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
import fi.livi.digitraffic.tie.service.v1.MaintenanceDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "maintenance")
@RestController
@Validated
@RequestMapping(API_V1_BASE_PATH + API_MAINTENANCE_PART_PATH)
@ConditionalOnWebApplication
public class V1RoadMaintenanceController {

    private static final Logger log = LoggerFactory.getLogger(V1RoadMaintenanceController.class);

    public static final String WORK_MACHINE_TRACKING_PATH = "/tracking/work_machine";

    final private MaintenanceDataService maintenanceDataService;
    private final ObjectMapper objectMapper;

    @Autowired
    public V1RoadMaintenanceController(final MaintenanceDataService maintenanceDataService,
                                       final ObjectMapper objectMapper) {
        this.maintenanceDataService = maintenanceDataService;
        this.objectMapper = objectMapper;
    }

    @ApiOperation("Posting real-time tracking information for a work machine from HARJA")
    @RequestMapping(method = RequestMethod.POST, path = WORK_MACHINE_TRACKING_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(@ApiResponse(code = 200, message = "Successful post of real-time tracking information for a work machine from HARJA"))
    public ResponseEntity<Void> postWorkMachineTrackingData(@RequestBody TyokoneenseurannanKirjausRequestSchema tyokoneenseurannanKirjaus)
        throws JsonProcessingException {

        log.debug("method=postWorkMachineTrackingData JSON=\n{}",
                 objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tyokoneenseurannanKirjaus));

        maintenanceDataService.saveWorkMachineTrackingData(tyokoneenseurannanKirjaus);

        return ResponseEntity.ok().build();
    }
}
