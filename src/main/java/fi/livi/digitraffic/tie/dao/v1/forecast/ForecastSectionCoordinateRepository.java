package fi.livi.digitraffic.tie.dao.v1.forecast;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSectionCoordinate;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSectionCoordinatePK;

@Repository
public interface ForecastSectionCoordinateRepository extends JpaRepository<ForecastSectionCoordinate, ForecastSectionCoordinatePK> {

}
