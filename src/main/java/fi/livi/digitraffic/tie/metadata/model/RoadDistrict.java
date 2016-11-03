package fi.livi.digitraffic.tie.metadata.model;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;

@Entity
@Immutable
public class RoadDistrict {

    @Id
    @GenericGenerator(name = "SEQ_ROAD_DISTRICT", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD_DISTRICT"))
    @GeneratedValue(generator = "SEQ_ROAD_DISTRICT")
    private Long id;
    private int naturalId;
    private String name;
    private boolean obsolete;
    private LocalDate obsoleteDate;
    private Integer speedLimitSeason;

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

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(final boolean obsolete) {
        this.obsolete = obsolete;
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(final LocalDate obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public Integer getSpeedLimitSeason() {
        return speedLimitSeason;
    }

    public void setSpeedLimitSeason(final Integer speedLimitSeason) {
        this.speedLimitSeason = speedLimitSeason;
    }
}
