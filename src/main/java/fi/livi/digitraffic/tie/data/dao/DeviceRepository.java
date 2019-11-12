package fi.livi.digitraffic.tie.data.dao;

import static org.hibernate.annotations.QueryHints.READ_ONLY;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;

import java.util.stream.Stream;
import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.model.trafficsigns.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    @QueryHints({@QueryHint(name = HINT_CACHEABLE, value = "false"), @QueryHint(name = READ_ONLY, value = "true")})
    @Query("select d from Device d")
    Stream<Device> streamAll();
}
