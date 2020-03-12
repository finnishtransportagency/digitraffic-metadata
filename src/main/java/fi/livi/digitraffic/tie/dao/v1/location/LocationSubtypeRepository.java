package fi.livi.digitraffic.tie.dao.v1.location;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.location.LocationSubtypeJson;
import fi.livi.digitraffic.tie.model.v1.location.LocationSubtype;

@Repository
public interface LocationSubtypeRepository extends JpaRepository<LocationSubtype, String> {
    List<LocationSubtypeJson> findAllByIdVersion(final String version);
}
