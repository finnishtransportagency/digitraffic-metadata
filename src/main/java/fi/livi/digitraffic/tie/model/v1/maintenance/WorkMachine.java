package fi.livi.digitraffic.tie.model.v1.maintenance;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
public class WorkMachine {

    @Id
    @GenericGenerator(name = "SEQ_WORK_MACHINE", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_WORK_MACHINE"))
    @GeneratedValue(generator = "SEQ_WORK_MACHINE")
    private Long id;
    private Long harjaId;
    private Long harjaUrakkaId;
    private String type;

    @JsonIgnore
    @OneToMany(mappedBy = "workMachine", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    List<WorkMachineObservation> observations;

    public WorkMachine() {

    }

    public WorkMachine(final long harjaId, final long harjaUrakkaId, final String type) {
        this.harjaId = harjaId;
        this.harjaUrakkaId = harjaUrakkaId;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHarjaId() {
        return harjaId;
    }

    public void setHarjaId(Long harjaId) {
        this.harjaId = harjaId;
    }

    public Long getHarjaUrakkaId() {
        return harjaUrakkaId;
    }

    public void setHarjaUrakkaId(Long harjaUrakkaId) {
        this.harjaUrakkaId = harjaUrakkaId;
    }

    public String getType() {
        return type;
    }

    public void setType(String machineType) {
        this.type = machineType;
    }

    public List<WorkMachineObservation> getObservations() {
        return observations;
    }

    public void setObservations(List<WorkMachineObservation> observations) {
        this.observations = observations;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringExcluded(this, "observations");
    }
}
