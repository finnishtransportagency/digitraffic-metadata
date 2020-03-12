package fi.livi.digitraffic.tie.dao.v1;

import org.springframework.data.jpa.repository.JpaRepository;

import fi.livi.digitraffic.tie.model.v1.SensorValueDescription;
import fi.livi.digitraffic.tie.model.v1.SensorValueDescriptionPK;

public interface SensorValueDescriptionRepository extends JpaRepository<SensorValueDescription, SensorValueDescriptionPK> {

}
