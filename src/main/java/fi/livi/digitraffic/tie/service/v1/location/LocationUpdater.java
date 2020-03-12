package fi.livi.digitraffic.tie.service.v1.location;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.model.v1.location.Location;
import fi.livi.digitraffic.tie.model.v1.location.LocationSubtype;

@Service
public class LocationUpdater {
    private final EntityManager entityManager;

    private final int batchSize;

    public LocationUpdater(final EntityManager entityManager,
                           @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}") int batchSize) {
        this.entityManager = entityManager;
        this.batchSize = batchSize;
    }

    public List<Location> updateLocations(final Path path, final List<LocationSubtype> locationSubtypes, final String version) {
        final List<Location> newLocations = getLocations(path, locationSubtypes, version);

        for(int i= 0;i < newLocations.size();i++) {
            entityManager.persist(newLocations.get(i));

            if(i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        return newLocations;
    }

    public List<Location> getLocations(final Path path, final List<LocationSubtype> locationSubtypes, final String version) {
        final Map<String, LocationSubtype> subtypeMap = locationSubtypes.stream().collect(Collectors.toMap(LocationSubtype::getSubtypeCode, Function.identity()));

        final LocationReader reader = new LocationReader(subtypeMap, version);
        final List<Location> locations = reader.read(path);

        setReferences(locations, reader.getAreaRefMap(), reader.getLinearRefMap());

        return locations;
    }

    private void setReferences(final List<Location> newLocations, final Map<Integer, Integer> areaRefMap, final Map<Integer, Integer> linearRefMap) {
        final Map<Integer, Location> newMap = newLocations.parallelStream().collect(Collectors.toMap(Location::getLocationCode, Function.identity()));

        areaRefMap.forEach((id, areaRefId) -> {
            final Location location = newMap.get(id);
            final Location areaRef = newMap.get(areaRefId);

            if(location == null) {
                throw new IllegalArgumentException();
            }

            if(areaRef == null) {
                throw new IllegalArgumentException("could not find area reference " + areaRefId);
            }

            location.setAreaRef(areaRefId);
        });

        linearRefMap.forEach((id, linearRefId) -> {
            final Location location = newMap.get(id);
            final Location linearRef = newMap.get(linearRefId);

            if(location == null) {
                throw new IllegalArgumentException();
            }

            if(linearRef == null) {
                throw new IllegalArgumentException("could not find linear reference " + linearRefId);
            }

            location.setLinearRef(linearRefId);
        });
    }
}
