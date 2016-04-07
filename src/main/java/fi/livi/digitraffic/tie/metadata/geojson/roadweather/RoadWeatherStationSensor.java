package fi.livi.digitraffic.tie.metadata.geojson.roadweather;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Road Weather Station Sensors", value = "RoadWeatherStationSensor")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "name", "description", "sensorTypeId", "altitude" })
public class RoadWeatherStationSensor {

    @ApiModelProperty(value = "Road Weather Station Sensor unique id", position = 1)
    private long id;

    @ApiModelProperty(value = "Sensor altitude from ground [m]", position = 5)
    private Integer altitude;

    @ApiModelProperty(value = "Sensor description", position = 3)
    private String description;

    @ApiModelProperty(value = "Sensor name", position = 2)
    private String name;

    @ApiModelProperty(value = "Sensor type id", position = 4)
    private long sensorTypeId;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setAltitude(Integer altitude) {
        this.altitude = altitude;
    }

    public Integer getAltitude() {
        return altitude;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSensorTypeId(long sensorTypeId) {
        this.sensorTypeId = sensorTypeId;
    }

    public long getSensorTypeId() {
        return sensorTypeId;
    }
}
