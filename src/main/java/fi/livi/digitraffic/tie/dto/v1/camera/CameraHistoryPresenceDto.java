package fi.livi.digitraffic.tie.dto.v1.camera;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CameraHistoryPresence", description = "Image history presence for camera and it's presets at given time interval.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraHistoryPresenceDto {

    @ApiModelProperty("Camera id.")
    public final String cameraId;

    @ApiModelProperty("If true then there is image history present for the camera at given time interval.")
    public final boolean historyPresent;

    @ApiModelProperty("History statuses of the camera presets at given time interval.")
    public final List<PresetHistoryPresenceDto> presetHistoryPresences;

    public CameraHistoryPresenceDto(final String cameraId, final List<PresetHistoryPresenceDto> presetHistoryPresences) {
        this.cameraId = cameraId;
        this.presetHistoryPresences = presetHistoryPresences;
        this.historyPresent = presetHistoryPresences.stream().map(ph -> ph.isHistoryPresent()).filter(Boolean::booleanValue).findFirst().orElse(false);
    }

}