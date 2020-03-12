package fi.livi.digitraffic.tie.dao.v1.forecast;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastConditionReason;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSectionWeatherPK;

@Repository
public interface ForecastConditionReasonRepository extends JpaRepository<ForecastConditionReason, ForecastSectionWeatherPK> {
}
