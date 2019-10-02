package fi.livi.digitraffic.tie.data.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Point;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Vms;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsDataDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.VmsMessage;
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
    private static final String DATA_SPEED = "80";

    private void assertText(final String expected, final VmsMessage vm) {
        Assert.assertEquals(expected, vm.getTextPage().get(0).getVmsText().getVmsTextLine().get(0).getVmsTextLine().getVmsTextLine());
    }

    private void assertSpeedLimit(final String expected, final VmsMessage vm) {
        Assert.assertEquals(expected+".0", // it's a float, so add .0
            vm.getVmsPictogramDisplayArea().get(0).getVmsPictogramDisplayArea().getVmsPictogram().get(0).getVmsPictogram().getSpeedAttribute().toString());
    }

    private void insertMetadata() {
        this.entityManager.createNativeQuery(
            "insert into device(id,updated_date,type,road_address,etrs_tm35fin_x,etrs_tm35fin_y,direction,carriageway)\n" +
                String.format("values('%s',current_timestamp, 'NOPEUSRAJOITUS', '%s 2 3',10, 20,'KASVAVA', 'NORMAALI')",
                    DEVICE_ID, ROAD_NUMBER)
        ).executeUpdate();
    }

    private void insertData() {
        this.entityManager.createNativeQuery(
            "insert into device_data(created_date,device_id,display_value,additional_information,effect_date,cause,reliability)\n" +
                String.format("values(current_timestamp,'%s','%s',null,current_date,null,'NORMAALI')",
                    DEVICE_ID, DATA_SPEED)
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

    @Test
    public void onlyData() {
        insertData();

        final VmsPublication data = getData();

        AssertHelper.assertCollectionSize(1, data.getVmsUnit());
        AssertHelper.assertCollectionSize(1, data.getVmsUnit().get(0).getVms());

        final Vms vms = data.getVmsUnit().get(0).getVms().get(0).getVms();
        Assert.assertTrue(vms.isVmsWorking());

        final VmsMessage vmsMessage = vms.getVmsMessage().get(0).getVmsMessage();
        // no device found, so speed is in text
        assertText(DATA_SPEED, vmsMessage);
    }

    @Test
    public void dataAndMetadata() {
        insertMetadata();
        insertData();

        final VmsPublication data = getData();

        AssertHelper.assertCollectionSize(1, data.getVmsUnit());
        AssertHelper.assertCollectionSize(1, data.getVmsUnit().get(0).getVms());

        final Vms vms = data.getVmsUnit().get(0).getVms().get(0).getVms();
        Assert.assertTrue(vms.isVmsWorking());

        final VmsMessage vmsMessage = vms.getVmsMessage().get(0).getVmsMessage();
        // device found, says it's speed limit
        AssertHelper.assertEmpty(vmsMessage.getTextPage());
        assertSpeedLimit(DATA_SPEED, vmsMessage);
    }
}
