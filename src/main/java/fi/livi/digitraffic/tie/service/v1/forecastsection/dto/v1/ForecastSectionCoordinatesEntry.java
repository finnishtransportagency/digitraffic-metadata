package fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v1;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
public class ForecastSectionCoordinatesEntry {

    private final String name;

    private final List<List<BigDecimal>> coordinates;

    public ForecastSectionCoordinatesEntry(@JsonProperty("name") String name,
                                           @JsonProperty("coord") List<List<BigDecimal>> coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    public String getName() {
        return name;
    }

    public List<List<BigDecimal>> getCoordinates() {
        return coordinates;
    }
}
