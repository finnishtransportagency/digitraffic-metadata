package fi.livi.digitraffic.tie.model.v2.maintenance;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

public interface MaintenanceTrackingDto {

    Long getId();

    Instant getSendingTime();

    Instant getStartTime();

    Instant getEndTime();

    String getLineStringJson();

    String getLastPointJson();

    BigDecimal getDirection();

    String getTasksAsString();

    Long getWorkMachineId();

    default Set<MaintenanceTrackingTask> getTasks() {
        return Arrays.stream(getTasksAsString().split(",")).map(s -> MaintenanceTrackingTask.valueOf(s)).collect(Collectors.toSet());
    }

    default String toStringTiny() {
        return ToStringHelper.toStringExcluded(this, "lineString");
    }
}
