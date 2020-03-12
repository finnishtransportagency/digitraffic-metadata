package fi.livi.digitraffic.tie.model.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.locationtech.jts.geom.LineString;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
@Table(name = "MAINTENANCE_REALIZATION")
public class MaintenanceRealization {

    @Id
    @GenericGenerator(name = "SEQ_MAINTENANCE_REALIZATION", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_MAINTENANCE_REALIZATION"))
    @GeneratedValue(generator = "SEQ_MAINTENANCE_REALIZATION")
    private Long id;

    @Column
    private Long jobId;

    @Column
    private String sendingSystem;

    @Column
    private ZonedDateTime sendingTime;

    @Column
    private ZonedDateTime startTime;

    @Column
    private ZonedDateTime endTime;

    @Column
    private Integer messageId;

    @NotNull
    @Column
    private LineString lineString;

    @Column(insertable = false, updatable = false) // auto generated
    private ZonedDateTime created;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REALIZATION_DATA_ID", referencedColumnName = "ID", nullable = false, updatable = false)
    private MaintenanceRealizationData realizationData;

    @Fetch(FetchMode.JOIN)
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name="MAINTENANCE_REALIZATION_TASK",
        joinColumns = @JoinColumn(name="realization_id"),
        inverseJoinColumns = @JoinColumn(name="task_id")
    )
    private Set<MaintenanceTask> tasks = new HashSet<>();


    public MaintenanceRealization() {
        // For Hibernate
    }

    public MaintenanceRealization(final MaintenanceRealizationData wmrd, final String sendingSystem, final Integer messageId,
                                  final ZonedDateTime sendingTime, final ZonedDateTime startTime, final ZonedDateTime endTime,
                                  final LineString lineString, final Set<MaintenanceTask> tasks) {
        this.realizationData = wmrd;
        this.sendingSystem = sendingSystem;
        this.messageId = messageId;
        this.sendingTime = sendingTime;
        this.jobId = wmrd.getJobId();
        this.startTime = startTime;
        this.endTime = endTime;
        this.lineString = lineString;
        this.tasks.addAll(tasks);
    }

    public Long getId() {
        return id;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getSendingSystem() {
        return sendingSystem;
    }

    public ZonedDateTime getSendingTime() {
        return sendingTime;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public LineString getLineString() {
        return lineString;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public MaintenanceRealizationData getRealizationData() {
        return realizationData;
    }

    public Set<MaintenanceTask> getTasks() {
        return tasks;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
