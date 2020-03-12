package fi.livi.digitraffic.tie.model.v1.datex2;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Situation", description = "Datex2 situation")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@DynamicUpdate
@Table(name = "DATEX2_SITUATION")
public class Datex2Situation {

    @JsonIgnore
    @Id
    @GenericGenerator(name = "SEQ_DATEX2SITUATION", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_DATEX2SITUATION"))
    @GeneratedValue(generator = "SEQ_DATEX2SITUATION")
    private Long id;

    @ApiModelProperty(value = "Situation id", required = true)
    @NotNull
    private String situationId;

    @JsonIgnore
    @ManyToOne()
    @JoinColumn(name = "DATEX2_ID", nullable = false)
    private Datex2 datex2;

    @ApiModelProperty(value = "Situation records", required = true)
    @NotNull
    @OneToMany(mappedBy = "situation", cascade = CascadeType.ALL)
    private List<Datex2SituationRecord> situationRecords;

    @ApiModelProperty(value = "Situation version date time")
    private ZonedDateTime versionTime;

    public Long getId() {
        return id;
    }

    public List<Datex2SituationRecord> getSituationRecords() {
        return situationRecords;
    }

    public void setSituationRecords(List<Datex2SituationRecord> situationRecords) {
        this.situationRecords = situationRecords;
    }

    public String getSituationId() {
        return situationId;
    }

    public void setSituationId(String situationId) {
        this.situationId = situationId;
    }

    public void setVersionTime(ZonedDateTime versionTime) {
        this.versionTime = versionTime;
    }

    public ZonedDateTime getVersionTime() {
        return versionTime;
    }

    public void addSituationRecord(Datex2SituationRecord situationRecord) {
        if (situationRecords == null) {
            situationRecords = new ArrayList<>();
        }
        situationRecords.add(situationRecord);
        situationRecord.setSituation(this);
    }

    public Datex2 getDatex2() {
        return datex2;
    }

    public void setDatex2(Datex2 datex2) {
        this.datex2 = datex2;
    }
}
