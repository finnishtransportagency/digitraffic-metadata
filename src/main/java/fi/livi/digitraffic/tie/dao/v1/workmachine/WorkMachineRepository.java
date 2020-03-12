package fi.livi.digitraffic.tie.dao.v1.workmachine;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.livi.digitraffic.tie.model.v1.maintenance.WorkMachine;

public interface WorkMachineRepository extends JpaRepository<WorkMachine, Long> {

    WorkMachine findByHarjaIdAndHarjaUrakkaId(final long HarjaId, final long harjaUrakkaId);

}
