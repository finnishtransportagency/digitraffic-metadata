package fi.livi.digitraffic.tie.data.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.data.dto.weather.WeatherRootDataObjectDto;
import fi.livi.digitraffic.tie.data.dto.weather.WeatherStationDto;
import fi.livi.digitraffic.tie.metadata.dao.RoadStationRepository;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.service.roadstationsensor.RoadStationSensorService;

@Service
public class WeatherService {

    private final RoadStationSensorService roadStationSensorService;
    private final RoadStationRepository roadStationRepository;

    @Autowired
    public WeatherService(final RoadStationSensorService roadStationSensorService,
                          final RoadStationRepository roadStationRepository) {
        this.roadStationSensorService = roadStationSensorService;
        this.roadStationRepository = roadStationRepository;
    }

    @Transactional(readOnly = true)
    public WeatherRootDataObjectDto findPublishableWeatherData(final boolean onlyUpdateInfo) {

        final ZonedDateTime updated = roadStationSensorService.getLatestMeasurementTime(RoadStationType.WEATHER_STATION);

        if (onlyUpdateInfo) {
            return new WeatherRootDataObjectDto(updated);
        } else {

            final Map<Long, List<SensorValueDto>> values =
                    roadStationSensorService.findAllPublishableRoadStationSensorValuesMappedByNaturalId(RoadStationType.WEATHER_STATION);
            final List<WeatherStationDto> stations = new ArrayList<>();
            for (final Map.Entry<Long, List<SensorValueDto>> entry : values.entrySet()) {
                final WeatherStationDto dto = new WeatherStationDto();
                stations.add(dto);
                dto.setRoadStationNaturalId(entry.getKey());
                dto.setSensorValues(entry.getValue());
                dto.setMeasuredTime(SensorValueDto.getStationLatestMeasurement(dto.getSensorValues()));
            }

            return new WeatherRootDataObjectDto(stations, updated);
        }
    }

    @Transactional(readOnly = true)
    public WeatherRootDataObjectDto findPublishableWeatherData(final long roadStationNaturalId) {

        if ( !roadStationRepository.isPublishableRoadStation(roadStationNaturalId, RoadStationType.WEATHER_STATION) ) {
            throw new ObjectNotFoundException("WeatherStation", roadStationNaturalId);
        }

        final ZonedDateTime updated = roadStationSensorService.getLatestMeasurementTime(RoadStationType.WEATHER_STATION);

        final List<SensorValueDto> values =
                roadStationSensorService.findAllPublishableRoadStationSensorValues(roadStationNaturalId,
                                                                                         RoadStationType.WEATHER_STATION);

        final WeatherStationDto dto = new WeatherStationDto();
        dto.setRoadStationNaturalId(roadStationNaturalId);
        dto.setSensorValues(values);
        dto.setMeasuredTime(SensorValueDto.getStationLatestMeasurement(dto.getSensorValues()));

        return new WeatherRootDataObjectDto(Collections.singletonList(dto), updated);
    }
}
