
package fi.livi.digitraffic.tie.model.v1.maintenance.harja;

import java.io.Serializable;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "sender",
    "messageIdentifier",
    "sendingTime"
})
public class Caption implements Serializable
{

    @JsonProperty(required = true)
    private Sender sender;

    @JsonProperty(required = true)
    private Integer messageIdentifier;

    @JsonProperty(required = true)
    private ZonedDateTime sendingTime;

    @JsonCreator
    public Caption(final Sender sender, final Integer messageIdentifier, final ZonedDateTime sendingTime) {
        this.sender = sender;
        this.messageIdentifier = messageIdentifier;
        this.sendingTime = sendingTime;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public Integer getMessageIdentifier() {
        return messageIdentifier;
    }

    public void setMessageIdentifier(Integer messageIdentifier) {
        this.messageIdentifier = messageIdentifier;
    }

    public ZonedDateTime getSendingTime() {
        return sendingTime;
    }

    public void setSendingTime(ZonedDateTime sendingTime) {
        this.sendingTime = sendingTime;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Caption)) {
            return false;
        }

        Caption caption = (Caption) o;

        return new EqualsBuilder()
            .append(getSender(), caption.getSender())
            .append(getMessageIdentifier(), caption.getMessageIdentifier())
            .append(getSendingTime(), caption.getSendingTime())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getSender())
            .append(getMessageIdentifier())
            .append(getSendingTime())
            .toHashCode();
    }
}
