package fi.livi.digitraffic.tie.model.v1.datex2;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "FluencyClass")
@Entity
@Immutable
public class FluencyClass {

    @JsonIgnore
    @Id
    private Long id;

    @JsonIgnore
    @NotNull
    private BigDecimal lowerLimit;

    @JsonIgnore
    private BigDecimal upperLimit;

    @ApiModelProperty(value = "1 = stationary traffic,\n"
                            + "2 = queuing traffic,\n"
                            + "3 = slow traffic,\n"
                            + "4 = heavy traffic,\n"
                            + "5 = traffic flowing freely", required = true)
    private int code;

    @ApiModelProperty(value = "Name for fluency class", required = true)
    @NotNull
    private String nameEn;

    public int getCode() {
        return code;
    }

    public void setCode(final int code) {
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Column(name = "LOWER_LIMIT", nullable = false)
    public BigDecimal getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(final BigDecimal lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(final String nameEn) {
        this.nameEn = nameEn;
    }

    public BigDecimal getUpperLimit() {
        return upperLimit;
    }

    /**
     * Sets the upper limit of the ratio of this fluency class.
     *
     * @param   upperLimit  the upper limit of the ratio of this fluency class.
     *          Use <code>null</code>, if no upper limit is defined and any ratio above lower
     *          limit fits into this class.
     */
    public void setUpperLimit(final BigDecimal upperLimit) {
        this.upperLimit = upperLimit;
    }
}
