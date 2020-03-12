package fi.livi.digitraffic.tie.conf;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.dto.v1.FlywayVersion;
import fi.livi.digitraffic.tie.service.FlywayService;

@Component
public class ActuatorInfo implements InfoContributor {

    private static final Logger log = LoggerFactory.getLogger(ActuatorInfo.class);
    private final FlywayService flywayService;

    @Autowired
    public ActuatorInfo(final FlywayService flywayService) {
        this.flywayService = flywayService;
    }

    @Override
    public void contribute(final Builder builder) {
        final Map<String, String> db = new HashMap<>();
        try {
            final FlywayVersion first = flywayService.getLatestVersion();
            db.put("version", first.getVersion());
            db.put("success", first.getSuccess().toString());
            final Instant instant = first.getInstalledOn().truncatedTo(ChronoUnit.SECONDS).atZone(ZoneOffset.UTC).toInstant();
            db.put("installedOn", instant.toString());
        } catch (Exception e) {
            log.error("Could not get db version info", e);
            db.put("error", "Could not get db version info");
        }
        builder.withDetail("db", db);
    }
}