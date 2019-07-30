package fi.livi.digitraffic.tie.metadata.geojson.camera;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Camera preset", value = "CameraPreset")
@JsonPropertyOrder({ "presetId", "cameraId", "name" })
public class CameraPresetDto implements Comparable<CameraPresetDto>{

    public enum Direction {
        UNKNOWN(0),
        INCREASING_DIRECTION(1),
        DECREASING_DIRECTION(2),
        CROSSING_ROAD_INCREASING_DIRECTION(3),
        CROSSING_ROAD_DECREASING_DIRECTION(4),
        SPECIAL_DIRECTION(null);

        private final Integer code;

        Direction(final Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }

        public static Direction getDirection(final String code) {
            if (code == null) {
                return UNKNOWN;
            }
            try {
                final int parsed = Integer.parseInt(code);
                for (final Direction direction : Direction.values()) {
                    if (direction.getCode() != null && direction.getCode().equals(parsed)) {
                        return direction;
                    }
                }
                return SPECIAL_DIRECTION;
            } catch (final NumberFormatException e) {
                return UNKNOWN;
            }
        }

    }

    @JsonIgnore // Using presetId id as id
    private long id;

    @JsonIgnore
    private Long lotjuId;

    @ApiModelProperty(value = "Camera id", position = 2)
    private String cameraId;

    @ApiModelProperty(value = "Camera preset id", position = 1)
    private String presetId;

    @ApiModelProperty(value = "PresentationName (Preset name 1, direction)")
    private String presentationName;

    @ApiModelProperty(value = "Is data in collection")
    private boolean inCollection;

    @ApiModelProperty(value = "Resolution of camera [px x px]")
    private String resolution;

    @JsonIgnore
    private Long cameraLotjuId;

    @ApiModelProperty(value = "Preset direction\n" +
                              "0 = Unknown direction. \n" +
                              "1 = According to the road register address increasing direction. I.e. on the road 4 to Lahti, if we are in Korso. \n" +
                              "2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki, if we are in Korso. \n" +
                              "3 = Increasing direction of the crossing road. \n" +
                              "4 = Decreasing direction of the crossing road.\n" +
                              "5-99 = Special directions)", required = true, position = 1)
    private String directionCode;

    @ApiModelProperty(value = "Image url")
    private String imageUrl;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setLotjuId(final Long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public Long getCameraLotjuId() {
        return cameraLotjuId;
    }

    public void setCameraLotjuId(final Long cameraLotjuId) {
        this.cameraLotjuId = cameraLotjuId;
    }

    public void setCameraId(final String cameraId) {
        this.cameraId = cameraId;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setPresetId(final String presetId) {
        this.presetId = presetId;
    }

    public String getPresetId() {
        return presetId;
    }

    public void setPresentationName(final String presentationName) {
        this.presentationName = presentationName;
    }

    public String getPresentationName() {
        return presentationName;
    }

    public void setInCollection(final boolean inCollection) {
        this.inCollection = inCollection;
    }

    public boolean isInCollection() {
        return inCollection;
    }

    public void setResolution(final String resolution) {
        this.resolution = resolution;
    }

    public String getResolution() {
        return resolution;
    }

    public void setDirectionCode(final String directionCode) {
        this.directionCode = directionCode;
    }

    public String getDirectionCode() {
        return directionCode;
    }

    @ApiModelProperty(value = "Direction of camera")
    public Direction getDirection() {
        return Direction.getDirection(directionCode);
    }

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final CameraPresetDto that = (CameraPresetDto) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(inCollection, that.inCollection)
                .append(lotjuId, that.lotjuId)
                .append(cameraId, that.cameraId)
                .append(presetId, that.presetId)
                .append(presentationName, that.presentationName)
                .append(resolution, that.resolution)
                .append(cameraLotjuId, that.cameraLotjuId)
                .append(directionCode, that.directionCode)
                .append(imageUrl, that.imageUrl)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(lotjuId)
                .append(cameraId)
                .append(presetId)
                .append(presentationName)
                .append(inCollection)
                .append(resolution)
                .append(cameraLotjuId)
                .append(directionCode)
                .append(imageUrl)
                .toHashCode();
    }

    @Override
    public int compareTo(final CameraPresetDto other) {
        if ( other == null ||
             (this.getPresetId() != null && other.getPresetId() == null)) {
            return -1;
        } else if (this.getPresetId() == null && other.getPresetId() != null) {
            return 1;
        } else if (this.getPresetId() == null && other.getPresetId() == null) {
            return 0;
        }
        return this.getPresetId().compareTo(other.getPresetId());
    }
}
