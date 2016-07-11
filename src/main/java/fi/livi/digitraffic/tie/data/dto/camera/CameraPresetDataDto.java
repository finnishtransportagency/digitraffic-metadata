package fi.livi.digitraffic.tie.data.dto.camera;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.MeasuredDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CameraPresetData", description = "Road wather station with sensor values")
@JsonPropertyOrder( value = {"id", "presentationName", "nameOnDevice", "public", "imageUrl"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraPresetDataDto implements MeasuredDataObjectDto {

    @ApiModelProperty(value = "Camera preset id", position = 1)
    private String id;

    @ApiModelProperty(value = "PresentationName (Preset name 1, direction)")
    private String presentationName;

    @ApiModelProperty(value = "Name on device (Preset name 2)")
    private String nameOnDevice;

    @ApiModelProperty(value = "Image url")
    private String imageUrl;

    @JsonIgnore
    private LocalDateTime measured;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setNameOnDevice(String nameOnDevice) {
        this.nameOnDevice = nameOnDevice;
    }

    public String getNameOnDevice() {
        return nameOnDevice;
    }

    public void setPresentationName(String presentationName) {
        this.presentationName = presentationName;
    }

    public String getPresentationName() {
        return presentationName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public LocalDateTime getMeasured() {
        return measured;
    }

    public void setMeasured(LocalDateTime measured) {
        this.measured = measured;
    }
}