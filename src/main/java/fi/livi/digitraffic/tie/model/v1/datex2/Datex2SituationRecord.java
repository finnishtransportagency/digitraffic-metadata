package fi.livi.digitraffic.tie.model.v1.datex2;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

@ApiModel(value = "SituationRecord", description = "Datex2 situation record")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@DynamicUpdate
@Table(name = "DATEX2_SITUATION_RECORD")
public class Datex2SituationRecord {

    @JsonIgnore
    @Id
    @GenericGenerator(name = "SEQ_DATEX2SITUATIONRECORD", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_DATEX2SITUATIONRECORD"))
    @GeneratedValue(generator = "SEQ_DATEX2SITUATIONRECORD")
    private Long id;

    @ApiModelProperty(value = "Record id", required = true)
    @NotNull
    private String situationRecordId;

    @JsonIgnore
    @ManyToOne()
    @JoinColumn(name = "DATEX2_SITUATION_ID", nullable = false)
    private Datex2Situation situation;

    @ApiModelProperty(value = "Record validy status", required = true)
    @NotNull
    @Enumerated(EnumType.STRING)
    private Datex2SituationRecordValidyStatus validyStatus;

    @ApiModelProperty(value = "Record creation date time", required = true)
    @NotNull
    private ZonedDateTime creationTime;

    @ApiModelProperty(value = "Record version date time", required = true)
    @NotNull
    private ZonedDateTime versionTime;

    @ApiModelProperty(value = "Record observation date time")
    private ZonedDateTime observationTime;

    @ApiModelProperty(value = "Record overall start date time", required = true)
    @NotNull
    private ZonedDateTime overallStartTime;

    @ApiModelProperty(value = "Record overall end date time")
    private ZonedDateTime overallEndTime;

    @ApiModelProperty(value = "Record type", required = true)
    @Enumerated(EnumType.STRING)
    @NotNull
    private Datex2SituationRecordType type;

    @ApiModelProperty(value = "Record comments", required = true)
    @OneToMany(mappedBy = "situationRecord", cascade = CascadeType.ALL)
    private List<SituationRecordCommentI18n> publicComments;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Datex2Situation getSituation() {
        return situation;
    }

    public void setSituation(Datex2Situation situation) {
        this.situation = situation;
    }

    public String getSituationRecordId() {
        return situationRecordId;
    }

    public void setSituationRecordId(String situationRecordId) {
        this.situationRecordId = situationRecordId;
    }

    public void setCreationTime(ZonedDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public ZonedDateTime getCreationTime() {
        return creationTime;
    }

    public void setVersionTime(ZonedDateTime versionTime) {
        this.versionTime = versionTime;
    }

    public ZonedDateTime getVersionTime() {
        return versionTime;
    }

    public void setObservationTime(ZonedDateTime observationTime) {
        this.observationTime = observationTime;
    }

    public ZonedDateTime getObservationTime() {
        return observationTime;
    }

    public Datex2SituationRecordValidyStatus getValidyStatus() {
        return validyStatus;
    }

    public void setValidyStatus(Datex2SituationRecordValidyStatus validyStatus) {
        this.validyStatus = validyStatus;
    }

    public void setOverallStartTime(ZonedDateTime overallStartTime) {
        this.overallStartTime = overallStartTime;
    }

    public ZonedDateTime getOverallStartTime() {
        return overallStartTime;
    }

    public void setOverallEndTime(ZonedDateTime overallEndTime) {
        this.overallEndTime = overallEndTime;
    }

    public ZonedDateTime getOverallEndTime() {
        return overallEndTime;
    }

    public Datex2SituationRecordType getType() {
        return type;
    }

    public void setType(Datex2SituationRecordType type) {
        this.type = type;
    }

    public List<SituationRecordCommentI18n> getPublicComments() {
        return publicComments;
    }

    public void setPublicComments(List<SituationRecordCommentI18n> publicComments) {
        this.publicComments = publicComments;
        publicComments.forEach(k-> k.setSituationRecord(this));
    }
}
