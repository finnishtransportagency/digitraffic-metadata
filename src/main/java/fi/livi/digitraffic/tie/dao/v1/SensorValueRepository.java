package fi.livi.digitraffic.tie.dao.v1;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.v1.SensorValue;

@Repository
public interface SensorValueRepository extends JpaRepository<SensorValue, Long> {
    @EntityGraph(attributePaths = {"roadStation", "roadStationSensor"})
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<SensorValue> findByRoadStationObsoleteDateIsNullAndRoadStationSensorObsoleteDateIsNullAndRoadStationLotjuIdInAndRoadStationType(final List<Long> tmsLotjuIds, final RoadStationType roadStationType);
}
