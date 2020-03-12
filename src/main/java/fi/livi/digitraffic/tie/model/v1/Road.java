package fi.livi.digitraffic.tie.model.v1;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

@Entity
@JsonPropertyOrder({ "naturalId", "name" })
public class Road {

    @Id
    @GenericGenerator(name = "SEQ_ROAD", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD"))
    @GeneratedValue(generator = "SEQ_ROAD")
    @JsonIgnore
    private Long id;

    @ApiModelProperty(value = "Road number")
    @JsonProperty(value = "id")
    private String naturalId;

    @JsonIgnore
    private Date obsoleteDate;

    @ApiModelProperty(value = "Road name")
    private String name;

    public Road() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(String naturalId) {
        this.naturalId = naturalId;
    }

    public Date getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(Date obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
