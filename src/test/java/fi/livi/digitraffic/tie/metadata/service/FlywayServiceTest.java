package fi.livi.digitraffic.tie.metadata.service;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.dto.v1.FlywayVersion;
import fi.livi.digitraffic.tie.service.FlywayService;

public class FlywayServiceTest extends AbstractServiceTest {

    @Autowired
    private FlywayService flywayService;

    @Test
    public void latestVersion() {
        final FlywayVersion lv = flywayService.getLatestVersion();
        assertTrue(lv.getInstalledOn().isBefore(DateHelper.getZonedDateTimeNowAtUtc().toLocalDateTime()));
        assertTrue(lv.getSuccess());
        Assert.assertNotNull(lv.getVersion());
    }

}
