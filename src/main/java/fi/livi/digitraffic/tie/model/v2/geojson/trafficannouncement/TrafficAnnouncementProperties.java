package fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Traffic Announcement properties", value = "TrafficAnnouncementProperties")
@JsonPropertyOrder({
    "situationId",
    "messageType",
    "version",
    "releaseTime",
    "locationToDisplay",
    "announcements",
    "contact"
})
public class TrafficAnnouncementProperties {

    @ApiModelProperty(value = "Situation id", required = true)
    public final String situationId;

    @ApiModelProperty(value = "Announcement version", required = true)
    public final Integer version;

    @ApiModelProperty(value = "Annoucement release time", required = true)
    public final ZonedDateTime releaseTime;

    @ApiModelProperty(value = "Location to display in ETRS-TM35FIN coordinate format.")
    public final LocationToDisplay locationToDisplay;

    @JsonProperty("announcements")
    @ApiModelProperty(value = "Contains announcement's different language versions available.", required = true)
    public final List<TrafficAnnouncement> announcements;

    @ApiModelProperty(value = "Sender's contact information")
    public final Contact contact;

    @ApiModelProperty(value = "Message type")
    private Datex2MessageType messageType;

    public TrafficAnnouncementProperties(final String situationId, final Integer version, final ZonedDateTime releaseTime,
                                         final LocationToDisplay locationToDisplay, final List<TrafficAnnouncement> announcements,
                                         final Contact contact) {
        super();
        this.situationId = situationId;
        this.version = version;
        this.releaseTime = releaseTime;
        this.locationToDisplay = locationToDisplay;
        this.announcements = announcements;
        this.contact = contact;
    }

    public Datex2MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(final Datex2MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
