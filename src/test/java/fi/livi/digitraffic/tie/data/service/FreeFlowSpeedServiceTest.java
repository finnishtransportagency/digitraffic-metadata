package fi.livi.digitraffic.tie.data.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dto.v1.freeflowspeed.FreeFlowSpeedRootDataObjectDto;
import fi.livi.digitraffic.tie.service.v1.FreeFlowSpeedService;

public class FreeFlowSpeedServiceTest extends AbstractServiceTest {

    @Autowired
    private FreeFlowSpeedService freeFlowSpeedService;

    @Test
    public void testListAllLinkDataFromNonObsoleteStations() {
        final FreeFlowSpeedRootDataObjectDto object = freeFlowSpeedService.listLinksPublicFreeFlowSpeeds(false);

        Assert.assertNotNull(object);
        Assert.assertNotNull(object.getDataUpdatedTime());

        Assert.assertNotNull(object.getTmsFreeFlowSpeeds());
        Assert.assertTrue(object.getTmsFreeFlowSpeeds().size() > 0);
    }
}
