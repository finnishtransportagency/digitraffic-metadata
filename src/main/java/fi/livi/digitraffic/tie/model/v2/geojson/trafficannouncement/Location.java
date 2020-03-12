
package fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "AlertC location of a traffic situation announcement")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "countryCode",
    "locationTableNumber",
    "locationTableVersion",
    "description" })
public class Location {

    @ApiModelProperty(value = "AlertC country code defined by RDS (IEC 62106)", required = true)
    public Integer countryCode;

    @ApiModelProperty(value = "AlertC location table number. Country code + location table number fully identifies the table.", required = true)
    public Integer locationTableNumber;

    @ApiModelProperty(value = "AlertC location table version number", required = true)
    public String locationTableVersion;

    @ApiModelProperty(value = "Textual representation of the location", required = true)
    public String description;

    public Location() {
    }

    public Location(Integer countryCode, Integer locationTableNumber, String locationTableVersion, String description) {
        super();
        this.countryCode = countryCode;
        this.locationTableNumber = locationTableNumber;
        this.locationTableVersion = locationTableVersion;
        this.description = description;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
