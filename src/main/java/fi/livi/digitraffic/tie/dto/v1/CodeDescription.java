package fi.livi.digitraffic.tie.dto.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="Description of code")
public interface CodeDescription {
    @ApiModelProperty(value = "Code", required = true)
    String getCode();
    @ApiModelProperty(value = "Description of the code (Finnish)", required = true)
    String getDescription();
}
