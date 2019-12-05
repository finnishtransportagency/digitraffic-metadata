package fi.livi.digitraffic.tie.dao.v1;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.livi.digitraffic.tie.data.model.maintenance.WorkMachine;

public interface WorkMachineRepository extends JpaRepository<WorkMachine, Long> {

    WorkMachine findByHarjaIdAndHarjaUrakkaId(final long HarjaId, final long harjaUrakkaId);

}
