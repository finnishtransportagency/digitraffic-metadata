package fi.livi.digitraffic.tie.dao.v1.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v1.location.LocationVersion;

@Repository
public interface LocationVersionRepository extends JpaRepository<LocationVersion, String> {
    @Query(value = "select version,updated from (select version, updated, first_value(version) over (order by updated desc) m "
            + "from location_version) mm "
            + "where version = m", nativeQuery = true)
    LocationVersion findLatestVersion();
}
