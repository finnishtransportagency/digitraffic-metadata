package fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2;

import java.util.List;

public class ForecastSectionV2PropertiesDto {

    private String id;

    private String description;

    private Integer roadNumber;

    private Integer roadSectionNumber;

    private Double totalLengthKm;

    private List<RoadSegmentDto> roadSegmentList;

    private List<Long> linkIdList;

    public ForecastSectionV2PropertiesDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRoadNumber() {
        return roadNumber;
    }

    public void setRoadNumber(Integer roadNumber) {
        this.roadNumber = roadNumber;
    }

    public Integer getRoadSectionNumber() {
        return roadSectionNumber;
    }

    public void setRoadSectionNumber(Integer roadSectionNumber) {
        this.roadSectionNumber = roadSectionNumber;
    }

    public Double getTotalLengthKm() {
        return totalLengthKm;
    }

    public void setTotalLengthKm(Double totalLengthKm) {
        this.totalLengthKm = totalLengthKm;
    }

    public List<RoadSegmentDto> getRoadSegmentList() {
        return roadSegmentList;
    }

    public void setRoadSegmentList(List<RoadSegmentDto> roadSegmentList) {
        this.roadSegmentList = roadSegmentList;
    }

    public List<Long> getLinkIdList() {
        return linkIdList;
    }

    public void setLinkIdList(List<Long> linkIdList) {
        this.linkIdList = linkIdList;
    }
}
