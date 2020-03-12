package fi.livi.digitraffic.tie.metadata.geojson.forecastsection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.model.v1.Road;
import fi.livi.digitraffic.tie.model.v1.RoadSection;
import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({ "naturalId", "description", "roadSectionNumber", "roadNumber", "roadSectionVersionNumber", "startDistance",
                     "endDistance", "length", "road", "startRoadSection", "endRoadSection" })
public class ForecastSectionProperties {

    @ApiModelProperty(value =
            "Forecast section identifier 15 characters ie. 00004_112_000_0: \n" +
            "1. Road number 5 characters ie. 00004, \n" +
            "2. Road section 3 characters ie. 112, \n" +
            "3. Road section version 3 characters ie. 000, \n" +
            "4. Reserved for future needs 1 characters default 0")
    @JsonProperty("id")
    private String naturalId;

    @ApiModelProperty(value = "Forecast section description")
    private String description;

    @ApiModelProperty(value = "Road section number")
    private int roadSectionNumber;

    @ApiModelProperty(value = "Forecast section road number")
    private int roadNumber;

    @ApiModelProperty(value = "Road section version number")
    private int roadSectionVersionNumber;

    @ApiModelProperty(value = "Forecast section start distance")
    private Integer startDistance;

    @ApiModelProperty(value = "Forecast section end distance")
    private Integer endDistance;

    @ApiModelProperty(value = "Forecast section length")
    private Integer length;

    @ApiModelProperty(value = "Road where forecast section is located")
    private Road road;

    @ApiModelProperty(value = "Road section where forecast section starts")
    private RoadSection startRoadSection;

    @ApiModelProperty(value = "Road section where forecast section ends")
    private RoadSection endRoadSection;

    public String getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(String naturalId) {
        this.naturalId = naturalId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRoadSectionNumber() {
        return roadSectionNumber;
    }

    public void setRoadSectionNumber(int roadSectionNumber) {
        this.roadSectionNumber = roadSectionNumber;
    }

    public int getRoadNumber() {
        return roadNumber;
    }

    public void setRoadNumber(int roadNumber) {
        this.roadNumber = roadNumber;
    }

    public int getRoadSectionVersionNumber() {
        return roadSectionVersionNumber;
    }

    public void setRoadSectionVersionNumber(int roadSectionVersionNumber) {
        this.roadSectionVersionNumber = roadSectionVersionNumber;
    }

    public Integer getStartDistance() {
        return startDistance;
    }

    public void setStartDistance(Integer startDistance) {
        this.startDistance = startDistance;
    }

    public Integer getEndDistance() {
        return endDistance;
    }

    public void setEndDistance(Integer endDistance) {
        this.endDistance = endDistance;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Road getRoad() {
        return road;
    }

    public void setRoad(Road road) {
        this.road = road;
    }

    public RoadSection getStartRoadSection() {
        return startRoadSection;
    }

    public void setStartRoadSection(RoadSection startRoadSection) {
        this.startRoadSection = startRoadSection;
    }

    public RoadSection getEndRoadSection() {
        return endRoadSection;
    }

    public void setEndRoadSection(RoadSection endRoadSection) {
        this.endRoadSection = endRoadSection;
    }
}
