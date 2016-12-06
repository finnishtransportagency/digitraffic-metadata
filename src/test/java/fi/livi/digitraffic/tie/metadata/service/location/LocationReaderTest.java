package fi.livi.digitraffic.tie.metadata.service.location;

import static org.mockito.Matchers.anyString;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.base.AbstractTestBase;
import fi.livi.digitraffic.tie.metadata.dao.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.metadata.model.location.Location;
import fi.livi.digitraffic.tie.metadata.model.location.LocationSubtype;

public class LocationReaderTest extends AbstractTestBase {
    @Autowired
    private LocationSubtypeRepository locationSubtypeRepository;

    private Map<String, LocationSubtype> subtypeMap;

    @Before
    public void setUp() {
        final List<LocationSubtype> locationSubtypes = locationSubtypeRepository.findAll();

        subtypeMap = locationSubtypes.stream().collect(Collectors.toMap(LocationSubtype::getSubtypeCode, Function.identity()));
    }

    @Test
    public void emptyLocationsFile() {
        final LocationReader reader = new LocationReader(subtypeMap);

        final List<Location> locations = reader.read(getPath("/locations/locations_empty.csv"));
        Assert.assertThat(locations, Matchers.empty());
    }

    @Test
    public void illegalGeocode() {
        final LocationReader reader = new LocationReader(subtypeMap);
        final LocationReader spyReader = Mockito.spy(reader);

        final List<Location> locations = spyReader.read(getPath("/locations/locations_illegal_geocode.csv"));
        Assert.assertThat(locations, Matchers.hasSize(2));
        Assert.assertThat(locations.get(0).getGeocode(), Matchers.equalTo("test"));
        Assert.assertThat(locations.get(1).getGeocode(), Matchers.isEmptyOrNullString());

        Mockito.verify(spyReader).log.error(anyString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalSubtype() {
        final LocationReader reader = new LocationReader(subtypeMap);

        reader.read(getPath("/locations/locations_illegal_subtype.csv"));
    }
}