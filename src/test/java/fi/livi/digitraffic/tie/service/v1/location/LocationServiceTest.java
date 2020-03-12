package fi.livi.digitraffic.tie.service.v1.location;

import static fi.livi.digitraffic.tie.service.v1.location.LocationService.LATEST;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.dto.v1.location.LocationFeatureCollection;
import fi.livi.digitraffic.tie.dto.v1.location.LocationTypesMetadata;
import fi.livi.digitraffic.tie.model.v1.location.LocationVersion;

public class LocationServiceTest extends AbstractServiceTest {
    @Autowired
    private LocationService locationService;

    private static final String VERSION0 = "1.0";
    private static final String VERSION1 = "1.1";

    @Test
    public void findLocationOkLatest() {
        assertNotNull(locationService.findLocation(12187, LATEST));
    }

    @Test
    public void findLocationOkEmpty() {
        assertNotNull(locationService.findLocation(12187, ""));
    }

    @Test
    public void findLocationOkVersion1() {
        assertNotNull(locationService.findLocation(1, VERSION1));
    }

    @Test
    public void findLocationOkVersion2() {
        assertNotNull(locationService.findLocation(1, VERSION0));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void findLocationNotFound() {
        assertNotNull(locationService.findLocation(-222, LATEST));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void findLocationNotFoundFromOtherVersion() {
        assertNotNull(locationService.findLocation(12187, VERSION0));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void findLocationVersionNotFound() {
        assertNotNull(locationService.findLocation(12187, "not_found"));
    }

    @Test
    public void findLocationSubtypesFromLatest() {
        final LocationTypesMetadata metadata = locationService.findLocationSubtypes(false, LATEST);
        assertNotNull(metadata);
        assertNotNull(metadata.typesUpdated);
        assertNotNull(metadata.typesVersion);
        assertThat(metadata.locationSubtypes, hasSize(7));
        assertThat(metadata.locationTypes, hasSize(7));
    }

    @Test
    public void findLocationSubtypesFromVersion2() {
        final LocationTypesMetadata metadata = locationService.findLocationSubtypes(false, VERSION0);
        assertNotNull(metadata);
        assertNotNull(metadata.typesUpdated);
        assertNotNull(metadata.typesVersion);
        assertThat(metadata.locationSubtypes, hasSize(1));
        assertThat(metadata.locationTypes, hasSize(1));
    }

    @Test
    public void findLocationSubtypesLastUpdated() {
        final LocationTypesMetadata metadata = locationService.findLocationSubtypes(true, LATEST);
        assertNotNull(metadata);
        assertNotNull(metadata.typesUpdated);
        assertNotNull(metadata.typesVersion);
        assertNull(metadata.locationSubtypes);
        assertNull(metadata.locationTypes);
    }

    @Test
    public void findLocations() {
        final LocationFeatureCollection metadata = locationService.findLocationsMetadata(false, LATEST);
        assertNotNull(metadata);
        assertNotNull(metadata.locationsUpdateTime);
        assertNotNull(metadata.locationsVersion);
        assertThat(metadata.features, not(empty()));
    }

    @Test
    public void findLocationsVersion2() {
        final LocationFeatureCollection metadata = locationService.findLocationsMetadata(false, VERSION0);
        assertNotNull(metadata);
        assertNotNull(metadata.locationsUpdateTime);
        assertNotNull(metadata.locationsVersion);
        assertThat(metadata.features, hasSize(1));
    }

    @Test
    public void findLocationsMetadataLastUpdated() {
        final LocationFeatureCollection metadata = locationService.findLocationsMetadata(true, LATEST);
        assertNotNull(metadata);
        assertNotNull(metadata.locationsUpdateTime);
        assertNotNull(metadata.locationsVersion);
        assertNull(metadata.features);
    }

    @Test
    public void findLocationVersions() {
        final List<LocationVersion> locationVersions = locationService.findLocationVersions();
        assertNotNull(locationVersions);
        assertThat(locationVersions, hasSize(2));
    }
}
