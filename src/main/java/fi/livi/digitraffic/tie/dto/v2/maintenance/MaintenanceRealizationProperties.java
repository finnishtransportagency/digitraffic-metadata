package fi.livi.digitraffic.tie.dto.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "MaintenanceRealizationProperties", description = "Maintenance realization properties")
public class MaintenanceRealizationProperties {


    @ApiModelProperty(value = "If for the realization", required = true)
    public Long id;

    @ApiModelProperty(value = "Time when realization was reported", required = true)
    public ZonedDateTime sendingTime;

    @ApiModelProperty(value = "Tasks done during maintenance work", required = true)
    public final Set<Long> tasks;

    @ApiModelProperty(value = "Start time of maintenance work tasks", required = true)
    public ZonedDateTime startTime;

    @ApiModelProperty(value = "End time of maintenance work tasks", required = true)
    public ZonedDateTime endTime;


    public MaintenanceRealizationProperties(final long id, final ZonedDateTime sendingTime, final ZonedDateTime startTime, final ZonedDateTime endTime, final Set<Long> tasks) {
        this.id = id;
        this.sendingTime = sendingTime;
        this.tasks = tasks;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
