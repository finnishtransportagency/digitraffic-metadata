package fi.livi.digitraffic.tie.metadata.service.location;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.base.AbstractTestBase;

public class LocationServiceTest extends AbstractTestBase {
    @Autowired
    private LocationService locationService;

    @Test
    public void testListAllLocationTypes() {
        Assert.assertThat(locationService.listAllLocationTypes(), IsCollectionWithSize.hasSize(7));
    }

    @Test
    public void testListAllLocationSubtypes() {
        Assert.assertThat(locationService.listAllLocationSubTypes(), IsCollectionWithSize.hasSize(7));
    }

    @Test
    public void testListAllLocations() {
        Assert.assertThat(locationService.listAllLocations(), IsCollectionWithSize.hasSize(4));
    }

    @Test
    public void testFindLocation() {
        Assert.assertNotNull(locationService.findLocation(12316));
    }
}
