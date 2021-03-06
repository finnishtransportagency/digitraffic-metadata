package fi.livi.digitraffic.tie.dao.v2;

import fi.livi.digitraffic.tie.model.v2.trafficsigns.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.stream.Stream;

@Repository
public interface V2DeviceRepository extends JpaRepository<Device, String> {
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    @Query("select d from Device d")
    Stream<Device> streamAll();
}
