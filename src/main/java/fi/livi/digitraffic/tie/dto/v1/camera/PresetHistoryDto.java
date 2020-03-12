package fi.livi.digitraffic.tie.dto.v1.camera;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "PresetHistory", description = "Weather camera preset's image history.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresetHistoryDto {

    @ApiModelProperty("Preset id")
    public final String presetId;

    @ApiModelProperty("Preset history")
    public final List<PresetHistoryDataDto> presetHistory;

    public PresetHistoryDto(final String presetId, final List<PresetHistoryDataDto> presetHistory) {
        this.presetId = presetId;
        this.presetHistory = presetHistory;
    }

    public String getPresetId() {
        return presetId;
    }
}
