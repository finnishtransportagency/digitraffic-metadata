
package fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "AlertC location", value = "AlertCLocationV3")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
   "locationCode",
   "name",
   "distance"
})
public class AlertCLocation extends JsonAdditionalProperties {

    @ApiModelProperty(value = "AlertC location code. Number of the location point in AlertC location table", required = true)
    @NotNull
    public Integer locationCode;

    @ApiModelProperty(value = "Location point name")
    @NotNull
    public String name;

    @JsonPropertyDescription("Distance of the road point from the AlertC location point")
    @NotNull
    public Integer distance;

    public AlertCLocation() {
    }

    public AlertCLocation(final Integer locationCode, final String name, final Integer distance) {
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
