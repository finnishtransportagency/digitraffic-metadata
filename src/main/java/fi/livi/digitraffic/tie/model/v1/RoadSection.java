package fi.livi.digitraffic.tie.model.v1;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

@Entity
@JsonPropertyOrder({ "naturalId", "endDistance", "road", "roadDistrict" })
public class RoadSection {

    @Id
    @GenericGenerator(name = "SEQ_ROAD_SECTION", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD_SECTION"))
    @GeneratedValue(generator = "SEQ_ROAD_SECTION")
    @JsonIgnore
    private Long id;

    @ApiModelProperty(value = "Road section number")
    @JsonProperty(value = "id")
    private String naturalId;

    @ApiModelProperty(value = "Distance from the beginning of the road section. Always 0.")
    @JsonIgnore
    private Integer beginDistance;

    @ApiModelProperty(value = "Distance from the beginning to the end of the road section. (Length of the road section)")
    private Integer endDistance;

    @ManyToOne
    @JoinColumn(name = "road_district_id")
    @Fetch(FetchMode.JOIN)
    @ApiModelProperty(value = "District where road is located (or most of it)")
    private RoadDistrict roadDistrict;

    @ManyToOne
    @JoinColumn(name = "road_id")
    @Fetch(FetchMode.JOIN)
    @ApiModelProperty(value = "Road where this section is located")
    private Road road;

    @JsonIgnore
    private Date obsoleteDate;

    public RoadSection() {
    }

    public String getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(String naturalId) {
        this.naturalId = naturalId;
    }

    public Integer getBeginDistance() {
        return beginDistance;
    }

    public void setBeginDistance(Integer beginDistance) {
        this.beginDistance = beginDistance;
    }

    public Integer getEndDistance() {
        return endDistance;
    }

    public void setEndDistance(Integer endDistance) {
        this.endDistance = endDistance;
    }

    public RoadDistrict getRoadDistrict() {
        return roadDistrict;
    }

    public void setRoadDistrict(RoadDistrict roadDistrict) {
        this.roadDistrict = roadDistrict;
    }

    public Road getRoad() {
        return road;
    }

    public void setRoad(Road road) {
        this.road = road;
    }

    public Date getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(Date obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }
}
