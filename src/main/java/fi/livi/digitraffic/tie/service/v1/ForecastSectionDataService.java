package fi.livi.digitraffic.tie.service.v1;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.dao.v1.ForecastSectionWeatherDao;
import fi.livi.digitraffic.tie.dto.v1.forecast.ForecastSectionWeatherDataDto;
import fi.livi.digitraffic.tie.dto.v1.forecast.ForecastSectionWeatherRootDto;
import fi.livi.digitraffic.tie.dto.v1.forecast.RoadConditionDto;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.IllegalArgumentException;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionApiVersion;

@Service
public class ForecastSectionDataService {

    private final ForecastSectionWeatherDao forecastSectionWeatherDao;
    private final DataStatusService dataStatusService;

    @Autowired
    public ForecastSectionDataService(final ForecastSectionWeatherDao forecastSectionWeatherDao,
                                      final DataStatusService dataStatusService) {
        this.forecastSectionWeatherDao = forecastSectionWeatherDao;
        this.dataStatusService = dataStatusService;
    }

    public ForecastSectionWeatherRootDto getForecastSectionWeatherData(final ForecastSectionApiVersion version, final boolean onlyUpdateInfo,
                                                                       final Integer roadNumber,
                                                                       final Double minLongitude, final Double minLatitude,
                                                                       final Double maxLongitude, final Double maxLatitude,
                                                                       final List<String> naturalIds) {
        final ZonedDateTime updatedTime = dataStatusService.findDataUpdatedTime(getDataType(version));

        if (onlyUpdateInfo) {
            return new ForecastSectionWeatherRootDto(updatedTime);
        }

        final Map<String, List<RoadConditionDto>> forecastSectionWeatherData =
            forecastSectionWeatherDao.getForecastSectionWeatherData(version, roadNumber, minLongitude, minLatitude, maxLongitude, maxLatitude, naturalIds);

        return new ForecastSectionWeatherRootDto(
                updatedTime,
                getWeatherData(forecastSectionWeatherData));
    }

    private List<ForecastSectionWeatherDataDto> getWeatherData(final Map<String, List<RoadConditionDto>> forecastSectionWeatherData) {
        return forecastSectionWeatherData.entrySet().stream()
            .map(w -> new ForecastSectionWeatherDataDto(w.getKey(), w.getValue()))
            .collect(Collectors.toList());
    }

    public static DataType getDataType(final ForecastSectionApiVersion version) {
        switch (version) {
            case V1: return DataType.FORECAST_SECTION_WEATHER_DATA;
            case V2: return DataType.FORECAST_SECTION_V2_WEATHER_DATA;
            default: throw new IllegalArgumentException("Unknown ForecastSectionApiVersion " + version);
        }
    }
}
