package fi.livi.digitraffic.tie;

import javax.transaction.Transactional;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RoadApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "config.test=true", "logging.level.org.springframework.test.context.transaction.TransactionContext=WARN" })
@Transactional
public abstract class AbstractSpringJUnitTest extends AbstractTest {

}
