package fi.livi.digitraffic.tie.metadata.model;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Road station sensor")
@JsonPropertyOrder(value = {"id", "nameFi", "shortNameFi", "description", "unit", "accuracy", "calculationFormula", "nameOld", "sensorValueDescriptions"})
@Entity
@DynamicUpdate
public class RoadStationSensor implements Comparable<RoadStationSensor> {

    /** These id:s are for station status sensors */
    public static final Set<Long> STATUS_SENSORS_NATURAL_IDS_SET =
            new HashSet<Long>(Arrays.asList(60000L, 60002L));

    @JsonIgnore
    @Id
    @SequenceGenerator(name = "RSS_SENSOR_SEQ", sequenceName = "SEQ_ROAD_STATION_SENSOR")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RSS_SENSOR_SEQ")
    private long id;

    @JsonIgnore
    private Long lotjuId;

    @ApiModelProperty(value = "Sensor id", position = 1)
    @JsonProperty("id")
    private long naturalId;

    @ApiModelProperty(value = "Sensor old name. For new sensors will equal sensorNameFi. Will deprecate in future.", position = 2, notes = "noteja")
    @JsonProperty(value = "nameOld")
    private String name;

    @ApiModelProperty(value = "Unit of sensor value")
    private String unit;

    @JsonIgnore
    private boolean obsolete;

    @JsonIgnore
    private LocalDate obsoleteDate;

    @ApiModelProperty(value = "Sensor descriptionFi [fi]")
    private String description;

    @ApiModelProperty(value = "Sensor name [fi]")
    private String nameFi;

    @ApiModelProperty(value = "Short name for sensor [fi]")
    private String shortNameFi;

    @ApiModelProperty(value = "Sensor accuracy")
    private Integer accuracy;

    @ApiModelProperty(value = "Calculation fomula of sensor value")
    private String calculationFormula;

    @ApiModelProperty("Possible additional descriptions for sensor values")
    @OneToMany(mappedBy = "sensorValueDescriptionPK.sensorId", cascade = CascadeType.ALL)
    private List<SensorValueDescription> sensorValueDescriptions;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(Long lotjuId) {
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
        if (obsoleteDate == null || obsolete == false) {
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

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNameFi() {
        return nameFi;
    }

    public void setNameFi(String nameFi) {
        this.nameFi = nameFi;
    }

    public String getShortNameFi() {
        return shortNameFi;
    }

    public void setShortNameFi(String shortNameFi) {
        this.shortNameFi = shortNameFi;
    }

    public Integer getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Integer accuracy) {
        this.accuracy = accuracy;
    }

    public String getCalculationFormula() {
        return calculationFormula;
    }

    public void setCalculationFormula(String calculationFormula) {
        this.calculationFormula = calculationFormula;
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
                .appendField("nameFi", getNameFi())
                .appendField("unit", getUnit())
                .toString();
    }

    @Override
    public int compareTo(RoadStationSensor o) {
        return Long.compare(this.getNaturalId(), o.getNaturalId());
    }

    public List<SensorValueDescription> getSensorValueDescriptions() {
        return sensorValueDescriptions;
    }

    public void setSensorValueDescriptions(List<SensorValueDescription> sensorValueDescriptions) {
        this.sensorValueDescriptions = sensorValueDescriptions;
    }
}
