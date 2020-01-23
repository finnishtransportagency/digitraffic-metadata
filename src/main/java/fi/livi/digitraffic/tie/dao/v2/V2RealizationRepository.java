package fi.livi.digitraffic.tie.dao.v2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealization;

@Repository
public interface V2RealizationRepository extends JpaRepository<MaintenanceRealization, Long> {

}