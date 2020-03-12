package fi.livi.digitraffic.tie.dao.v1.forecast;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSection;

@Repository
public interface ForecastSectionRepository extends JpaRepository<ForecastSection, Long> {

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @EntityGraph(attributePaths = { "road", "startRoadSection", "endRoadSection", "startRoadSection.roadDistrict", "endRoadSection.roadDistrict",
                                    "forecastSectionCoordinateLists" }, type = EntityGraph.EntityGraphType.LOAD)
    List<ForecastSection> findDistinctByVersionIsOrderByNaturalIdAsc(final int version);

    @Modifying
    @Query(value = "DELETE FROM forecast_section_coordinate_list WHERE forecast_section_id IN " +
                   "(SELECT id FROM forecast_section WHERE version = :version AND obsolete_date IS NULL)",
           nativeQuery = true)
    void deleteCoordinates(@Param("version") final int version);

    @Modifying
    @Query(value = "DELETE FROM road_segment WHERE forecast_section_id IN " +
                   "(SELECT forecast_section_id FROM forecast_section WHERE version = :version AND obsolete_date IS NULL)",
           nativeQuery = true)
    void deleteRoadSegments(@Param("version") final int version);

    @Modifying
    @Query(value = "DELETE FROM link_id WHERE forecast_section_id IN (SELECT forecast_section_id FROM forecast_section WHERE version = :version)",
           nativeQuery = true)
    void deleteLinkIds(@Param("version") final int version);

    @Modifying
    @Query(value = "DELETE FROM forecast_section WHERE natural_id NOT IN (:naturalIds) AND version = :version", nativeQuery = true)
    void deleteAllNotIn(@Param("naturalIds") final List<String> naturalIds, @Param("version") final int version);
}
