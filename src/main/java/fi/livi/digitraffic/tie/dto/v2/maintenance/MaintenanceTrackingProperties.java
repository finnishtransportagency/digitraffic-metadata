package fi.livi.digitraffic.tie.dto.v2.maintenance;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.livi.digitraffic.tie.metadata.geojson.Properties;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingWorkMachine;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "MaintenanceTrackingProperties", description = "Maintenance tracking properties")
public class MaintenanceTrackingProperties extends Properties {

    @ApiModelProperty(value = "Id for the tracking", required = true)
    public final long id;

    // "Value is not allowe to share to public"
    @JsonIgnore()
    // @ApiModelProperty(value = "Id for work machine for tracking", required = true)
    public final long workMachineId;

    @ApiModelProperty(value = "Time when tracking was reported", required = true)
    public final ZonedDateTime sendingTime;

    @ApiModelProperty(value = "Tasks done during maintenance work", required = true)
    public final Set<MaintenanceTrackingTask> tasks;

    @ApiModelProperty(value = "Start time of maintenance work tasks", required = true)
    public final ZonedDateTime startTime;

    @ApiModelProperty(value = "End time of maintenance work tasks", required = true)
    public final ZonedDateTime endTime;

    @ApiModelProperty(value = "Direction of the last observation")
    public BigDecimal direction;

    public MaintenanceTrackingProperties(final long id, final MaintenanceTrackingWorkMachine workMachine,
                                         final ZonedDateTime sendingTime, final ZonedDateTime startTime, final ZonedDateTime endTime,
                                         final Set<MaintenanceTrackingTask> tasks, BigDecimal direction) {
        this.id = id;
        this.workMachineId = workMachine.getId();
        this.sendingTime = sendingTime;
        this.tasks = tasks;
        this.startTime = startTime;
        this.endTime = endTime;
        this.direction = direction;
    }
}
