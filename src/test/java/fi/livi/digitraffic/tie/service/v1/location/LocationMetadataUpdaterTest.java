package fi.livi.digitraffic.tie.service.v1.location;

import static junit.framework.TestCase.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.service.IllegalArgumentException;

public class LocationMetadataUpdaterTest extends AbstractServiceTest {
    @Autowired
    private LocationMetadataUpdater locationMetadataUpdater;

    @SpyBean
    private MetadataFileFetcher metadataFileFetcher;

    @Test
    @Disabled
    public void findAndUpdate() throws IOException {
        locationMetadataUpdater.findAndUpdate();
        verify(metadataFileFetcher).getFilePaths(any(MetadataVersions.class));
    }

    @Test
    public void findAndUpdateVersionsDiffer() throws IOException {
        final MetadataVersions mv = mock(MetadataVersions.class);
        when(mv.getLocationsVersion()).thenReturn(new MetadataVersions.MetadataVersion("a", "1"));
        when(mv.getLocationTypeVersion()).thenReturn(new MetadataVersions.MetadataVersion("b", "2"));

        when(metadataFileFetcher.getLatestVersions()).thenReturn(mv);

        locationMetadataUpdater.findAndUpdate();

        verify(metadataFileFetcher, never()).getFilePaths(any(MetadataVersions.class));
    }

    @Test
    public void findAndUpdateNoUpdateNeeded() throws IOException {
        final MetadataVersions mv = mock(MetadataVersions.class);
        when(mv.getLocationsVersion()).thenReturn(new MetadataVersions.MetadataVersion("a", "1.1"));
        when(mv.getLocationTypeVersion()).thenReturn(new MetadataVersions.MetadataVersion("a", "1.1"));

        when(metadataFileFetcher.getLatestVersions()).thenReturn(mv);

        locationMetadataUpdater.findAndUpdate();

        verify(metadataFileFetcher, never()).getFilePaths(any(MetadataVersions.class));
    }

    @Test
    public void findAndUpdateException() throws IOException {
        try {
            when(metadataFileFetcher.getLatestVersions()).thenThrow(new IllegalArgumentException("TEST"));

            locationMetadataUpdater.findAndUpdate();

            fail();
        } catch(final IllegalArgumentException iae) {
            assertEquals(iae.getMessage(), "TEST");
        }

        verify(metadataFileFetcher, never()).getFilePaths(any(MetadataVersions.class));
    }

    @Test
    public void findAndUpdateVersionsEmpty() throws IOException {
        when(metadataFileFetcher.getLatestVersions()).thenReturn(null);

        locationMetadataUpdater.findAndUpdate();

        verify(metadataFileFetcher, never()).getFilePaths(any(MetadataVersions.class));
    }
}
