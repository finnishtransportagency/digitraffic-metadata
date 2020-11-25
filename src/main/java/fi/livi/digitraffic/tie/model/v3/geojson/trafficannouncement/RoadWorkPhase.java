
package fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModelProperty;

/**
 * Road work phase
 * <p>
 * A single phase in a larger road work
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "location",
    "locationDetails",
    "features",
    "workingHours",
    "comment",
    "timeAndDuration"
})
public class RoadWorkPhase extends JsonAdditionalProperties {

    @ApiModelProperty(value = "id", required = true)
    @NotNull
    public String id;

    @ApiModelProperty(value = "Location of an traffic situation announcement")
    public Location location;

    @ApiModelProperty(value = "locationDetails")
    public LocationDetails locationDetails;

    @ApiModelProperty(value = "Features of a traffic situation announcement", required = true)
    @NotNull
    public List<Feature> features = new ArrayList<>();

    @ApiModelProperty(value = "Severity of the disruption to traffic. How severely this road work phase disrupts traffic. LOW - no disruption, " +
                               "HIGH - disruption, HIGHEST - significant disruption", required = true)
    @NotNull
    public Severity severity;

    @ApiModelProperty(value = "WorkingHours of an traffic situation announcement", required = true)
    @NotNull
    public List<WorkingHour> workingHours = new ArrayList<>();

    @ApiModelProperty(value = "Free comment")
    public String comment;

    @ApiModelProperty(value = "Time and duration of an traffic situation announcement", required = true)
    @NotNull
    public TimeAndDuration timeAndDuration;

    public RoadWorkPhase() {
    }

    public RoadWorkPhase(final String id, final Location location, final LocationDetails locationDetails, final List<Feature> features,
                         final Severity severity, final List<WorkingHour> workingHours, final String comment, final TimeAndDuration timeAndDuration) {
        this.id = id;
        this.location = location;
        this.locationDetails = locationDetails;
        this.features = features;
        this.severity = severity;
        this.workingHours = workingHours;
        this.comment = comment;
        this.timeAndDuration = timeAndDuration;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    public enum Severity {

        LOW,
        HIGH,
        HIGHEST;

        @JsonCreator
        public static Severity fromValue(final String value) {
            return Severity.valueOf(value.toUpperCase());
        }
    }
}
