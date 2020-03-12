
package fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "AlertC location")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "locationCode",
    "name",
    "distance"
})
public class AlertCLocation {

    @ApiModelProperty(value = "Number of the location point in AlertC location table", required = true)
    public Integer locationCode;

    @ApiModelProperty(value = "Location point name")
    public String name;

    @ApiModelProperty(value = "Distance of the road point from the AlertC location point", required = true)
    public Integer distance;

    public AlertCLocation() {
    }

    public AlertCLocation(Integer locationCode, String name, Integer distance) {
        super();
        this.locationCode = locationCode;
        this.name = name;
        this.distance = distance;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
