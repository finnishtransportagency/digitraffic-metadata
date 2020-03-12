package fi.livi.digitraffic.tie.dao.v2;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.QueryHint;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealization;

@Repository
public interface V2MaintenanceRealizationRepository extends JpaRepository<MaintenanceRealization, Long> {

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @Query(value =
        "SELECT mr FROM #{#entityName} mr\n" +
        "join mr.tasks task\n" +
        "WHERE mr.sendingTime BETWEEN :from AND :to\n" +
        "  AND intersects(mr.lineString, :area) = true\n" +
        "ORDER by mr.id")
    @EntityGraph(attributePaths = { "tasks" }, type = EntityGraph.EntityGraphType.LOAD)
    List<MaintenanceRealization> findByAgeAndBoundingBox(final ZonedDateTime from, final ZonedDateTime to, final Geometry area);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @Query(value =
        "SELECT mr FROM #{#entityName} mr\n" +
        "join mr.tasks task\n" +
        "WHERE mr.sendingTime BETWEEN :from AND :to\n" +
        "  AND intersects(mr.lineString, :area) = true\n" +
        "  AND task.id in (:taskIds)" +
        "ORDER by mr.id")
    @EntityGraph(attributePaths = { "tasks" }, type = EntityGraph.EntityGraphType.LOAD)
    List<MaintenanceRealization> findByAgeAndBoundingBoxAndTaskIds(final ZonedDateTime from, final ZonedDateTime to, final Geometry area, final List<Long> taskIds);

}
