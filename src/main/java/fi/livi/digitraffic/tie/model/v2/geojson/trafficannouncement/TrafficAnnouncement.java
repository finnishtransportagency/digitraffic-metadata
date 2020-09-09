
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

@ApiModel(description = "Announcement time and duration", value = "TrafficAnnouncementV2")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "language",
    "title",
    "location",
    "locationDetails",
    "features",
    "comment",
    "timeAndDuration",
    "additionalInformation",
    "sender"
})
public class TrafficAnnouncement {

    @ApiModelProperty(value = "Language of the announcement eq. fi, sv, en or ru. A subset of ISO 639-1.", required = true)
    public String language;

    @ApiModelProperty(value = "Short description about the situation", required = true)
    public String title;

    @ApiModelProperty(value = "Location of an traffic situation announcement")
    public Location location;

    @ApiModelProperty(value = "TODO")
    public LocationDetails locationDetails;

    @ApiModelProperty(value = "Features of the announcement")
    public List<String> features = new ArrayList<>();

    @ApiModelProperty(value = "Free comment")
    public String comment;

    @ApiModelProperty(value = "Time and expected duration of the announcement.")
    public TimeAndDuration timeAndDuration;

    @ApiModelProperty(value = "Additional information.")
    public String additionalInformation;

    @ApiModelProperty(value = "Name of the sender", required = true)
    @NotNull
    public String sender;

    @JsonIgnore
    public Map<String, Object> additionalProperties = new HashMap<>();

    public TrafficAnnouncement() {
    }

    public TrafficAnnouncement(final String language, final String title, final Location location, final LocationDetails locationDetails,
                               final List<String> features, final String comment, final TimeAndDuration timeAndDuration,
                               final String additionalInformation, final String sender) {
        this.language = language;
        this.title = title;
        this.location = location;
        this.locationDetails = locationDetails;
        this.features = features;
        this.comment = comment;
        this.timeAndDuration = timeAndDuration;
        this.additionalInformation = additionalInformation;
        this.sender = sender;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
