package fi.livi.digitraffic.tie.dao.v2;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealization;

@Repository
public interface V2MaintenanceRealizationRepository extends JpaRepository<MaintenanceRealization, Long> {

    @Query(value =
               "Select mr.*\n" +
               "from maintenance_realization mr\n" +
               "where mr.sending_time between :from and :to\n" +
               "  and mr.line_string && ST_MakeEnvelope(:xMin, :yMin, :xMax, :yMax)", // Bounding box
           nativeQuery = true)
    List<MaintenanceRealization> findByAgeAndBoundingBox(final Instant from, final Instant to, double xMin, double yMin, double xMax, double yMax);
}