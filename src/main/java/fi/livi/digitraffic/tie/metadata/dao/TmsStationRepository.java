package fi.livi.digitraffic.tie.metadata.dao;

import java.util.List;
import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.TmsStation;

@Repository
public interface TmsStationRepository extends JpaRepository<TmsStation, Long> {
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadDistrict", "roadStation.roadAddress"})
    List<TmsStation> findByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByRoadStation_NaturalId(final CollectionStatus collectionStatus);

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadDistrict", "roadStation.roadAddress"})
    List<TmsStation> findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();

    TmsStation findByRoadStation_NaturalIdAndRoadStationPublishableIsTrue(long roadStationNaturalId);

    @Query("SELECT CASE WHEN COUNT(tms) > 0 THEN TRUE ELSE FALSE END\n" +
           "FROM TmsStation tms\n" +
           "WHERE tms.roadStation.naturalId = ?1\n" +
           "  AND tms.roadStation.obsolete = false\n" +
           "  AND tms.roadStation.isPublic = true")
    boolean tmsExistsWithRoadStationNaturalId(long roadStationNaturalId);

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadDistrict", "roadStation.roadAddress"})
    List<TmsStation> findByLotjuIdIsNull();

    TmsStation findByRoadStationIsPublicIsTrueAndRoadStation_NaturalId(final Long id);
}
