package fi.livi.digitraffic.tie.metadata.geojson;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;

import fi.livi.digitraffic.tie.model.JsonAdditionalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON object", discriminator = "type")
public abstract class GeoJsonObject extends JsonAdditionalProperties implements Serializable {
    private static final Logger log = getLogger(GeoJsonObject.class);

    public GeoJsonObject() {
    }

    @ApiModelProperty(value = "GeoJSON Object Type", required = true)
    public abstract String getType();

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
