
package fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement;

import javax.validation.constraints.Pattern;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Announcement estimated duration")
public class EstimatedDuration {

    // Regexp taken from Duration class
    @Pattern(regexp="([-+]?)P(?:([-+]?[0-9]+)D)?(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?", message="Invalid minimum duration!")
    @ApiModelProperty(value = "Estimated minimum duration using ISO-8601 duration", required = true, example = "PT6H")
    public String minimum;

    @Pattern(regexp="([-+]?)P(?:([-+]?[0-9]+)D)?(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?", message="Invalid maximum duration!")
    @ApiModelProperty(value = "Estimated maximum duration using ISO-8601 duration", dataType = "String", example = "PT8H")
    public String maximum;

    @ApiModelProperty(value = "Informal description e.g. 1 - 3 hours", required = true)
    public String informal;

    /**
     * No args constructor for use in serialization
     * 
     */
    public EstimatedDuration() {
    }

    public EstimatedDuration(final String minimum, final String maximum, final String informal) {
        super();
        this.minimum = minimum;
        this.maximum = maximum;
        this.informal = informal;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
