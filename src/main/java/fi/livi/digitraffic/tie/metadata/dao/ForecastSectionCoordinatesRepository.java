package fi.livi.digitraffic.tie.metadata.dao;

import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionCoordinates;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionCoordinatesPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForecastSectionCoordinatesRepository extends JpaRepository<ForecastSectionCoordinates, ForecastSectionCoordinatesPK> {

}