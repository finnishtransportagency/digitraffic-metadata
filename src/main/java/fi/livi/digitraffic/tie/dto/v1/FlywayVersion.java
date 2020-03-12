package fi.livi.digitraffic.tie.dto.v1;

import java.time.LocalDateTime;

public interface FlywayVersion {
    String getVersion();
    LocalDateTime getInstalledOn();
    Boolean getSuccess();
}
