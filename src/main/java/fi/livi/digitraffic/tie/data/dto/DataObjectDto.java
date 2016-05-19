package fi.livi.digitraffic.tie.data.dto;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({"dataLocalTime", "dataUtc"})
public class DataObjectDto {

    @JsonIgnore
    private final ZonedDateTime timestamp;

    public DataObjectDto(final ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public DataObjectDto() {
        this.timestamp = ZonedDateTime.now();
    }

    @ApiModelProperty(value = "Data read " + ToStringHelpper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE, required = true)
    public String getDataLocalTime() {
        return ToStringHelpper.toString(timestamp, ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Data read " + ToStringHelpper.ISO_8601_UTC_TIMESTAMP_EXAMPLE, required = true)
    public String getDataUtc() {
        return ToStringHelpper.toString(timestamp, ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }
}