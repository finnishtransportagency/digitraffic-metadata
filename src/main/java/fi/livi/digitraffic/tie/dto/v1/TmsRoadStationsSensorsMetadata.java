package fi.livi.digitraffic.tie.dto.v1;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Available sensors of weather stations")
@JsonPropertyOrder({ "dataUpdatedTime", "dataLastCheckedTime", "roadStationSensors" })
public class TmsRoadStationsSensorsMetadata extends RootMetadataObjectDto {

    @ApiModelProperty(value = "Available sensors of weather stations", required = true)
    private final List<TmsRoadStationSensorDto> roadStationSensors;

    public TmsRoadStationsSensorsMetadata(final List<TmsRoadStationSensorDto> roadStationSensors, final ZonedDateTime lastUpdated, final ZonedDateTime dataLastCheckedTime) {
        super(lastUpdated, dataLastCheckedTime);
        this.roadStationSensors = roadStationSensors;
    }

    public List<TmsRoadStationSensorDto> getRoadStationSensors() {
        return roadStationSensors;
    }
}
