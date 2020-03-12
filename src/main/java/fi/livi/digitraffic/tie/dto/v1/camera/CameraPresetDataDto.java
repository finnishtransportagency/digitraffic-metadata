package fi.livi.digitraffic.tie.dto.v1.camera;

import java.time.ZonedDateTime;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.MeasuredDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Immutable
@ApiModel(value = "CameraPresetData", description = "Camera's preset data", parent = MeasuredDataObjectDto.class)
@JsonPropertyOrder( value = {"id", "presentationName", "public", "imageUrl"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraPresetDataDto implements MeasuredDataObjectDto {

    @ApiModelProperty(value = "Camera preset id", position = 1)
    private String id;

    @ApiModelProperty(value = "PresentationName (Preset name 1, direction)")
    private String presentationName;

    @ApiModelProperty(value = "Image url")
    private String imageUrl;

    private ZonedDateTime measuredTime;

    public ZonedDateTime getMeasuredTime() {
        return measuredTime;
    }

    public void setMeasuredTime(ZonedDateTime measuredTime) {
        this.measuredTime = measuredTime;
    }
    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPresentationName(final String presentationName) {
        this.presentationName = presentationName;
    }

    public String getPresentationName() {
        return presentationName;
    }

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

}
