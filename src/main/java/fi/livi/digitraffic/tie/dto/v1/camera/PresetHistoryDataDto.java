package fi.livi.digitraffic.tie.dto.v1.camera;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "PresetHistoryData", description = "Weather camera preset's image history details.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresetHistoryDataDto {

    @ApiModelProperty("Last modified date of the image.")
    private final ZonedDateTime lastModified;
    @ApiModelProperty("Url to read the image.")
    private final String imageUrl;
    @ApiModelProperty("Image size in bytes.")
    private int sizeBytes;

    public PresetHistoryDataDto(final ZonedDateTime lastModified, final String imageUrl, final int sizeBytes) {
        this.lastModified = lastModified;
        this.imageUrl = imageUrl;
        this.sizeBytes = sizeBytes;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ZonedDateTime getLastModified() {
        return lastModified;
    }

    public int getSizeBytes() {
        return sizeBytes;
    }
}
