
package fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "AlertC area")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "locationCode",
    "type"
})
public class Area {

    @ApiModelProperty(value = "The name of the area", required = true)
    public String name;

    @ApiModelProperty(value = "Location code of the area, number of the road point in AlertC location table", required = true)
    public Integer locationCode;

    @JsonProperty("type")
    @ApiModelProperty(value = "The type of the area, example kaupunki, maakunta, sää-alue", required = true)
    public String type;

    public Area() {
    }

    public Area(String name, Integer locationCode, String type) {
        super();
        this.name = name;
        this.locationCode = locationCode;
        this.type = type;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
