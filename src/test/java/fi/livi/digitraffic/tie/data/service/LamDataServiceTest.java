package fi.livi.digitraffic.tie.data.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.data.dto.lam.LamRootDataObjectDto;

public class LamDataServiceTest extends MetadataTest {

    @Autowired
    private LamDataService lamDataService;

    @Test
    public void testListAllLamDataFromNonObsoleteStations()  {
        final LamRootDataObjectDto object = lamDataService.listAllLamDataFromNonObsoleteStations();

        Assert.notNull(object);
        Assert.notNull(object.getDataLocalTime());
        Assert.notNull(object.getDataUtc());
        Assert.notNull(object.getLamMeasurements());
        Assert.notEmpty(object.getLamMeasurements());
    }
}
