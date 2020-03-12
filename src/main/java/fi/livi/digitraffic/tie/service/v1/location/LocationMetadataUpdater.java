package fi.livi.digitraffic.tie.service.v1.location;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.location.LocationVersionRepository;
import fi.livi.digitraffic.tie.model.v1.location.LocationSubtype;
import fi.livi.digitraffic.tie.model.v1.location.LocationVersion;

@Service
public class LocationMetadataUpdater {
    private final LocationUpdater locationUpdater;
    private final LocationTypeUpdater locationTypeUpdater;
    private final LocationSubtypeUpdater locationSubtypeUpdater;

    private final LocationVersionRepository locationVersionRepository;

    private final MetadataFileFetcher metadataFileFetcher;

    private static final Logger log = LoggerFactory.getLogger(LocationMetadataUpdater.class);

    public LocationMetadataUpdater(final LocationUpdater locationUpdater,
                                   final LocationTypeUpdater locationTypeUpdater,
                                   final LocationSubtypeUpdater locationSubtypeUpdater,
                                   final LocationVersionRepository locationVersionRepository,
                                   final MetadataFileFetcher metadataFileFetcher) {
        this.locationUpdater = locationUpdater;
        this.locationTypeUpdater = locationTypeUpdater;
        this.locationSubtypeUpdater = locationSubtypeUpdater;
        this.locationVersionRepository = locationVersionRepository;
        this.metadataFileFetcher = metadataFileFetcher;
    }

    @Transactional
    public void findAndUpdate() throws IOException {
        try {
            final MetadataVersions latestVersions = metadataFileFetcher.getLatestVersions();
            final LocationVersion currentVersion = locationVersionRepository.findLatestVersion();

            if(areVersionsEmpty(latestVersions)) {
                log.error("empty metadataversions:" + latestVersions);
            } else if(!areVersionsSame(latestVersions)) {
                log.info("Different versions, locations {} and types {}", latestVersions.getLocationsVersion().version, latestVersions.getLocationTypeVersion().version);
            } else if(isUpdateNeeded(latestVersions, currentVersion)) {
                final MetadataPathCollection paths = metadataFileFetcher.getFilePaths(latestVersions);
                final StopWatch stopWatch = StopWatch.createStarted();

                updateAll(paths.typesPath, paths.subtypesPath, paths.locationsPath, latestVersions);
                removeTempFiles(paths);
                stopWatch.stop();

                log.info("Locations and locationtypes updated, tookMs={}", stopWatch.getTime());
            } else {
                log.info("No need to update locations or locationtypes");
            }
        } catch(final Exception e) {
            log.error("exception when fetching locations metadata", e);

            throw e;
        }
    }

    private boolean areVersionsEmpty(final MetadataVersions latestVersions) {
        return latestVersions == null || latestVersions.getLocationsVersion() == null || latestVersions.getLocationTypeVersion() == null;
    }

    private boolean areVersionsSame(final MetadataVersions latestVersions) {
        return StringUtils.equals(latestVersions.getLocationsVersion().version, latestVersions.getLocationTypeVersion().version);
    }

    private void removeTempFiles(final MetadataPathCollection paths) {
        FileUtils.deleteQuietly(paths.locationsPath.toFile());
        FileUtils.deleteQuietly(paths.typesPath.toFile());
        FileUtils.deleteQuietly(paths.subtypesPath.toFile());
    }

    private boolean isUpdateNeeded(final MetadataVersions latestVersions, final LocationVersion currentVersion) {
        return currentVersion == null ||
               needUpdate(latestVersions.getLocationsVersion(), currentVersion.getVersion()) ||
               needUpdate(latestVersions.getLocationTypeVersion(), currentVersion.getVersion());
    }

    private static boolean needUpdate(final MetadataVersions.MetadataVersion newVersion, final String currentVersion) {
        if(!StringUtils.equals(newVersion.version, currentVersion)) {
            log.info("Versions differ, oldVersion={}, newVersion={}, must be updated", currentVersion, newVersion.version);

            return true;
        }
        return false;
    }

    private void updateAll(final Path locationTypePath, final Path locationSubtypePath, final Path locationPath,
                          final MetadataVersions latestVersions) {
        final String version = latestVersions.getLocationsVersion().version;

        locationTypeUpdater.updateLocationTypes(locationTypePath, version);
        final List<LocationSubtype> locationSubtypes = locationSubtypeUpdater.updateLocationSubtypes(locationSubtypePath, version);
        locationUpdater.updateLocations(locationPath, locationSubtypes, version);

        locationVersionRepository.save(new LocationVersion(version));
    }
}
