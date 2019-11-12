package fi.livi.digitraffic.tie.metadata.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.dto.FlywayVersion;

@Repository
public interface FlywayRepository extends SqlRepository {

    @Query(value =
            "SELECT version, installed_on AS installedOn, success \n" +
            "FROM flyway_schema_history \n" +
            "ORDER BY flyway_schema_history.installed_rank DESC \n" +
            "LIMIT 1",
           nativeQuery = true)
    FlywayVersion findLatest();
}
