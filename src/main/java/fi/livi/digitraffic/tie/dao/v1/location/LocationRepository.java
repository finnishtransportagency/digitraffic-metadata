package fi.livi.digitraffic.tie.dao.v1.location;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

import java.util.stream.Stream;
import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.location.LocationJson;
import fi.livi.digitraffic.tie.model.v1.location.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    LocationJson findByVersionAndLocationCode(final String version, final int locationCode);

    @QueryHints(@QueryHint(name=HINT_FETCH_SIZE, value="7000"))
    Stream<LocationJson> findAllByVersion(final String version);
}
