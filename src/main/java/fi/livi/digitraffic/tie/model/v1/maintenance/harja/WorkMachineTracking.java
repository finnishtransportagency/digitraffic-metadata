package fi.livi.digitraffic.tie.model.v1.maintenance.harja;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import fi.livi.digitraffic.tie.conf.postgres.WorkMachineTrackingRecordUserType;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;

@TypeDef(name = "WorkMachineTrackingRecordUserType", typeClass = WorkMachineTrackingRecordUserType.class)
@Entity
@DynamicUpdate
@Table(name = "WORK_MACHINE_TRACKING")
public class WorkMachineTracking {

    @Id
    @GenericGenerator(name = "SEQ_WORK_MACHINE_TRACKING", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_WORK_MACHINE_TRACKING"))
    @GeneratedValue(generator = "SEQ_WORK_MACHINE_TRACKING")
    private Long id;

    @Column
    @Type(type = "WorkMachineTrackingRecordUserType")
    private WorkMachineTrackingRecord record;

    @Column
    @Enumerated(EnumType.STRING)
    private Geometry.Type type;

    @Column
    private ZonedDateTime created;

    @Column
    private ZonedDateTime handled;

    public WorkMachineTracking() {
    }

    public WorkMachineTracking(final WorkMachineTrackingRecord record) {
        this.record = record;
        created = ZonedDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public WorkMachineTrackingRecord getRecord() {
        return record;
    }

    public void setRecord(final WorkMachineTrackingRecord record) {
        this.record = record;
    }

    public Geometry.Type getType() {
        return type;
    }

    public void setType(final Geometry.Type type) {
        this.type = type;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public void setHandled(ZonedDateTime handled) {
        this.handled = handled;
    }

    public ZonedDateTime getHandled() {
        return handled;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof WorkMachineTracking)) {
            return false;
        }

        WorkMachineTracking that = (WorkMachineTracking) o;

        return new EqualsBuilder()
            .append(getId(), that.getId())
            .append(getRecord(), that.getRecord())
            .append(getType(), that.getType())
            .append(getCreated(), that.getCreated())
            .append(getHandled(), that.getHandled())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getId())
            .append(getRecord())
            .append(getType())
            .append(getCreated())
            .append(getHandled())
            .toHashCode();
    }
}
