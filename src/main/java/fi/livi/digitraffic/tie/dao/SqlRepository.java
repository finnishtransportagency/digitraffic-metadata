package fi.livi.digitraffic.tie.dao;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface SqlRepository extends Repository<SqlRepository.DummyEntity, Long> {
    @Entity
    class DummyEntity {
        @Id
        public long id;
    }
}
