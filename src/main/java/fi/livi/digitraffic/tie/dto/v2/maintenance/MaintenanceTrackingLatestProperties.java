package fi.livi.digitraffic.tie.dto.v2.maintenance;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

import fi.livi.digitraffic.tie.metadata.geojson.Properties;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "MaintenanceTrackingLatestProperties", description = "Maintenance tracking properties")
public class MaintenanceTrackingLatestProperties extends Properties {

    @ApiModelProperty(value = "Id for the tracking", required = true)
    private final long id;

    @ApiModelProperty(value = "Time of latest tracking", required = true)
    private ZonedDateTime time;

    @ApiModelProperty(value = "Tasks done during maintenance work", required = true)
    public final Set<MaintenanceTrackingTask> tasks;

    @ApiModelProperty(value = "Direction of the last observation")
    private BigDecimal direction;

    public MaintenanceTrackingLatestProperties(final long id, final ZonedDateTime time,
                                               final Set<MaintenanceTrackingTask> tasks, final BigDecimal direction) {
        this.id = id;
        this.time = time;
        this.tasks = tasks;
        this.direction = direction;
    }

    public long getId() {
        return id;
    }

    public void setDirection(final BigDecimal direction) {
        this.direction = direction;
    }

    public BigDecimal getDirection() {
        return direction;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(final ZonedDateTime time) {
        this.time = time;
    }
}
