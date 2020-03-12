package fi.livi.digitraffic.tie.dao.v1.workmachine;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v1.maintenance.harja.WorkMachineTracking;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.WorkMachineTrackingDto;

@Repository
public interface WorkMachineTrackingRepository extends JpaRepository<WorkMachineTracking, Long> {

    @Modifying
    @Query(value = "UPDATE WORK_MACHINE_TRACKING\n" +
                   "SET type = record -> 'observationFeatureCollection' -> 'features' -> 0 -> 'geometry' ->> 'type'\n" +
                   "WHERE type IS NULL",
           nativeQuery = true)
    int updateWorkMachineTrackingTypes();

    List<WorkMachineTrackingDto> findByHandledIsNullOrderByCreatedAsc(final Pageable pageable);
    List<WorkMachineTrackingDto> findByHandledIsNullOrderByCreatedAsc();

    default List<WorkMachineTrackingDto> findUnhandeldOldestFirst(final Integer maxResultCount) {
        if (maxResultCount != null && maxResultCount > 0) {
            return findByHandledIsNullOrderByCreatedAsc(PageRequest.of(0, maxResultCount));
        }
        return findByHandledIsNullOrderByCreatedAsc();
    }

    @Modifying
    @Query(value = "UPDATE WORK_MACHINE_TRACKING\n" +
                   "SET handled = clock_timestamp()\n" +
                   "WHERE id = :workMachineTrackingId",
           nativeQuery = true)
    int markHandled(final long workMachineTrackingId);
}
