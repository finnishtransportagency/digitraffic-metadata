
package fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Sender's contact information")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "phone",
    "fax",
    "email"
})
public class Contact {

    @ApiModelProperty("Phone number")
    public String phone;

    @ApiModelProperty("Fax number")
    public String fax;

    @ApiModelProperty("Email")
    public String email;

    public Contact() {
    }

    public Contact(String phone, String fax, String email) {
        super();
        this.phone = phone;
        this.fax = fax;
        this.email = email;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
