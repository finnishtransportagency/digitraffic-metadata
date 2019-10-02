package fi.livi.digitraffic.tie.data.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Point;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsDataDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsRecord;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsTableDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsTablePublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsUnitRecord;

public class VariableSignDatex2ServiceTest extends AbstractRestWebTest {
    @Autowired
    private VariableSignDatex2Service variableSignDatex2Service;

    private static final String DEVICE_ID  = "ID1";
    private static final String ROAD_NUMBER = "21";

    private void insertMetadata() {
        this.entityManager.createNativeQuery(
            "insert into device(id,updated_date,type,road_address,etrs_tm35fin_x,etrs_tm35fin_y,direction,carriageway)\n" +
                String.format("values('%s',current_timestamp, 'NOPEUSRAJOITUS', '%s 2 3',10, 20,'KASVAVA', 'NORMAALI');",
                    DEVICE_ID, ROAD_NUMBER)
        ).executeUpdate();
    }

    private VmsTablePublication getMetadata() {
        final VmsTableDatex2Response metadata = variableSignDatex2Service.findVariableSignMetadata();
        final VmsTablePublication publication = (VmsTablePublication)metadata.getD2LogicalModel().getPayloadPublication();

        Assert.assertNotNull("Payload should not be null", publication);
        AssertHelper.assertCollectionSize(1, publication.getVmsUnitTable());

        return publication;
    }

    private VmsPublication getData() {
        final VmsDataDatex2Response metadata = variableSignDatex2Service.findVariableSignData();
        final VmsPublication publication = (VmsPublication)metadata.getD2LogicalModel().getPayloadPublication();

        Assert.assertNotNull("Payload should not be null", publication);

        return publication;
    }

    @Test
    public void emptyMetadata() {
        final VmsTablePublication metadata = getMetadata();

        AssertHelper.assertEmpty(metadata.getVmsUnitTable().get(0).getVmsUnitRecord());
    }

    @Test
    public void emptyData() {
        final VmsPublication data = getData();

        AssertHelper.assertEmpty(data.getVmsUnit());
    }

    @Test
    public void onlyMetadata() {
        insertMetadata();

        final VmsTablePublication metadata = getMetadata();

        AssertHelper.assertCollectionSize(1, metadata.getVmsUnitTable().get(0).getVmsUnitRecord());

        final VmsUnitRecord r = metadata.getVmsUnitTable().get(0).getVmsUnitRecord().get(0);
        final VmsRecord vmsRecord = r.getVmsRecord().get(0).getVmsRecord();

        Assert.assertEquals(DEVICE_ID, r.getId());

        final Point point = (Point) vmsRecord.getVmsLocation();

        Assert.assertEquals(ROAD_NUMBER, point.getPointAlongLinearElement().getLinearElement().getRoadNumber());
    }
}
