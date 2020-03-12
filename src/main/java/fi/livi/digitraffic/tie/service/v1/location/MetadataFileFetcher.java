package fi.livi.digitraffic.tie.service.v1.location;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MetadataFileFetcher {
    private final String tmcUrl;

    private static final String LATEST_FILENAME = "latest.txt";
    private static final String LOCATIONS_FILENAME = "locations.csv";
    private static final String LOCATION_TYPES_FILENAME = "TYPES.DAT";
    private static final String LOCATION_SUBTYPES_FILENAME = "SUBTYPES.DAT";

    private static final Logger log = LoggerFactory.getLogger(MetadataFileFetcher.class);

    public MetadataFileFetcher(@Value("${metadata.tmc.url}") final String tmcUrl) {
        this.tmcUrl = tmcUrl;
    }

    public MetadataPathCollection getFilePaths(final MetadataVersions latestVersions) throws IOException {
        final Path locationsPath = getLocationsFile(latestVersions.getLocationsVersion());
        final Pair<Path, Path> pathPair = getTypefiles(latestVersions.getLocationTypeVersion());

        return new MetadataPathCollection(locationsPath, pathPair.getRight(), pathPair.getLeft());
    }

    public MetadataVersions getLatestVersions() throws MalformedURLException {
        final URL url = getLatestUrl();
        final LatestReader reader = new LatestReader();

        log.info("reading latest from url={}", url);

        try {
            reader.read(url);
        } catch (IOException e) {
            log.error("error reading latest versions", e);
        }

        return reader.getLatestMetadataVersions();
    }

    public Path getLocationsFile(final MetadataVersions.MetadataVersion latestVersion) throws IOException {
        final URL url = getUrl(latestVersion.filename);
        final File destination = getLocationsZipDestination();

        try {
            FileUtils.copyURLToFile(url, destination);

            return getLocationsFileFromZip(destination);
        } finally {
            FileUtils.deleteQuietly(destination);
        }
    }

    public Pair<Path, Path> getTypefiles(final MetadataVersions.MetadataVersion latestVersion) throws IOException {
        final URL url = getUrl(latestVersion.filename);
        final File destination = getCcLtnZipDestination();

        try {
            FileUtils.copyURLToFile(url, destination);

            final Path typesPath = getTypesPathFromZip(destination);
            final Path subtypesPath = getSubtypesPathFromZip(destination);

            return Pair.of(typesPath, subtypesPath);
        } finally {
            FileUtils.deleteQuietly(destination);
        }
    }

    private Path getTypesPathFromZip(final File destination) throws IOException {
        return getFileFromZip(destination, LOCATION_TYPES_FILENAME, LOCATION_TYPES_FILENAME);
    }

    private Path getSubtypesPathFromZip(final File destination) throws IOException {
        return getFileFromZip(destination, LOCATION_SUBTYPES_FILENAME, LOCATION_SUBTYPES_FILENAME);
    }

    private Path getLocationsFileFromZip(final File zipfile) throws IOException {
        return getFileFromZip(zipfile, null, LOCATIONS_FILENAME);
    }

    private Path getFileFromZip(final File zipfile, final String entryName, final String destinationName) throws IOException {
        try(final ZipFile z = new ZipFile(zipfile)) {
            final File entryDestination = Files.createTempFile(destinationName, null).toFile();
            final ZipEntry e = findEntry(z, entryName);
            final InputStream is = z.getInputStream(e);

            doCopy(is, entryDestination);

            return entryDestination.toPath();
        }
    }

    private void doCopy(final InputStream is, final File entryDestination) throws IOException {
        try(final OutputStream os = new FileOutputStream(entryDestination)) {
            IOUtils.copy(is, os);
        }
    }

    private static ZipEntry findEntry(final ZipFile z, final String name) {
        final Enumeration<? extends ZipEntry> entries = z.entries();

        // if given name is empty, return first entry
        if(StringUtils.isEmpty(name)) {
            return entries.nextElement();
        }

        while(entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();

            if(StringUtils.equals(entry.getName(), name)) {
                return entry;
            }
        }

        return null;
    }

    private static File getLocationsZipDestination() throws IOException {
        return Files.createTempFile("locations", "zip").toFile();
    }

    private static File getCcLtnZipDestination() throws IOException {
        return Files.createTempFile("cc_ltn", "zip").toFile();
    }

    public URL getLatestUrl() throws MalformedURLException {
        return getUrl(LATEST_FILENAME);
    }

    public URL getUrl(final String filename) throws MalformedURLException {
        return new URL(tmcUrl + filename);
    }
}
