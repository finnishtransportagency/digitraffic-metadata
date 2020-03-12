package fi.livi.digitraffic.tie.dto.v1;

import java.util.List;

import fi.livi.digitraffic.tie.model.v1.SensorValueDescription;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "Weather road station sensor", value = "WeatherRoadStationSensor")
public class WeatherRoadStationSensorDto extends RoadStationSensorDto {
    public WeatherRoadStationSensorDto(long naturalId, String name, String unit, String descriptionFi, String descriptionSv, String descriptionEn,
                                       String nameFi, String shortNameFi, Integer accuracy,
                                       List<SensorValueDescription> sensorValueDescriptions,
                                       String presentationNameFi, String presentationNameSv, String presentationNameEn) {
        super(naturalId, name, unit, descriptionFi, descriptionSv, descriptionEn, nameFi, shortNameFi, accuracy, sensorValueDescriptions,
              presentationNameFi, presentationNameSv, presentationNameEn);
    }
}
