package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.API_TRAFFIC_SIGNS_UPDATE_PART_PATH;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.data.dto.trafficsigns.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import fi.livi.digitraffic.tie.data.service.TrafficSignsUpdateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "variable speed limits")
@RestController
@Validated
@RequestMapping(API_V1_BASE_PATH + API_TRAFFIC_SIGNS_UPDATE_PART_PATH)
@ConditionalOnWebApplication
public class TrafficSignsUpdateController {
    public static final String METADATA_PATH = "/metadata";
    public static final String DATA_PATH = "/data";

    private final TrafficSignsUpdateService trafficSignsService;

    public TrafficSignsUpdateController(final TrafficSignsUpdateService trafficSignsService) {
        this.trafficSignsService = trafficSignsService;
    }

    @ApiOperation("Posting variable speed limits from HARJA")
    @RequestMapping(method = RequestMethod.POST, path = METADATA_PATH, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful post of traffic signs metadata from TLOIK"))
    public ResponseEntity<Void> postTrafficSignsMetadata(@RequestBody MetadataSchema metadata) {
        trafficSignsService.saveMetadata(metadata);

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Posting variable speed limits from HARJA")
    @RequestMapping(method = RequestMethod.POST, path = DATA_PATH, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(@ApiResponse(code = SC_OK, message = "Successful post of traffic signs data from TLOIK"))
    public ResponseEntity<Void> postTrafficSignsData(@RequestBody DataSchema data) {
        trafficSignsService.saveData(data);

        return ResponseEntity.ok().build();
    }

}
