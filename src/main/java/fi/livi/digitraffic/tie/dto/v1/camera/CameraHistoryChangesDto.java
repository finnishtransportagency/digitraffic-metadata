package fi.livi.digitraffic.tie.dto.v1.camera;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CameraHistory", description = "Weather cameras history changes.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraHistoryChangesDto {

    @ApiModelProperty("Latest history change time. Use this value as parameter for next query in api.")
    public final Instant latestChange;

    @ApiModelProperty("Changes of weather camera history")
    public final List<PresetHistoryChangesDto> changes;

    public CameraHistoryChangesDto(final Instant latestChange, final List<PresetHistoryChangesDto> changes) {
        this.latestChange = latestChange;
        this.changes = changes;
    }
}
