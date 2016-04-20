package fi.livi.digitraffic.tie.data.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
@JsonPropertyOrder({ "localTime", "utc"})
public class LamMeasurement {

    @ApiModelProperty(value = "LAM station identifier (naturalId)", required = true)
    @Id
    private long lamId;

    @ApiModelProperty(value = "Traffic volume in 5 minutes to direction 1", required = true)
    private long trafficVolume1;

    @ApiModelProperty(value = "Traffic volume in 5 minutes to direction 2", required = true)
    private long trafficVolume2;

    @ApiModelProperty(value = "Average speed to direction 1", required = true)
    private long averageSpeed1;

    @ApiModelProperty(value = "Average speed to direction 2", required = true)
    private long averageSpeed2;

    @JsonIgnore
    private LocalDateTime measured;

    public long getLamId() {
        return lamId;
    }

    public void setLamId(final long lamId) {
        this.lamId = lamId;
    }

    public long getTrafficVolume1() {
        return trafficVolume1;
    }

    public void setTrafficVolume1(final long trafficVolume1) {
        this.trafficVolume1 = trafficVolume1;
    }

    public long getTrafficVolume2() {
        return trafficVolume2;
    }

    public void setTrafficVolume2(final long trafficVolume2) {
        this.trafficVolume2 = trafficVolume2;
    }

    public long getAverageSpeed1() {
        return averageSpeed1;
    }

    public void setAverageSpeed1(final long averageSpeed1) {
        this.averageSpeed1 = averageSpeed1;
    }

    public long getAverageSpeed2() {
        return averageSpeed2;
    }

    public void setAverageSpeed2(final long averageSpeed2) {
        this.averageSpeed2 = averageSpeed2;
    }

    public LocalDateTime getMeasured() {
        return measured;
    }

    public void setMeasured(final LocalDateTime measured) {
        this.measured = measured;
    }

    @ApiModelProperty(value = "Timestamp in ISO 8601 format with time offsets from UTC (eg. 2016-04-20T12:38:16.328+03:00)", required = true)
    public String getLocalTime() {
        return ToStringHelpper.toString(measured, ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Timestamp in ISO 8601 UTC format (eg. 2016-04-20T09:38:16.328Z)", required = true)
    public String getUtc() {
        return ToStringHelpper.toString(measured, ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }
}
