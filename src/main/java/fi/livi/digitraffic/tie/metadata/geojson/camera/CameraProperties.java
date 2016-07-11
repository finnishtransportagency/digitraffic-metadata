package fi.livi.digitraffic.tie.metadata.geojson.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.roadstation.RoadStationProperties;
import fi.livi.digitraffic.tie.metadata.geojson.roadweather.RoadWeatherStationProperties;
import fi.livi.digitraffic.tie.metadata.model.CameraType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Camera preset properties", value = "CameraProperties", parent = RoadWeatherStationProperties.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "presetId", "cameraId", "naturalId", "name", "cameraType" })
public class CameraProperties extends RoadStationProperties {

    @JsonIgnore // Camerapreset id
    private long id;

    @ApiModelProperty(value = "Camera id", position = 2)
    @JsonProperty("id")
    private String cameraId;

    @ApiModelProperty(value = "Type of camera")
    private CameraType cameraType;

    @ApiModelProperty(value = "Is camera targeted to default direction")
    private Boolean defaultDirection;

    @ApiModelProperty(name = "nearestRoadWeatherStationId", value = "Id of nearest road weather station")
    @JsonProperty(value = "nearestRoadWeatherStationId")
    private Long nearestRoadWeatherStationNaturalId;

    @ApiModelProperty(value = "Camera presets")
    private List<CameraPresetDto> presets = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setCameraId(final String cameraId) {
        this.cameraId = cameraId;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraType(final CameraType cameraType) {
        this.cameraType = cameraType;
    }

    public CameraType getCameraType() {
        return cameraType;
    }

    public void setNearestRoadWeatherStationNaturalId(Long nearestRoadWeatherStationNaturalId) {
        this.nearestRoadWeatherStationNaturalId = nearestRoadWeatherStationNaturalId;
    }

    public Long getNearestRoadWeatherStationNaturalId() {
        return nearestRoadWeatherStationNaturalId;
    }

    public List<CameraPresetDto> getPresets() {
        return presets;
    }

    public void setPresets(List<CameraPresetDto> presets) {
        this.presets = presets;
    }

    public void addPreset(CameraPresetDto preset) {
        this.presets.add(preset);
        Collections.sort(this.presets);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        CameraProperties that = (CameraProperties) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(id, that.id)
                .append(cameraId, that.cameraId)
                .append(cameraType, that.cameraType)
                .append(defaultDirection, that.defaultDirection)
                .append(nearestRoadWeatherStationNaturalId, that.nearestRoadWeatherStationNaturalId)
                .append(presets, that.presets)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(id)
                .append(cameraId)
                .append(cameraType)
                .append(defaultDirection)
                .append(nearestRoadWeatherStationNaturalId)
                .append(presets)
                .toHashCode();
    }
}