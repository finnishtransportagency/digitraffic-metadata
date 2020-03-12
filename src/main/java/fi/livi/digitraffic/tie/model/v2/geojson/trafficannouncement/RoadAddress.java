
package fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Location in road address (road number + number of the road section + distance from the beginning of the road section")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "road",
    "roadSection",
    "distance"
})
public class RoadAddress {

    @ApiModelProperty(value = "Number of the road", required = true)
    public Integer road;

    @ApiModelProperty(value = "Number of the road section", required = true)
    @NotNull
    public Integer roadSection;

    @ApiModelProperty(value = "Distance from the beginning of the road section.", required = true)
    public Integer distance;

    public RoadAddress() {
    }

    public RoadAddress(Integer road, Integer roadSection, Integer distance) {
        super();
        this.road = road;
        this.roadSection = roadSection;
        this.distance = distance;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
