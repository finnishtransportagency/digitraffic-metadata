package fi.livi.digitraffic.tie.metadata.geojson.camera;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Camera preset", value = "CameraPreset")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "presetId", "cameraId", "name" })
public class CameraPresetDto implements Comparable<CameraPresetDto>{

    /** Presentation names that are set for unknown directions in Lotju */
    private static final Set<String> UNKNOWN_PRESENTATION_NAMES =
            new HashSet(Arrays.asList(new String[] {"-", "–", "PUUTTUU"}));

    public enum Direction {
        UNKNOWN(0),
        INCREASING_DIRECTION(1),
        DECREASING_DIRECTION(2),
        CROSSING_ROAD_INCREASING_DIRECTION(3),
        CROSSING_ROAD_DECREASING_DIRECTION(4),
        SPECIAL_DIRECTION(null);

        private final Integer code;

        Direction(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }

        public static Direction getDirection(String code) {
            if (code == null) {
                return UNKNOWN;
            }
            try {
                int parsed = Integer.parseInt(code);
                for (Direction direction : Direction.values()) {
                    if (direction.getCode() != null && direction.getCode().equals(parsed)) {
                        return direction;
                    }
                }
                return SPECIAL_DIRECTION;
            } catch (NumberFormatException e) {
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
    @JsonProperty("id")
    private String presetId;

    @ApiModelProperty(value = "Preset description")
    private String description;

    @ApiModelProperty(value = "PresentationName (Preset name 1, direction)")
    private String presentationName;

    @ApiModelProperty(value = "Name on device (Preset name 2)")
    private String nameOnDevice;

    @ApiModelProperty(value = "Preset order")
    private Integer presetOrder;

    @ApiModelProperty(name = "public", value = "Is image publicly available")
    @JsonProperty(value = "public")
    private boolean isPublic;

    @ApiModelProperty(value = "Is data in collection")
    private boolean inCollection;

    @ApiModelProperty(value = "Jpeg image Quality Factor (Q)")
    private Integer compression;

    @ApiModelProperty(value = "Resolution of camera [px x px]")
    private String resolution;

    @JsonIgnore
    private Long cameraLotjuId;

    @ApiModelProperty(value = "Preset direction " +
                              "(0 = Unknown direction. " +
                              "1 = According to the road register address increasing direction. I.e. on the road 4 to Lahti, if we are in Korso. " +
                              "2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki, if we are in Korso. " +
                              "3 = Increasing direction of the crossing road. " +
                              "4 = Decreasing direction of the crossing road" +
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

    public void setLotjuId(Long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public Long getCameraLotjuId() {
        return cameraLotjuId;
    }

    public void setCameraLotjuId(Long cameraLotjuId) {
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

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setPresentationName(final String presentationName) {
        this.presentationName = presentationName;
    }

    public String getPresentationName() {
        return presentationName;
    }

    public void setNameOnDevice(final String nameOnDevice) {
        this.nameOnDevice = nameOnDevice;
    }

    public String getNameOnDevice() {
        return nameOnDevice;
    }

    public void setPresetOrder(final Integer presetOrder) {
        this.presetOrder = presetOrder;
    }

    public Integer getPresetOrder() {
        return presetOrder;
    }

    public void setPublic(final boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setInCollection(final boolean inCollection) {
        this.inCollection = inCollection;
    }

    public boolean isInCollection() {
        return inCollection;
    }

    public void setCompression(final Integer compression) {
        this.compression = compression;
    }

    public Integer getCompression() {
        return compression;
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

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        CameraPresetDto that = (CameraPresetDto) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(isPublic, that.isPublic)
                .append(inCollection, that.inCollection)
                .append(lotjuId, that.lotjuId)
                .append(cameraId, that.cameraId)
                .append(presetId, that.presetId)
                .append(description, that.description)
                .append(presentationName, that.presentationName)
                .append(nameOnDevice, that.nameOnDevice)
                .append(presetOrder, that.presetOrder)
                .append(compression, that.compression)
                .append(resolution, that.resolution)
                .append(cameraLotjuId, that.cameraLotjuId)
                .append(directionCode, that.directionCode)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(id)
                .append(cameraId)
                .append(presetId)
                .append(description)
                .append(presentationName)
                .append(nameOnDevice)
                .append(presetOrder)
                .append(isPublic)
                .append(inCollection)
                .append(compression)
                .append(nameOnDevice)
                .append(resolution)
                .append(directionCode)
                .toHashCode();
    }

    public static boolean isUnknownPresentationName(String name) {
        if (name == null) {
            return false;
        }
        return UNKNOWN_PRESENTATION_NAMES.contains(name.trim().toUpperCase());
    }

    @Override
    public int compareTo(CameraPresetDto other) {
        if (other == null) {
            return 1;
        }
        if (this.getPresetOrder() == null && other.getPresetOrder() == null) {
            return 0;
        } else if (this.getPresetOrder() != null && other.getPresetOrder() == null) {
            return 1;
        } else if (this.getPresetOrder() == null && other.getPresetOrder() != null) {
            return -1;
        }
        return this.getPresetOrder().compareTo(other.getPresetOrder());
    }
}
