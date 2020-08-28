
package fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Location consisting of one or more areas", value = "AreaLocationV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "areas"
})
public class AreaLocation {

    @ApiModelProperty(value = "List of areas", required = true)
    @NotNull
    public List<Area> areas = new ArrayList<>();

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    public AreaLocation() {
    }

    public AreaLocation(List<Area> areas) {
        super();
        this.areas = areas;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
