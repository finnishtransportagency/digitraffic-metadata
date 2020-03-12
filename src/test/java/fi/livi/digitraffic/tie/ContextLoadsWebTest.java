package fi.livi.digitraffic.tie;

import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = { "spring.localstack.enabled=false" })
public class ContextLoadsWebTest extends AbstractSpringJUnitTest {
    @Test
    public void contextLoads() {
    }

}
