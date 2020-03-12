package fi.livi.digitraffic.tie.model.v1.maintenance;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class WorkMachineTaskPK implements Serializable {

    @Column(name = "WORK_MACHINE_COORDINATE_OBSERVATION_ID", nullable = false, insertable = false, updatable=false)
    private Long workMachineCoordinateObservationId;

    @Column(name = "WORK_MACHINE_COORDINATE_ORDER_NUMBER", nullable = false, insertable = false, updatable=false)
    private Integer workMachineCoordinateOrderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "task", nullable = false, insertable = false, updatable=false)
    private WorkMachineTask.Task task;

    public WorkMachineTaskPK() {
    }

    public WorkMachineTaskPK(final long workMachineCoordinateObservationId, final int workMachineCoordinateOrderNumber, WorkMachineTask.Task task) {
        this.workMachineCoordinateObservationId = workMachineCoordinateObservationId;
        this.workMachineCoordinateOrderNumber = workMachineCoordinateOrderNumber;
        this.task = task;
    }

    public Long getWorkMachineCoordinateObservationId() {
        return workMachineCoordinateObservationId;
    }

    public Integer getWorkMachineCoordinateOrderNumber() {
        return workMachineCoordinateOrderNumber;
    }

    public WorkMachineTask.Task getTask() {
        return task;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof WorkMachineTaskPK)) {
            return false;
        }

        WorkMachineTaskPK that = (WorkMachineTaskPK) o;

        return new EqualsBuilder()
            .append(getWorkMachineCoordinateObservationId(), that.getWorkMachineCoordinateObservationId())
            .append(getWorkMachineCoordinateOrderNumber(), that.getWorkMachineCoordinateOrderNumber())
            .append(getTask(), that.getTask())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getWorkMachineCoordinateObservationId())
            .append(getWorkMachineCoordinateOrderNumber())
            .append(getTask())
            .toHashCode();
    }
}
