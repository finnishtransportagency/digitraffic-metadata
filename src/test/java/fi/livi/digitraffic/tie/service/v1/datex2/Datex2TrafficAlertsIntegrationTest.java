package fi.livi.digitraffic.tie.service.v1.datex2;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.conf.RestTemplateConfiguration;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;

@TestPropertySource(properties = "datex2.traffic.alerts.url=https://ava.liikennevirasto.fi/incidents/datex2/")
@Import({ Datex2SimpleMessageUpdater.class, Datex2WeightRestrictionsHttpClient.class, Datex2RoadworksHttpClient.class, RestTemplateConfiguration.class })
public class Datex2TrafficAlertsIntegrationTest extends AbstractServiceTest {

    @Autowired
    private Datex2SimpleMessageUpdater datex2SimpleMessageUpdater;

    @Autowired
    private Datex2Repository datex2Repository;
    @Test
    @Rollback(false)
    @Ignore("For manual integration testing")
    public void updateTrafficAlertMessages() {
        // Uncomment if clean up first
//        datex2Repository.deleteAll();

        while (datex2SimpleMessageUpdater.updateDatex2TrafficAlertMessages() > 0) {
            if (TestTransaction.isActive()) {
                TestTransaction.flagForCommit();
                TestTransaction.end();
            }
            TestTransaction.start();
        }
    }
}
