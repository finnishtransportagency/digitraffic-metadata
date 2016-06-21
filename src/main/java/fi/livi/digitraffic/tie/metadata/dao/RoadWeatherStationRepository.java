package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;

@Repository
public interface RoadWeatherStationRepository extends JpaRepository<RoadWeatherStation, Long> {

    @EntityGraph("roadWeatherStation")
    @Override
    List<RoadWeatherStation> findAll();

    List<RoadWeatherStation> findByRoadStationObsoleteFalseAndIsPublicTrueOrderByRoadStation_NaturalId();

    @Query(value =
            "SELECT rws.roadStation.naturalId\n" +
            "FROM RoadWeatherStation rws\n" +
            "WHERE rws.isPublic = 1\n" +
            "  AND rws.roadStation.obsolete = 0")
    List<Long> findNonObsoleteAndPublicRoadStationNaturalIds();
}
