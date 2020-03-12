package fi.livi.digitraffic.tie.dao.v1.location;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.location.LocationTypeJson;
import fi.livi.digitraffic.tie.model.v1.location.LocationType;

@Repository
public interface LocationTypeRepository extends JpaRepository<LocationType, String> {
    List<LocationTypeJson> findAllByIdVersion(final String version);
}
