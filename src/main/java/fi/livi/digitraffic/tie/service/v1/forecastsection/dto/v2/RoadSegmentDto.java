package fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2;

public class RoadSegmentDto {

    private Integer startDistance;

    private Integer endDistance;

    private Integer carriageway;

    public RoadSegmentDto() {
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

    public Integer getCarriageway() {
        return carriageway;
    }

    public void setCarriageway(Integer carriageway) {
        this.carriageway = carriageway;
    }
}
