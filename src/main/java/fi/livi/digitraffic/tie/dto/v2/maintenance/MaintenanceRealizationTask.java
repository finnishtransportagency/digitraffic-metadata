package fi.livi.digitraffic.tie.dto.v2.maintenance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "MaintenanceRealizationTask", description = "Maintenance realization task properties")
public class MaintenanceRealizationTask {

    @ApiModelProperty(value = "Id of the task", required = true)
    public final long id;

    @ApiModelProperty(value = "Task in Finnish", required = true)
    public final String fi;

    @ApiModelProperty(value = "Task in Swedish")
    public final String sv;

    @ApiModelProperty(value = "Task in English")
    public final String en;

    @ApiModelProperty(value = "Operation id", required = true)
    public final long operationId;

    @ApiModelProperty(value = "Category id", required = true)
    public final long categoryId;

    public MaintenanceRealizationTask(final long id, final String fi, final String sv, final String en,
                                      final long operationId, final long categoryId) {
        this.id = id;
        this.fi = fi;
        this.sv = sv;
        this.en = en;
        this.operationId = operationId;
        this.categoryId = categoryId;
    }
}
