
package fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Location consisting of a single road point or a road segment between two road points")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "primaryPoint",
    "secondaryPoint",
    "direction",
    "directionDescription"
})
public class RoadAddressLocation {

    @ApiModelProperty(value = "Primary road point", required = true)
    public RoadPoint primaryPoint;

    @ApiModelProperty(value = "Secondary  road point")
    public RoadPoint secondaryPoint;

    @ApiModelProperty(value = "Affected road direction", required = true)
    public RoadAddressLocation.Direction direction;

    @ApiModelProperty(value = "Human readable description of the affected direction")
    public String directionDescription;

    public RoadAddressLocation() {
    }

    public RoadAddressLocation(RoadPoint primaryPoint, RoadPoint secondaryPoint, RoadAddressLocation.Direction direction, String directionDescription) {
        super();
        this.primaryPoint = primaryPoint;
        this.secondaryPoint = secondaryPoint;
        this.direction = direction;
        this.directionDescription = directionDescription;
    }

    public enum Direction {

        UNKNOWN,
        POS,
        NEG,
        BOTH;

        private final static Map<String, RoadAddressLocation.Direction> CONSTANTS = new HashMap<>();

        static {
            for (RoadAddressLocation.Direction c: values()) {
                CONSTANTS.put(c.name(), c);
            }
        }

        @JsonCreator
        public static RoadAddressLocation.Direction fromValue(String value) {
            RoadAddressLocation.Direction constant = CONSTANTS.get(value.toUpperCase());
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }
    }
}
