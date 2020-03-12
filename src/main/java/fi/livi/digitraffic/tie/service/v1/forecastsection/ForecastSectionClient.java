package fi.livi.digitraffic.tie.service.v1.forecastsection;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v1.ForecastSectionCoordinatesDto;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v1.ForecastSectionCoordinatesEntry;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2.ForecastSectionV2Dto;

@Component
public class ForecastSectionClient {

    @Value("${roadConditions.baseUrl}")
    private String baseUrl;

    private static final String roadsUrl = "roads.php";

    private static final String roadsV2Url = "roadsV2.php";

    private static final String roadConditionsV1Url = "roadConditionsV1-json.php";

    private static final String roadConditionsV2Url = "roadConditionsV2-json.php";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final RestTemplate restTemplate;

    @Autowired
    public ForecastSectionClient(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable
    public List<ForecastSectionCoordinatesDto> getForecastSectionV1Metadata() {

        final LinkedHashMap<String, Object> response = restTemplate.getForObject(baseUrl + roadsUrl, LinkedHashMap.class);

        return response.entrySet().stream().map(this::mapForecastSectionCoordinates).collect(Collectors.toList());
    }

    @Retryable
    public ForecastSectionDataDto getRoadConditions(final int version) {
        if (version == 1) {
            return restTemplate.getForObject(baseUrl + roadConditionsV1Url, ForecastSectionDataDto.class);
        } else {
            return restTemplate.getForObject(baseUrl + roadConditionsV2Url, ForecastSectionDataDto.class);
        }
    }

    protected ForecastSectionCoordinatesDto mapForecastSectionCoordinates(final Map.Entry<String, Object> forecastSection) {
        final ForecastSectionCoordinatesEntry entry = objectMapper.convertValue(forecastSection.getValue(), ForecastSectionCoordinatesEntry.class);

        return new ForecastSectionCoordinatesDto(forecastSection.getKey(), entry.getName(), entry.getCoordinates());
    }

    public ForecastSectionV2Dto getForecastSectionV2Metadata() {

        return restTemplate.getForObject(baseUrl + roadsV2Url, ForecastSectionV2Dto.class);
    }
}
