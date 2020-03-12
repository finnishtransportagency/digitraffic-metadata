package fi.livi.digitraffic.tie;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "spring.localstack.enabled=false" })
public abstract class AbstractDaemonTestWithoutS3 extends AbstractDaemonTest {

    @Autowired
    protected EntityManager entityManager;
}
