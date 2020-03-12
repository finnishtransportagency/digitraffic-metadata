package fi.livi.digitraffic.tie.dao.v1.tms;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.v1.TmsStation;

@Repository
public interface TmsStationRepository extends JpaRepository<TmsStation, Long> {

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadDistrict", "roadStation.roadAddress"})
    List<TmsStation> findAll();

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadDistrict", "roadStation.roadAddress"})
    List<TmsStation> findByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsOrderByRoadStation_NaturalId(final CollectionStatus collectionStatus);

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadDistrict", "roadStation.roadAddress"})
    List<TmsStation> findByRoadStationPublishableIsTrueOrderByRoadStation_NaturalId();

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadDistrict", "roadStation.roadAddress"})
    List<TmsStation> findByRoadStationIsPublicIsTrueOrderByRoadStation_NaturalId();

    TmsStation findByRoadStation_NaturalIdAndRoadStationPublishableIsTrue(final long roadStationNaturalId);

    @Query("SELECT CASE WHEN COUNT(tms) > 0 THEN TRUE ELSE FALSE END\n" +
           "FROM TmsStation tms\n" +
           "WHERE tms.roadStation.naturalId = ?1\n" +
           "  AND tms.roadStation.obsoleteDate is null\n" +
           "  AND tms.roadStation.isPublic = true")
    boolean tmsExistsWithRoadStationNaturalId(final long roadStationNaturalId);

    TmsStation findByRoadStationIsPublicIsTrueAndRoadStation_NaturalId(final Long id);

    TmsStation findByRoadStationIsPublicIsTrueAndNaturalId(final Long lamId);

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadDistrict", "roadStation.roadAddress"})
    List<TmsStation> findByRoadStationPublishableIsTrueAndRoadStationRoadAddressRoadNumberIsOrderByRoadStation_NaturalId(final Integer
        roadNumber);

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadDistrict", "roadStation.roadAddress"})
    List<TmsStation> findByRoadStationIsPublicIsTrueAndRoadStationCollectionStatusIsAndRoadStationRoadAddressRoadNumberIsOrderByRoadStation_NaturalId(
        final CollectionStatus removedPermanently, final Integer roadNumber);

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @EntityGraph(attributePaths = {"roadStation", "roadDistrict", "roadStation.roadAddress"})
    List<TmsStation> findByRoadStationIsPublicIsTrueAndRoadStationRoadAddressRoadNumberIsOrderByRoadStation_NaturalId(final Integer
        roadNumber);

    @EntityGraph(attributePaths = {"roadStation", "roadDistrict", "roadStation.roadAddress"})
    TmsStation findByLotjuId(Long lotjuId);

    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "1000"))
    @Query("select p.lotjuId\n" +
           "from #{#entityName} p\n")
    List<Long> findAllTmsStationsLotjuIds();
}
