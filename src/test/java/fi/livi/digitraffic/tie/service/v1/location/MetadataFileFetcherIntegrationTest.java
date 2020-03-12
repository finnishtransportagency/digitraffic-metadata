package fi.livi.digitraffic.tie.service.v1.location;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@Import(MetadataFileFetcher.class)
public class MetadataFileFetcherIntegrationTest {
    @Test(expected = FileNotFoundException.class)
    public void testLocationNotFound() throws IOException {
        final MetadataFileFetcher fetcher = new MetadataFileFetcher("https://aineistot.vayla.fi/not_found");

        fetcher.getLocationsFile(new MetadataVersions.MetadataVersion("", ""));
    }
}
