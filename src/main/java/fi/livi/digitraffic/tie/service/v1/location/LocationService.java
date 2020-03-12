package fi.livi.digitraffic.tie.service.v1.location;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.dao.v1.location.LocationRepository;
import fi.livi.digitraffic.tie.dao.v1.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.dao.v1.location.LocationTypeRepository;
import fi.livi.digitraffic.tie.dao.v1.location.LocationVersionRepository;
import fi.livi.digitraffic.tie.dto.v1.location.LocationFeature;
import fi.livi.digitraffic.tie.dto.v1.location.LocationFeatureCollection;
import fi.livi.digitraffic.tie.dto.v1.location.LocationJson;
import fi.livi.digitraffic.tie.dto.v1.location.LocationTypesMetadata;
import fi.livi.digitraffic.tie.model.v1.location.LocationVersion;

@Service
public class LocationService {
    private final LocationTypeRepository locationTypeRepository;
    private final LocationSubtypeRepository locationSubtypeRepository;
    private final LocationRepository locationRepository;
    private final LocationVersionRepository locationVersionRepository;

    public static final String LATEST="latest";

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    public LocationService(final LocationTypeRepository locationTypeRepository,
                           final LocationSubtypeRepository locationSubtypeRepository,
                           final LocationRepository locationRepository,
                           final LocationVersionRepository locationVersionRepository) {
        this.locationTypeRepository = locationTypeRepository;
        this.locationSubtypeRepository = locationSubtypeRepository;
        this.locationRepository = locationRepository;
        this.locationVersionRepository = locationVersionRepository;
    }

    @PerformanceMonitor(maxInfoExcecutionTime = 100000, maxWarnExcecutionTime = 3000)
    @Transactional(readOnly = true)
    public LocationFeatureCollection findLocationsMetadata(final boolean onlyUpdateInfo, final String version) {
        final LocationVersion locationVersion = getLocationVersion(version);
        final String lVersion = locationVersion.getVersion();

        if(onlyUpdateInfo) {
            return new LocationFeatureCollection(locationVersion.getUpdated(), lVersion);
        }

        final List<LocationFeature> features =
            locationRepository.findAllByVersion(lVersion).parallel().map(LocationFeature::new).collect(Collectors.toList());

        return new LocationFeatureCollection(locationVersion.getUpdated(), lVersion, features);
    }

    private static boolean isLatestVersion(final String version) {
        return StringUtils.isEmpty(version) || version.equalsIgnoreCase(LATEST);
    }

    private LocationVersion getLocationVersion(final String version) {
        final LocationVersion locationVersion = isLatestVersion(version) ?
                                                locationVersionRepository.findLatestVersion() :
                                                locationVersionRepository.findById(version).orElse(null);

        if(locationVersion == null) {
            throw new ObjectNotFoundException(LocationVersion.class, version);
        }

        return locationVersion;
    }

    @Transactional(readOnly = true)
    public LocationFeatureCollection findLocation(final int id, final String version) {
        final LocationVersion locationVersion = getLocationVersion(version);
        final String lVersion = locationVersion.getVersion();

        final LocationJson location = locationRepository.findByVersionAndLocationCode(lVersion, id);

        if(location == null) {
            throw new ObjectNotFoundException("Location", id);
        }

        return new LocationFeatureCollection(locationVersion.getUpdated(), lVersion,
                Collections.singletonList(new LocationFeature(location)));
    }

    @Transactional(readOnly = true)
    public LocationTypesMetadata findLocationSubtypes(final boolean lastUpdated, final String version) {
        final LocationVersion locationVersion = getLocationVersion(version);
        final String lVersion = locationVersion.getVersion();

        if(lastUpdated) {
            return new LocationTypesMetadata(locationVersion.getUpdated(), lVersion);
        }

        return new LocationTypesMetadata(locationVersion.getUpdated(), lVersion,
                locationTypeRepository.findAllByIdVersion(lVersion),
                locationSubtypeRepository.findAllByIdVersion(lVersion));
    }

    @Transactional(readOnly = true)
    public List<LocationVersion> findLocationVersions() {
        return locationVersionRepository.findAll();
    }
}
