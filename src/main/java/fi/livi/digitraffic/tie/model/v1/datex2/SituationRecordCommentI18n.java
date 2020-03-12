package fi.livi.digitraffic.tie.model.v1.datex2;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "SituationRecordGeneralPublicComment", description = "Datex2 situation record general public comment")
@JsonPropertyOrder({ "lang", "value"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@DynamicUpdate
@Table(name = "SITUATION_RECORD_COMMENT_I18N")
public class SituationRecordCommentI18n {

    @JsonIgnore
    @Id
    @GenericGenerator(name = "SEQ_SITUATION_RECORD_COMMENT", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_SITUATION_RECORD_COMMENT"))
    @GeneratedValue(generator = "SEQ_SITUATION_RECORD_COMMENT")
    private Long id;

    @ApiModelProperty(value = "Comment language", required = true)
    @NotNull
    @Length(min = 2, max = 2)
    private String lang;

    @ApiModelProperty(value = "Comment value", required = true)
    @NotNull
    private String value;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="DATEX2_SITUATION_RECORD_ID", nullable = false)
    @Fetch(FetchMode.JOIN)
    private Datex2SituationRecord situationRecord;

    public SituationRecordCommentI18n() {}

    public SituationRecordCommentI18n(String lang) {
        this.lang = lang;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Datex2SituationRecord getSituationRecord() {
        return situationRecord;
    }

    public void setSituationRecord(Datex2SituationRecord situationRecord) {
        this.situationRecord = situationRecord;
    }

}
