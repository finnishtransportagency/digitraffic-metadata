package fi.livi.digitraffic.tie.dto.v1.camera;

import java.time.ZonedDateTime;
import java.util.List;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Immutable
@ApiModel(value = "CameraData", description = "Latest image data from camera stations", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataUpdatedTime", "cameraStationData"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraRootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Camera stations data")
    private final List<CameraStationDataDto> cameraStations;

    public CameraRootDataObjectDto(final List<CameraStationDataDto> cameraStationData,
                                   final ZonedDateTime updated) {
        super(updated);
        this.cameraStations = cameraStationData;
    }

    public CameraRootDataObjectDto(final ZonedDateTime updated) {
        this(null, updated);
    }

    public List<CameraStationDataDto> getCameraStations() {
        return cameraStations;
    }
}
