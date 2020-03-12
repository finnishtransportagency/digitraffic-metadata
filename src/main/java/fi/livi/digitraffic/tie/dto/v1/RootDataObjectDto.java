package fi.livi.digitraffic.tie.dto.v1;

import java.time.ZonedDateTime;

import org.hibernate.annotations.Immutable;

import io.swagger.annotations.ApiModelProperty;

@Immutable
public class RootDataObjectDto {

    @ApiModelProperty(value = "Data last updated date time", required = true)
    private final ZonedDateTime dataUpdatedTime;

    public RootDataObjectDto(final ZonedDateTime dataUpdatedTime) {
        this.dataUpdatedTime = dataUpdatedTime;
    }

    public ZonedDateTime getDataUpdatedTime() {
        return dataUpdatedTime;
    }
}
