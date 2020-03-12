package fi.livi.digitraffic.tie.dto.v1;

import java.time.ZonedDateTime;

import io.swagger.annotations.ApiModelProperty;

public interface MeasuredDataObjectDto {

    @ApiModelProperty(value = "Value measured date time")
    ZonedDateTime getMeasuredTime();
}
