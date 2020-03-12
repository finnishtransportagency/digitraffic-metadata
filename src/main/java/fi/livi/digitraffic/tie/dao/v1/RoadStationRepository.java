package fi.livi.digitraffic.tie.dao.v1;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.RoadStationType;

@Repository
public interface RoadStationRepository extends JpaRepository<RoadStation, Long>{

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @EntityGraph(attributePaths = { "roadAddress" }, type = EntityGraph.EntityGraphType.LOAD)
    List<RoadStation> findByType(RoadStationType type);

    @Query("SELECT CASE WHEN count(rs) > 0 THEN TRUE ELSE FALSE END\n" +
           "FROM RoadStation rs\n" +
           "WHERE rs.publishable = true\n" +
           "  AND rs.type = :roadStationType\n" +
           "  AND rs.naturalId = :roadStationNaturalId")
    boolean isPublishableRoadStation(@Param("roadStationNaturalId")
                                     final long roadStationNaturalId,
                                     @Param("roadStationType")
                                     final RoadStationType roadStationType);

    RoadStation findByTypeAndNaturalId(final RoadStationType type, final Long naturalId);

    RoadStation findByTypeAndLotjuId(final RoadStationType tmsStation, final Long id);
}
