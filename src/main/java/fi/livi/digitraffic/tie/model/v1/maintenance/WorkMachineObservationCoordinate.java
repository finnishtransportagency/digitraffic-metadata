package fi.livi.digitraffic.tie.model.v1.maintenance;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
public class WorkMachineObservationCoordinate {

    @EmbeddedId
    private WorkMachineObservationCoordinatePK workMachineObservationCoordinatePK;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="WORK_MACHINE_OBSERVATION_ID", referencedColumnName = "ID", nullable = false, insertable = false, updatable = false)
    private WorkMachineObservation workMachineObservation;

    @NotNull
    private BigDecimal longitude;

    @NotNull
    private BigDecimal latitude;

    @Column
    private ZonedDateTime observationTime;

    @OneToMany(mappedBy = "workMachineObservationCoordinate", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<WorkMachineTask> workMachineTasks = new HashSet<>();

    public WorkMachineObservationCoordinate() {
    }

    public WorkMachineObservationCoordinate(final @NotNull WorkMachineObservationCoordinatePK workMachineObservationCoordinatePK,
                                            final @NotNull BigDecimal longitude, final @NotNull BigDecimal latitude,
                                            final ZonedDateTime observationTime) {
        this.workMachineObservationCoordinatePK = workMachineObservationCoordinatePK;
        this.longitude = longitude;
        this.latitude = latitude;
        this.observationTime = observationTime;
    }

    public Long getWorkMachineObservationId() {
        return workMachineObservationCoordinatePK.getWorkMachineObservationId();
    }

    public Integer getOrderNumber() {
        return workMachineObservationCoordinatePK.getOrderNumber();
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public ZonedDateTime getObservationTime() {
        return observationTime;
    }

    public Set<WorkMachineTask> getWorkMachineTasks() {
        return workMachineTasks;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringExcluded(this, "workMachineObservation");
    }
}
