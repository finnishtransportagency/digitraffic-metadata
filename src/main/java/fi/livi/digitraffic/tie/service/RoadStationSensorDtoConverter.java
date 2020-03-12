package fi.livi.digitraffic.tie.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fi.livi.digitraffic.tie.dto.v1.TmsRoadStationSensorDto;
import fi.livi.digitraffic.tie.dto.v1.WeatherRoadStationSensorDto;
import fi.livi.digitraffic.tie.model.v1.RoadStationSensor;

public class RoadStationSensorDtoConverter {

    public static List<WeatherRoadStationSensorDto> convertWeatherSensors(List<RoadStationSensor> sensors) {
        return (List<WeatherRoadStationSensorDto>)sensors.stream().map(s -> new WeatherRoadStationSensorDto(
            s.getNaturalId(), s.getName(), s.getUnit(), s.getDescriptionFi(), s.getDescriptionSv(), s.getDescriptionEn(),
            s.getNameFi(), s.getShortNameFi(), s.getAccuracy(), new ArrayList(s.getSensorValueDescriptions()), s.getPresentationNameFi(),
            s.getPresentationNameSv(), s.getPresentationNameEn()))
            .collect(Collectors.toList());
    }

    public static List<TmsRoadStationSensorDto> convertTmsSensors(List<RoadStationSensor> sensors) {
        return (List<TmsRoadStationSensorDto>)sensors.stream().map(s -> new TmsRoadStationSensorDto(
            s.getNaturalId(), s.getName(), s.getUnit(), s.getDescriptionFi(), s.getDescriptionSv(), s.getDescriptionEn(),
            s.getNameFi(), s.getShortNameFi(), s.getAccuracy(), new ArrayList(s.getSensorValueDescriptions()), s.getPresentationNameFi(),
            s.getPresentationNameSv(), s.getPresentationNameEn(), s.getVehicleClass(), s.getLane(), s.getDirection()))
            .collect(Collectors.toList());
    }
}
