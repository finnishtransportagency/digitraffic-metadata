package fi.livi.digitraffic.tie.metadata.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
public class ForecastSection {

    @ApiModelProperty(value = "Forecast section id")
    @Id
    @JsonProperty("id")
    private String naturalId;

    @ApiModelProperty(value = "Forecast section description")
    private String description;

    @ApiModelProperty(value = "Road section number")
    private int roadSectionNumber;

    @ApiModelProperty(value = "Forecast section road number")
    private int roadNumber;

    @ApiModelProperty(value = "Forecast section start distance")
    private int startDistance;

    @ApiModelProperty(value = "Forecast section road number")
    private int startSectionNumber;

    @ApiModelProperty(value = "Forecast section end number")
    private int endSectionNumber;

    @ApiModelProperty(value = "Forecast section end distance")
    private int endDistance;

    @ApiModelProperty(value = "Forecast section leght")
    private int length;

    public String getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final String naturalId) {
        this.naturalId = naturalId;
    }

    public int getRoadSectionNumber() {
        return roadSectionNumber;
    }

    public void setRoadSectionNumber(final int roadSectionNumber) {
        this.roadSectionNumber = roadSectionNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public int getRoadNumber() {
        return roadNumber;
    }

    public void setRoadNumber(final int roadNumber) {
        this.roadNumber = roadNumber;
    }

    public int getStartSectionNumber() {
        return startSectionNumber;
    }

    public void setStartSectionNumber(final int startSectionNumber) {
        this.startSectionNumber = startSectionNumber;
    }

    public int getStartDistance() {
        return startDistance;
    }

    public void setStartDistance(final int startDistance) {
        this.startDistance = startDistance;
    }

    public int getEndSectionNumber() {
        return endSectionNumber;
    }

    public void setEndSectionNumber(final int endSectionNumber) {
        this.endSectionNumber = endSectionNumber;
    }

    public int getEndDistance() {
        return endDistance;
    }

    public void setEndDistance(final int endDistance) {
        this.endDistance = endDistance;
    }

    public int getLength() {
        return length;
    }

    public void setLength(final int length) {
        this.length = length;
    }
}
