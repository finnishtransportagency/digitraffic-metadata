package fi.livi.digitraffic.tie.model.v3;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="Description of code")
public interface V3CodeDescription {
    @ApiModelProperty(value = "Code", required = true)
    String getCode();
    @ApiModelProperty(value = "Description of the code(Finnish)", required = true)
    String getDescriptionFi();
    @ApiModelProperty(value = "Description of the code(English", required = true)
    String getDescriptionEn();
}
