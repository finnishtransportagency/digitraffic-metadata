package fi.livi.digitraffic.tie.dto.v1.camera;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;

@Immutable
@ApiModel(value = "CameraStationData", description = "Road wather station with sensor values")
@JsonPropertyOrder( value = {"id", "roadStationId", "nearestWeatherStationId", "sensorValues"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraStationDataDto {

    private String id;
    private Long roadStationId;
    private Long nearestWeatherStationId;

    private List<CameraPresetDataDto> cameraPresets = new ArrayList<>();

    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setRoadStationId(final Long roadStationId) {
        this.roadStationId = roadStationId;
    }

    public Long getRoadStationId() {
        return roadStationId;
    }

    public void setNearestWeatherStationId(final Long nearestWeatherStationId) {
        this.nearestWeatherStationId = nearestWeatherStationId;
    }

    public Long getNearestWeatherStationId() {
        return nearestWeatherStationId;
    }

    public List<CameraPresetDataDto> getCameraPresets() {
        return cameraPresets;
    }

    public void setCameraPresets(final List<CameraPresetDataDto> cameraPresets) {
        this.cameraPresets = cameraPresets;
    }

    public void addPreset(final CameraPresetDataDto cameraPresetDataDto) {
        this.cameraPresets.add(cameraPresetDataDto);
    }
}
