
package fi.livi.digitraffic.tie.dto.v2.trafficannouncement.geojson;

import java.time.ZonedDateTime;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Announcement time and duration", value = "TimeAndDurationV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "startTime",
    "endTime",
    "estimatedDuration"
})
public class TimeAndDuration extends JsonAdditionalProperties {

    @ApiModelProperty(value = "Start time of the situation", required = true)
    @NotNull
    public ZonedDateTime startTime;

    @ApiModelProperty(value = "End time of the situation. If the end time has been passed, the situation can be assumed to be over. If end time is not given, there will be follow-up announcement about the situation.")
    public ZonedDateTime endTime;

    @ApiModelProperty(value = "If exact endtime is not known, duration may be estimated.")
    public EstimatedDuration estimatedDuration;

    public TimeAndDuration() {
    }

    public TimeAndDuration(final ZonedDateTime startTime, final ZonedDateTime endTime, final EstimatedDuration estimatedDuration) {
        super();
        this.startTime = startTime;
        this.endTime = endTime;
        this.estimatedDuration = estimatedDuration;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
