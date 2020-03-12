package fi.livi.digitraffic.tie.model.v1.maintenance;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;

@Entity
public class WorkMachineObservation {

    /**
     * Enum to keep track of observation coordinates type
     */
    public enum WorkMachineObservationType {
        Point,
        LineString;

        public static WorkMachineObservationType valueOf(Geometry.Type geometryType) {
            return valueOf(geometryType.name());
        }

    }
    @Id
    @GenericGenerator(name = "SEQ_WORK_MACHINE_OBSERVATION", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_WORK_MACHINE_OBSERVATION"))
    @GeneratedValue(generator = "SEQ_WORK_MACHINE_OBSERVATION")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="WORK_MACHINE_ID", referencedColumnName = "ID", nullable = false, updatable = false)
    private WorkMachine workMachine;
    @OneToMany(mappedBy = "workMachineObservation", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderColumn(name = "order_number", nullable = false, updatable = false)
    private List<WorkMachineObservationCoordinate> coordinates = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private WorkMachineObservationType type;

    private BigDecimal direction;

    @Column
    private ZonedDateTime lastObservationTime;

    @Column
    private ZonedDateTime updated;

    @Column
    private Boolean transition;

    public WorkMachineObservation() {
    }

    public WorkMachineObservation(final WorkMachine workMachine, final ZonedDateTime observationTime,
                                  final WorkMachineObservationType observationType) {
        this.workMachine = workMachine;
        this.lastObservationTime = observationTime;
        this.type = observationType;
    }


    public Long getId() {
        return id;
    }

    public WorkMachine getWorkMachine() {
        return workMachine;
    }

    public List<WorkMachineObservationCoordinate> getCoordinates() {
        return coordinates;
    }

    public List<List<Double>> getListCoordinates() {
        return coordinates.stream().map(c -> Arrays.asList(c.getLongitude().doubleValue(), c.getLatitude().doubleValue())).collect(Collectors.toList());
    }

    public WorkMachineObservationType getType() {
        return type;
    }

    public void setObservationType(final WorkMachineObservationType observationType) {
        this.type = observationType;
    }

    public BigDecimal getDirection() {
        return direction;
    }

    public void setDirection(final BigDecimal direction) {
        this.direction = direction;
    }

    public ZonedDateTime getLastObservationTime() {
        return lastObservationTime;
    }

    public void setLastObservationTime(final ZonedDateTime lastObservationTime) {
        this.lastObservationTime = lastObservationTime;
    }

    public void setUpdated(ZonedDateTime updated) {
        this.updated = updated;
    }

    public ZonedDateTime getUpdated() {
        return updated;
    }

    public void setUpdatedNow() {
        setUpdated(ZonedDateTime.now());
    }


    /**
     * Observation is transition if it doesn't have tasks
     */
    public void setTransition(final boolean transition) {
        this.transition = transition;
    }

    /**
     * @return Observation is transition if it doesn't have tasks
     */
    public boolean isTransition() {
        return transition;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringExcluded(this, "coordinates");
    }
}
