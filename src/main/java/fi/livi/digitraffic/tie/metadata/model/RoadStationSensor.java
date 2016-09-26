package fi.livi.digitraffic.tie.metadata.model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.metadata.converter.RoadStationTypeEnumConverter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Road station sensor")
@JsonPropertyOrder(value = {"id", "name", "shortName", "description", "unit", "accuracy", "nameOld", "sensorValueDescriptions"})
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
    private long id;

    @JsonIgnore
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
    private boolean obsolete;

    @JsonIgnore
    private LocalDate obsoleteDate;

    @ApiModelProperty(value = "Sensor description [fi]")
    private String description;

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
    private List<SensorValueDescription> sensorValueDescriptions;

    @JsonIgnore
    @Convert(converter = RoadStationTypeEnumConverter.class)
    private RoadStationType roadStationType;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
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

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(final boolean obsolete) {
        this.obsolete = obsolete;
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(final LocalDate obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public boolean obsolete() {
        if (obsoleteDate == null || !obsolete ) {
            obsoleteDate = LocalDate.now();
            obsolete = true;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
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
        return new ToStringHelpper(this)
                .appendField("id", getId())
                .appendField("lotjuId", this.getLotjuId())
                .appendField("naturalId", getNaturalId())
                .appendField("name", getName())
                .appendField("nameFi", getNameFi())
                .appendField("unit", getUnit())
                .appendField("roadStationType", getRoadStationType())
                .toString();
    }

    public List<SensorValueDescription> getSensorValueDescriptions() {
        return sensorValueDescriptions;
    }

    public void setSensorValueDescriptions(final List<SensorValueDescription> sensorValueDescriptions) {
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
}
