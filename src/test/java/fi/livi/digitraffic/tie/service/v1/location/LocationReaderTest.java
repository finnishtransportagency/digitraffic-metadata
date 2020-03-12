package fi.livi.digitraffic.tie.service.v1.location;

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

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.dao.v1.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.model.v1.location.Location;
import fi.livi.digitraffic.tie.model.v1.location.LocationSubtype;

public class LocationReaderTest extends AbstractJpaTest {
    @Autowired
    private LocationSubtypeRepository locationSubtypeRepository;

    private Map<String, LocationSubtype> subtypeMap;

    private final static String VERSION = "VERSION";

    @Before
    public void setUp() {
        final List<LocationSubtype> locationSubtypes = locationSubtypeRepository.findAll();

        subtypeMap = locationSubtypes.stream()
                .filter(s -> s.getId().getVersion().equals("1.1"))
                .collect(Collectors.toMap(LocationSubtype::getSubtypeCode, Function.identity()));
    }

    @Test
    public void emptyLocationsFile() {
        final LocationReader reader = new LocationReader(subtypeMap, VERSION);

        final List<Location> locations = reader.read(getPath("/locations/locations_empty.csv"));
        Assert.assertThat(locations, Matchers.empty());
    }

    @Test
    public void illegalGeocode() {
        final LocationReader reader = new LocationReader(subtypeMap, VERSION);
        final LocationReader spyReader = Mockito.spy(reader);

        final List<Location> locations = spyReader.read(getPath("/locations/locations_illegal_geocode.csv"));
        Assert.assertThat(locations, Matchers.hasSize(2));
        Assert.assertThat(locations.get(0).getGeocode(), Matchers.equalTo("test"));
        Assert.assertThat(locations.get(1).getGeocode(), Matchers.isEmptyOrNullString());

        //Mockito.verify(spyReader).log.error(anyString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalSubtype() {
        final LocationReader reader = new LocationReader(subtypeMap, VERSION);

        reader.read(getPath("/locations/locations_illegal_subtype.csv"));
    }
}
