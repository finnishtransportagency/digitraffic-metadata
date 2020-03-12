package fi.livi.digitraffic.tie.dto.v1.camera;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "PresetHistoryPresence", description = "Image history status for preset at given time interval.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface PresetHistoryPresenceDto {

    @JsonIgnore
    String getCameraId();

    @ApiModelProperty("Camera preset id")
    String getPresetId();

    @ApiModelProperty("If true then there is image history present for the preset at given time interval.")
    boolean isHistoryPresent();

}