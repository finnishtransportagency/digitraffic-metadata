package fi.livi.digitraffic.tie;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "app.type=daemon",
    "spring.main.web-application-type=none",
    "road.datasource.hikari.maximum-pool-size=2"
})
public abstract class AbstractDaemonTest extends AbstractSpringJUnitTest {

}
