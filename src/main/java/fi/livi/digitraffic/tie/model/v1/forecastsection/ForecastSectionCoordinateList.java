package fi.livi.digitraffic.tie.model.v1.forecastsection;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
public class ForecastSectionCoordinateList {

    @EmbeddedId
    private ForecastSectionCoordinateListPK forecastSectionCoordinateListPK;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumns({ @JoinColumn(name="forecast_section_id", referencedColumnName = "forecast_section_id", nullable = false, insertable = false, updatable = false),
                   @JoinColumn(name="list_order_number", referencedColumnName = "order_number", nullable = false, insertable = false, updatable = false)})
    List<ForecastSectionCoordinate> forecastSectionCoordinates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="forecast_section_id", nullable = false, referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    private ForecastSection forecastSection;

    public ForecastSectionCoordinateList() {
    }

    public ForecastSectionCoordinateList(final ForecastSectionCoordinateListPK forecastSectionCoordinateListPK,
                                         final List<ForecastSectionCoordinate> forecastSectionCoordinates) {
        this.forecastSectionCoordinateListPK = forecastSectionCoordinateListPK;
        this.forecastSectionCoordinates = forecastSectionCoordinates;
    }

    public Long getForecastSectionId() {
        return forecastSectionCoordinateListPK.getForecastSectionId();
    }

    public Long getOrderNumber() {
        return forecastSectionCoordinateListPK.getOrderNumber();
    }

    public List<ForecastSectionCoordinate> getForecastSectionCoordinates() {
        return forecastSectionCoordinates;
    }

    public List<List<Double>> getListCoordinates() {
        return forecastSectionCoordinates.stream().map(c -> Arrays.asList(c.getLongitude().doubleValue(), c.getLatitude().doubleValue())).collect(Collectors.toList());
    }

    public void removeCoordinates() {
        forecastSectionCoordinates.forEach(c -> c.removeCoordinate());
        forecastSectionCoordinates.clear();
    }
}
