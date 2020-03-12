package fi.livi.digitraffic.tie.metadata.geojson.weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.roadstation.RoadStationProperties;
import fi.livi.digitraffic.tie.model.WeatherStationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Weather Station properties", value = "WeatherStationProperties", parent = RoadStationProperties.class)
@JsonPropertyOrder({ "roadStationId", "weatherStationType", "naturalId", "name" })
public class WeatherStationProperties extends RoadStationProperties {

    @JsonIgnore // Using road station's natural id
    @ApiModelProperty(name = "id", value = "Weather station's unique id", required = true)
    private long id;

    @ApiModelProperty(value = "Type of Weather Station")
    private WeatherStationType weatherStationType;

    @ApiModelProperty(value = "Is station master or slave station")
    private Boolean master;

    /** Sensors natural ids */
    @ApiModelProperty(value = "Weather Station Sensors ids")
    private List<Long> stationSensors = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setWeatherStationType(final WeatherStationType weatherStationType) {
        this.weatherStationType = weatherStationType;
    }

    public WeatherStationType getWeatherStationType() {
        return weatherStationType;
    }

    public List<Long> getStationSensors() {
        return stationSensors;
    }

    public void setStationSensors(List<Long> stationSensors) {
        this.stationSensors = stationSensors;
        Collections.sort(this.stationSensors);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final WeatherStationProperties rhs = (WeatherStationProperties) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.id, rhs.id)
                .append(this.weatherStationType, rhs.weatherStationType)
                .append(this.stationSensors, rhs.stationSensors)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(id)
                .append(weatherStationType)
                .append(stationSensors)
                .toHashCode();
    }

    public void setMaster(final Boolean master) {
        this.master = master;
    }

    public Boolean isMaster() {
        return master;
    }

    /** This field is only for TMS and Camera -stations */
    @JsonIgnore
    @Override
    public void setPurpose(final String purpose) {
        if (purpose != null) {
            throw new UnsupportedOperationException("Available only for TMS and Camera stations");
        }
    }
}
