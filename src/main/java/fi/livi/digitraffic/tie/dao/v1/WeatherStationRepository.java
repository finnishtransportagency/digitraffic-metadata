package fi.livi.digitraffic.tie.dao.v1;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v1.WeatherStation;

@Repository
public interface WeatherStationRepository extends JpaRepository<WeatherStation, Long> {

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = { "roadStation", "roadStation.roadAddress"})
    List<WeatherStation> findAll();

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadStation.roadAddress"})
    List<WeatherStation> findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();

    WeatherStation findByLotjuId(Long lotjuId);
}
