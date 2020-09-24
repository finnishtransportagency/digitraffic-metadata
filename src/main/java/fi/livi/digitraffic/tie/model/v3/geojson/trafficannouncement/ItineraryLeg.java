
package fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "ItineraryLeg", value = "ItineraryLegV3")
@JsonPropertyOrder({
    "roadLeg",
    "streetName"
})
public class ItineraryLeg {

    @ApiModelProperty(value = "Road leg")
    public ItineraryRoadLeg roadLeg;

    @ApiModelProperty(value = "Name of the street")
    public String streetName;

    @JsonIgnore
    @Valid
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public ItineraryLeg() {
    }

    public ItineraryLeg(final ItineraryRoadLeg roadLeg, final String streetName) {
        super();
        this.roadLeg = roadLeg;
        this.streetName = streetName;
    }
}