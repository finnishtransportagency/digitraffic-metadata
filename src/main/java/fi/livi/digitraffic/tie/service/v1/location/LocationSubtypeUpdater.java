package fi.livi.digitraffic.tie.service.v1.location;

import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.dao.v1.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.model.v1.location.LocationSubtype;

@Service
public class LocationSubtypeUpdater {
    private final LocationSubtypeRepository locationSubtypeRepository;

    public LocationSubtypeUpdater(final LocationSubtypeRepository locationSubtypeRepository) {
        this.locationSubtypeRepository = locationSubtypeRepository;
    }

    public List<LocationSubtype> updateLocationSubtypes(final Path path, final String version) {
        final LocationSubtypeReader locationSubtypeReader = new LocationSubtypeReader(version);
        final List<LocationSubtype> newTypes = locationSubtypeReader.read(path);

        locationSubtypeRepository.saveAll(newTypes);

        return newTypes;
    }
}
