package fi.livi.digitraffic.tie.data.dto;

import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModelProperty;

@Immutable
@JsonPropertyOrder({ "measuredLocalTime", "measuredUtc" })
public interface MeasuredDataObjectDto {

    @JsonIgnore
    LocalDateTime getMeasured();

    @ApiModelProperty(value = "Value measured " + ToStringHelpper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE)
    default String getMeasuredLocalTime() {
        return ToStringHelpper.toString(getMeasured(), ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Value measured " + ToStringHelpper.ISO_8601_UTC_TIMESTAMP_EXAMPLE)
    default String getMeasuredUtc() {
        return ToStringHelpper.toString(getMeasured(), ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }
}
