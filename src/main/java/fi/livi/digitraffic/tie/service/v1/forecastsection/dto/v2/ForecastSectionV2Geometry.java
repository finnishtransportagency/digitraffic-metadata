package fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.Coordinate;

public class ForecastSectionV2Geometry {

    private String type;

    private List<List<List<BigDecimal>>> coordinates;

    public ForecastSectionV2Geometry() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<List<Coordinate>> getCoordinates() {
        return coordinates.stream().map(list -> list.stream().map(c -> new Coordinate(c)).collect(Collectors.toList())).collect(Collectors.toList());
    }

    public void setCoordinates(List<List<List<BigDecimal>>> coordinates) {
        this.coordinates = coordinates;
    }
}
