package fi.livi.digitraffic.tie.dto.v1.tms;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Sensor constant value")
@JsonPropertyOrder({ "name", "value", "validFrom", "validTo"})
@Entity
@Immutable
public class TmsSensorConstantValueDto {

    @JsonIgnore
    @Id
    private Long lotjuId;

    @JsonIgnore
    private Long roadStationId;

    @ApiModelProperty(value = "Name of the sensor constant", required = true)
    @NotNull
    private String name;

    @ApiModelProperty(value = "Value of the sensor constant", required = true)
    @NotNull
    private Integer value;

    @JsonIgnore
    @NotNull
    private Integer validFrom;

    @JsonIgnore
    @NotNull
    private Integer validTo;

    public TmsSensorConstantValueDto() {
    }

    public TmsSensorConstantValueDto(@NotNull final Long lotjuId,
                                     @NotNull final String name, @NotNull final Integer value,
                                     @NotNull final Integer validFrom, @NotNull final Integer validTo,
                                     @NotNull final Long roadStationId) {
        this.lotjuId = lotjuId;
        this.name = name;
        this.value = value;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.roadStationId = roadStationId;
    }

    public String getName() {
        return name;
    }

    public Integer getValue() {
        return value;
    }

    public Integer getValidFrom() {
        return validFrom;
    }

    public Integer getValidTo() {
        return validTo;
    }

    @ApiModelProperty(value = "Validity start in format mm-dd ie. value 01-31 is 31th of January", required = true)
    @JsonProperty("validFrom")
    public String getValidFromFormated() {
        return formatValidity(validFrom);
    }

    @ApiModelProperty(value = "Validity end in format mm-dd ie. value 01-31 is 31th of January", required = true)
    @JsonProperty("validTo")
    public String getValidToFormated() {
        return formatValidity(validTo);
    }

    public Long getRoadStationId() {
        return roadStationId;
    }

    private static String formatValidity(final Integer value) {
        return String.format("%02d-%02d", value / 100, value % 100);
    }
}
