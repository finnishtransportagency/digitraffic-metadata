package fi.livi.digitraffic.tie.model.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
@DynamicUpdate
@Table(name = "MAINTENANCE_REALIZATION_DATA")
public class MaintenanceRealizationData {

    public enum Status {
        UNHANDLED,
        HANDLED,
        ERROR
    }

    @Id
    @GenericGenerator(name = "SEQ_MAINTENANCE_REALIZATION_DATA", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_MAINTENANCE_REALIZATION_DATA"))
    @GeneratedValue(generator = "SEQ_MAINTENANCE_REALIZATION_DATA")
    private Long id;

    @Column
    private Long jobId;

    @Column
    private String json;

    @Column
    @Enumerated(EnumType.STRING)
    private Status status = Status.UNHANDLED;

    @Column(insertable = false, updatable = false) // auto generated
    private ZonedDateTime created;

    @Column(insertable = false, updatable = false) // auto updated
    private ZonedDateTime modified;

    @OneToMany(mappedBy = "realizationData", fetch = FetchType.LAZY)
    private Set<MaintenanceRealization> realizationData;

    @Column
    private String handlingInfo;

    public MaintenanceRealizationData() {
        // For Hibernate
    }

    public MaintenanceRealizationData(final Long jobId, final String json) {
        this.jobId = jobId;
        this.json = json;
    }

    public Long getId() {
        return id;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getJson() {
        return json;
    }

    public Status getStatus() {
        return status;
    }

    public void updateStatusToHandled() {
        if (Status.HANDLED.equals(status)) {
            throw new IllegalStateException(String.format("%s status is already %s", getClass().getSimpleName(), status));
        }
        status = Status.HANDLED;
    }

    public void updateStatusToError() {
        if (Status.HANDLED.equals(status) || Status.ERROR.equals(status)) {
            throw new IllegalStateException(String.format("%s status is already %s cannot be changed to %s", getClass().getSimpleName(), status, Status.ERROR));
        }
        status = Status.ERROR;
    }

    public void appendHandlingInfo(final String append) {
        this.handlingInfo = handlingInfo != null ? handlingInfo + ", " + append : append;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
