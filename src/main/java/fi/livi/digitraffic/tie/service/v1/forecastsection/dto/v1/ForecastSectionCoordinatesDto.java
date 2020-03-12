package fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v1;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.Coordinate;

public class ForecastSectionCoordinatesDto {

    private final String naturalId;

    private final String name;

    private final List<Coordinate> coordinates;

    public ForecastSectionCoordinatesDto(final String naturalId, final String name, final List<List<BigDecimal>> coordinates) {
        this.naturalId = naturalId;
        this.name = name;
        this.coordinates = coordinates.stream().map(c -> new Coordinate(c)).collect(Collectors.toList());
    }

    public String getNaturalId() {
        return naturalId;
    }

    public String getName() {
        return name;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "ForecastSectionCoordinatesDto{" +
               "naturalId='" + naturalId + '\'' +
               ", name='" + name + '\'' +
               ", coordinates=" + coordinates +
               '}';
    }
}
