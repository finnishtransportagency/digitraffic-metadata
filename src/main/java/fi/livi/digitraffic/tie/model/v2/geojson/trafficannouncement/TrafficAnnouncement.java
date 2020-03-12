
package fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModelProperty;

/**
 * Announcement schema.
 * <p>
 * 
 * 
 */
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

    @ApiModelProperty(value = "Language of the announcement eq. fi, sv, en or ru", required = true)
    public String language;

    @ApiModelProperty(value = "Short description about the situation", required = true)
    public String title;

    @ApiModelProperty(value = "Location of an traffic situation announcement")
    public Location location;

    public LocationDetails locationDetails;

    public List<String> features = new ArrayList<String>();

    @ApiModelProperty(value = "Free comment")
    public String comment;

    public TimeAndDuration timeAndDuration;

    @ApiModelProperty(value = "Additional information.")
    public String additionalInformation;

    @ApiModelProperty(value = "Name of the sender", required = true)
    public String sender;

    public TrafficAnnouncement() {
    }

    public TrafficAnnouncement(String language, String title, Location location, LocationDetails locationDetails, List<String> features, String comment, TimeAndDuration timeAndDuration, String additionalInformation, String sender) {
        super();
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public TrafficAnnouncement withLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TrafficAnnouncement withTitle(String title) {
        this.title = title;
        return this;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LocationDetails getLocationDetails() {
        return locationDetails;
    }

    public void setLocationDetails(LocationDetails locationDetails) {
        this.locationDetails = locationDetails;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public TimeAndDuration getTimeAndDuration() {
        return timeAndDuration;
    }

    public void setTimeAndDuration(TimeAndDuration timeAndDuration) {
        this.timeAndDuration = timeAndDuration;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
