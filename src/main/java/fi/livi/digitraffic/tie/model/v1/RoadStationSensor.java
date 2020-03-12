package fi.livi.digitraffic.tie.model.v1;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.SortNatural;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.VehicleClass;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Road station sensor")
@JsonPropertyOrder(value = {"id", "name", "shortName", "descriptionFi", "descriptionSv", "descriptionEn", "unit", "accuracy", "nameOld", "sensorValueDescriptions"})
@Entity
@DynamicUpdate
public class RoadStationSensor {

    /** These id:s are for station status sensors */
    protected static final Set<Long> STATUS_SENSORS_NATURAL_IDS_SET =
            new HashSet<Long>(Arrays.asList(60000L, 60002L));

    @JsonIgnore
    @Id
    @GenericGenerator(name = "SEQ_ROAD_STATION_SENSOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD_STATION_SENSOR"))
    @GeneratedValue(generator = "SEQ_ROAD_STATION_SENSOR")
    private Long id;

    @JsonIgnore
    @NotNull
    private Long lotjuId;

    @ApiModelProperty(value = "Sensor id", position = 1)
    @JsonProperty("id")
    private long naturalId;

    @ApiModelProperty(value = "Sensor old name. For new sensors will equal name. Will deprecate in future.", position = 2, notes = "noteja")
    @JsonProperty(value = "nameOld")
    private String name;

    @ApiModelProperty(value = "Unit of sensor value")
    private String unit;

    @JsonIgnore
    private LocalDate obsoleteDate;

    @ApiModelProperty(value = "Sensor description [fi]")
    private String descriptionFi;

    @ApiModelProperty(value = "Sensor description [sv]")
    private String descriptionSv;

    @ApiModelProperty(value = "Sensor description [en]")
    private String descriptionEn;

    @ApiModelProperty(value = "Sensor name [fi]")
    @JsonProperty(value = "name")
    private String nameFi;

    @ApiModelProperty(value = "Short name for sensor [fi]")
    @JsonProperty(value = "shortName")
    private String shortNameFi;

    @ApiModelProperty(value = "Sensor accuracy")
    private Integer accuracy;

    @ApiModelProperty("Possible additional descriptions for sensor values")
    @OneToMany(mappedBy = "sensorValueDescriptionPK.sensorId", cascade = CascadeType.ALL)
    @OrderBy("sensorValueDescriptionPK.sensorValue")
    @SortNatural
    private SortedSet<SensorValueDescription> sensorValueDescriptions;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private RoadStationType roadStationType;

    @ApiModelProperty(value = "Presentation name for sensor [fi]")
    private String presentationNameFi;

    @ApiModelProperty(value = "Presentation name for sensor [sv]")
    private String presentationNameSv;

    @ApiModelProperty(value = "Presentation name for sensor [en]")
    private String presentationNameEn;

    @JsonIgnore
    private boolean isPublic;

    @ApiModelProperty(value = "Vehicle class")
    @Enumerated(EnumType.STRING)
    private VehicleClass vehicleClass;

    @ApiModelProperty(value = "Lane of the sensor, 1st, 2nd, 3rd, etc.")
    private Integer lane;

    @ApiModelProperty(value = "Preset direction " +
        "(0 = Unknown direction. " +
        "1 = According to the road register address increasing direction. I.e. on the road 4 to Rovaniemi." +
        "2 = According to the road register address decreasing direction. I.e. on the road 4 to Helsinki.")
    private Integer direction;

    /**
     * This value is calculated by db so it's value is not
     * reliable if entity is modified after fetch from db.
     */
    @Column(updatable = false, insertable = false) // virtual column
    private boolean publishable;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(final Long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public long getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final long naturalId) {
        this.naturalId = naturalId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(final LocalDate obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public boolean obsolete() {
        if (obsoleteDate == null) {
            obsoleteDate = LocalDate.now();
            return true;
        }
        return false;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(final String unit) {
        this.unit = unit;
    }

    @ApiModelProperty(value = "Sensor description [fi]")
    public String getDescription() {
        return getDescriptionFi();
    }

    public String getDescriptionFi() {
        return descriptionFi;
    }

    public void setDescriptionFi(final String description) {
        this.descriptionFi = description;
    }

    public void setDescriptionSv(final String descriptionSv) {
        this.descriptionSv = descriptionSv;
    }

    public String getDescriptionSv() {
        return descriptionSv;
    }

    public void setDescriptionEn(final String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public String getNameFi() {
        return nameFi;
    }

    public void setNameFi(final String nameFi) {
        this.nameFi = StringUtils.upperCase(nameFi);
    }

    public String getShortNameFi() {
        return shortNameFi;
    }

    public void setShortNameFi(final String shortNameFi) {
        this.shortNameFi = shortNameFi;
    }

    public Integer getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(final Integer accuracy) {
        this.accuracy = accuracy;
    }

    @JsonIgnore
    public boolean isStatusSensor() {
        return STATUS_SENSORS_NATURAL_IDS_SET.contains(naturalId);
    }

    @Override
    public String toString() {
        return new ToStringHelper(this)
                .appendField("id", getId())
                .appendField("lotjuId", this.getLotjuId())
                .appendField("naturalId", getNaturalId())
                .appendField("name", getName())
                .appendField("nameFi", getNameFi())
                .appendField("unit", getUnit())
                .appendField("roadStationType", getRoadStationType())
                .toString();
    }

    public SortedSet<SensorValueDescription> getSensorValueDescriptions() {
        return sensorValueDescriptions;
    }

    public void setSensorValueDescriptions(final SortedSet<SensorValueDescription> sensorValueDescriptions) {
        this.sensorValueDescriptions = sensorValueDescriptions;
    }

    public RoadStationType getRoadStationType() {
        return roadStationType;
    }

    public void setRoadStationType(RoadStationType roadStationType) {
        if (this.roadStationType != null && !this.roadStationType.equals(roadStationType)) {
            throw new IllegalStateException("Cannot change roadStationType of RoadStationSensor from " +
                    this.roadStationType + " to " + roadStationType + ". (" + this.toString() + ")");
        }
        this.roadStationType = roadStationType;
    }

    public void setPresentationNameFi(final String presentationNameFi) {
        this.presentationNameFi = presentationNameFi;
    }

    public String getPresentationNameFi() {
        return presentationNameFi;
    }

    public void setPresentationNameSv(final String presentationNameSe) {
        this.presentationNameSv = presentationNameSe;
    }

    public String getPresentationNameSv() {
        return presentationNameSv;
    }

    public void setPresentationNameEn(final String presentationNameEn) {
        this.presentationNameEn = presentationNameEn;
    }

    public String getPresentationNameEn() {
        return presentationNameEn;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @JsonIgnore
    public boolean isPublic() {
        return isPublic;
    }

    public void setVehicleClass(final VehicleClass vehicleClass) {
        this.vehicleClass = vehicleClass;
    }

    public VehicleClass getVehicleClass() {
        return vehicleClass;
    }

    public void setLane(final Integer lane) {
        this.lane = lane;
    }

    public Integer getLane() {
        return lane;
    }

    public void setDirection(final Integer direction) {
        this.direction = direction;
    }

    public Integer getDirection() {
        return direction;
    }
}
