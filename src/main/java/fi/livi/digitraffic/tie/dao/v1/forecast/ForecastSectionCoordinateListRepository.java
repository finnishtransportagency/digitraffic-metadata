package fi.livi.digitraffic.tie.dao.v1.forecast;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSectionCoordinateList;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSectionCoordinateListPK;

@Repository
public interface ForecastSectionCoordinateListRepository extends JpaRepository<ForecastSectionCoordinateList, ForecastSectionCoordinateListPK> {

}
