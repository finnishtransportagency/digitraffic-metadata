package fi.livi.digitraffic.tie.dto.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.model.v1.SensorValueDescription;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Road station sensor")
@JsonPropertyOrder(value = {"id", "name", "shortName", "descriptionFi", "descriptionSv", "descriptionEn", "unit", "accuracy", "nameOld", "sensorValueDescriptions"})
public abstract class RoadStationSensorDto {

    @ApiModelProperty(value = "Sensor id", position = 1)
    @JsonProperty("id")
    private long naturalId;

    @ApiModelProperty(value = "Sensor old name. For new sensors will equal name. Will deprecate in future.", position = 2, notes = "noteja")
    @JsonProperty(value = "nameOld")
    private String name;

    @ApiModelProperty(value = "Unit of sensor value")
    private String unit;

    @ApiModelProperty(value = "Sensor name [fi]")
    @JsonProperty(value = "name")
    private String nameFi;

    @ApiModelProperty(value = "Short name for sensor [fi]")
    @JsonProperty(value = "shortName")
    private String shortNameFi;

    @ApiModelProperty(value = "Sensor accuracy")
    private Integer accuracy;

    @ApiModelProperty("Possible additional descriptions for sensor values")
    private List<SensorValueDescription> sensorValueDescriptions;

    @ApiModelProperty(value = "Map of descriptions [fi, sv, en]")
    private Map<String, String> descriptions = new HashMap<>();

    @ApiModelProperty(value = "Map of presentation names [fi, sv, en]")
    private Map<String, String> presentationNames = new HashMap<>();

    public RoadStationSensorDto(long naturalId, String name, String unit, String descriptionFi, String descriptionSv, String descriptionEn,
                                String nameFi, String shortNameFi, Integer accuracy,
                                List<SensorValueDescription> sensorValueDescriptions, String presentationNameFi, String presentationNameSv,
                                String presentationNameEn) {
        this.naturalId = naturalId;
        this.name = name;
        this.unit = unit;
        this.nameFi = nameFi;
        this.shortNameFi = shortNameFi;
        this.accuracy = accuracy;
        this.sensorValueDescriptions = sensorValueDescriptions;
        addPresentationName("fi", presentationNameFi);
        addPresentationName("en", presentationNameEn);
        addPresentationName("sv", presentationNameSv);
        addDescription("fi", descriptionFi);
        addDescription("en", descriptionEn);
        addDescription("sv", descriptionSv);

    }

    public long getNaturalId() {
        return naturalId;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    @ApiModelProperty(value = "Sensor description [fi]")
    public String getDescription() {
        return descriptions.get("fi");
    }

    public String getNameFi() {
        return nameFi;
    }

    public String getShortNameFi() {
        return shortNameFi;
    }

    public Integer getAccuracy() {
        return accuracy;
    }

    public List<SensorValueDescription> getSensorValueDescriptions() {
        return sensorValueDescriptions;
    }

    public void addPresentationName(final String lang, final String name) {
        this.presentationNames.put(lang, name);
    }

    public void addDescription(final String lang, final String description) {
        this.descriptions.put(lang, description);
    }

    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    public Map<String, String> getPresentationNames() {
        return presentationNames;
    }
}
