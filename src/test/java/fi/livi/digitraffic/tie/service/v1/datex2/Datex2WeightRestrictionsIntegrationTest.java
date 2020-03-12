package fi.livi.digitraffic.tie.service.v1.datex2;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;

public class Datex2WeightRestrictionsIntegrationTest extends AbstractDaemonTestWithoutS3 {
    @Autowired
    private Datex2SimpleMessageUpdater messageUpdater;

    @Autowired
    private Datex2Repository datex2Repository;

    @SpyBean
    private Datex2WeightRestrictionsHttpClient datex2WeightRestrictionsHttpClient;

    @Test
    @Rollback(false)
    @Ignore("For manual integration testing")
    public void updateMessagesWithRealData() {
        //assertEmpty(datex2Repository.findAllActive(Datex2MessageType.ROADWORK.name()));

        messageUpdater.updateDatex2WeightRestrictionMessages();

        Assert.assertTrue(datex2Repository.findAllActive(Datex2MessageType.WEIGHT_RESTRICTION.name(), 0).size() > 0);
    }

}
