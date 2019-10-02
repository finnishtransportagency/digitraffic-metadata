package fi.livi.digitraffic.tie.data.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsDataDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsTableDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsTablePublication;

public class VariableSignDatex2ServiceTest extends AbstractServiceTest {
    @Autowired
    private VariableSignDatex2Service variableSignDatex2Service;

    private VmsTablePublication getMetadata() {
        final VmsTableDatex2Response metadata = variableSignDatex2Service.findVariableSignMetadata();
        final VmsTablePublication publication = (VmsTablePublication)metadata.getD2LogicalModel().getPayloadPublication();

        Assert.notNull(publication, "Payload should not be null");

        return publication;
    }

    private VmsPublication getData() {
        final VmsDataDatex2Response metadata = variableSignDatex2Service.findVariableSignData();
        final VmsPublication publication = (VmsPublication)metadata.getD2LogicalModel().getPayloadPublication();

        Assert.notNull(publication, "Payload should not be null");

        return publication;
    }

    @Test
    public void emptyMetadata() {
        final VmsTablePublication metadata = getMetadata();

        AssertHelper.assertEmpty(metadata.getVmsUnitTable());
    }

    @Test
    public void emptyData() {
        final VmsPublication data = getData();

        AssertHelper.assertEmpty(data.getVmsUnit());
    }
}
