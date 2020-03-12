package fi.livi.digitraffic.tie.dao.v1.workmachine;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import fi.livi.digitraffic.tie.model.v1.maintenance.WorkMachineObservation;

public interface WorkMachineObservationRepository extends JpaRepository<WorkMachineObservation, Long> {

    List<WorkMachineObservation> findByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdOrderByUpdatedAscIdAsc(final long workMachineHarjaId, final long contractHarjaId);

    WorkMachineObservation findFirstByWorkMachine_HarjaIdAndWorkMachine_HarjaUrakkaIdOrderByUpdatedDescIdDesc(final long workMachineHarjaId, final long contractHarjaId);

    @Modifying
    @Query(value =
               "insert into work_machine_task (work_machine_coordinate_observation_id, work_machine_coordinate_order_number, task)\n" +
               "values (:observationId, :orderNumber, :task)",
           nativeQuery = true)
    void addCoordinateTaskToLastCoordinate(final long observationId, final int orderNumber, final String task);

    @Modifying
    @Query(value = "delete from work_machine_observation",
           nativeQuery = true)
    void deleteAll();
}
