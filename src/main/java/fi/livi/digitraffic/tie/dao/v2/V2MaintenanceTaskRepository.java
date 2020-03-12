package fi.livi.digitraffic.tie.dao.v2;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTask;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTaskCategory;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTaskOperation;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTask;

@Repository
public interface V2MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, Long> {

    List<MaintenanceTask> findAllByOrderById();

    @Query(value =
    "select new fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTaskOperation(c.id, c.fi, c.sv, c.en) " +
    "from MaintenanceTaskOperation c order by c.id")
    List<MaintenanceRealizationTaskOperation> findAllOperationsOrderById();

    @Query(value =
    "select new fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTaskCategory(c.id, c.fi, c.sv, c.en) " +
    "from MaintenanceTaskCategory c order by c.id")
    List<MaintenanceRealizationTaskCategory> findAllCategoriesOrderById();


}
