package fi.livi.digitraffic.tie.dto.v1.camera;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "PresetHistoryPresence", description = "Image history status for preset at given time interval.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface PresetHistoryChangesDto {

    @ApiModelProperty("Camera id")
    String getCameraId();

    @ApiModelProperty("Camera preset id")
    String getPresetId();

    @JsonIgnore
    @ApiModelProperty("Previous state for publicity")
    Boolean getPublishableFrom();

    @ApiModelProperty("New state for publicity")
    Boolean getPublishableTo();

    /* For some reason ZonedDateTime is not working here. It gives error:
       "java.lang.IllegalArgumentException: Projection type must be an interface" when value is asked */
    @ApiModelProperty("The time when change takes place. Also the last modified date of the image in history.")
    Instant getLastModified();

    @ApiModelProperty("Modification time of the history.")
    Instant getModified();
}