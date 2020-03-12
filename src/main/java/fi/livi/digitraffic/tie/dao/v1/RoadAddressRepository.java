package fi.livi.digitraffic.tie.dao.v1;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v1.RoadAddress;

@Repository
public interface RoadAddressRepository extends JpaRepository<RoadAddress, Long>{
}
