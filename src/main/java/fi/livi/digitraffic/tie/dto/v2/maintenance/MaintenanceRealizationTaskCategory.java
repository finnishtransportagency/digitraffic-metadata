package fi.livi.digitraffic.tie.dto.v2.maintenance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "MaintenanceRealizationTaskOperation", description = "Maintenance realization task Operation")
public class MaintenanceRealizationTaskCategory {

    @ApiModelProperty(value = "Id of the operation", required = true)
    public final long id;

    @ApiModelProperty(value = "Operation in Finnish", required = true)
    public final String fi;

    @ApiModelProperty(value = "Operation in Swedish")
    public final String sv;

    @ApiModelProperty(value = "Operation in English")
    public final String en;

    public MaintenanceRealizationTaskCategory(final long id, final String fi, final String sv, final String en) {
        this.id = id;
        this.fi = fi;
        this.sv = sv;
        this.en = en;
    }
}
