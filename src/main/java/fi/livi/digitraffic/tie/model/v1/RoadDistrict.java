package fi.livi.digitraffic.tie.model.v1;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.SpeedLimitSeason;
import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
@JsonPropertyOrder({ "id", "name" })
public class RoadDistrict {

    @Id
    @GenericGenerator(name = "SEQ_ROAD_DISTRICT", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD_DISTRICT"))
    @GeneratedValue(generator = "SEQ_ROAD_DISTRICT")
    @JsonIgnore
    private Long id;

    @ApiModelProperty(value = "Road district id (ELY)")
    @JsonProperty(value = "id")
    private int naturalId;

    @ApiModelProperty(value = "Road district name")
    private String name;

    @JsonIgnore
    private LocalDate obsoleteDate;

    @JsonIgnore
    private Integer speedLimitSeason;

    @ApiModelProperty("Road district speed limit season")
    @JsonProperty("speedLimitSeason")
    public SpeedLimitSeason getSpeedLimitSeason() {
        return speedLimitSeason.equals(SpeedLimitSeason.SUMMER.getCode()) ? SpeedLimitSeason.SUMMER : SpeedLimitSeason.WINTER;
    }

    public int getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final int naturalId) {
        this.naturalId = naturalId;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(final LocalDate obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    @JsonIgnore
    public Integer getSpeedLimitSeasonCode() {
        return speedLimitSeason;
    }

    public void setSpeedLimitSeason(final Integer speedLimitSeason) {
        this.speedLimitSeason = speedLimitSeason;
    }

    @Override
    public String toString() {
        return new ToStringHelper(this)
            .appendField("id", getId())
            .appendField("naturalId", getNaturalId())
            .appendField("name", getName())
            .toString();
    }
}
