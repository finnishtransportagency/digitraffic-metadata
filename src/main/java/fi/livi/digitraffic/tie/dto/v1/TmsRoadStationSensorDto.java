package fi.livi.digitraffic.tie.dto.v1;

import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import fi.livi.digitraffic.tie.model.v1.SensorValueDescription;
import fi.livi.digitraffic.tie.model.VehicleClass;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "TMS road station sensor")
public class TmsRoadStationSensorDto extends RoadStationSensorDto {

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

    public TmsRoadStationSensorDto(long naturalId, String name, String unit, String descriptionFi, String descriptionSv, String descriptionEn,
                                   String nameFi, String shortNameFi, Integer accuracy,
                                   List<SensorValueDescription> sensorValueDescriptions,
                                   String presentationNameFi, String presentationNameSv, String presentationNameEn,
                                   VehicleClass vehicleClass, Integer lane, Integer direction) {
        super(naturalId, name, unit, descriptionFi, descriptionSv, descriptionEn, nameFi, shortNameFi, accuracy, sensorValueDescriptions,
              presentationNameFi, presentationNameSv, presentationNameEn);
        this.vehicleClass = vehicleClass;
        this.lane = lane;
        this.direction = direction;
    }

    public VehicleClass getVehicleClass() {
        return vehicleClass;
    }

    public Integer getLane() {
        return lane;
    }

    public Integer getDirection() {
        return direction;
    }

}
