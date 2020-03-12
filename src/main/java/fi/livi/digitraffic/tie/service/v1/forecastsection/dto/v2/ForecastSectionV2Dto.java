package fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2;

import java.time.ZonedDateTime;
import java.util.List;

public class ForecastSectionV2Dto {

    private String type;

    private ZonedDateTime dataUpdatedTime;

    private List<ForecastSectionV2FeatureDto> features;

    public ForecastSectionV2Dto() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ZonedDateTime getDataUpdatedTime() {
        return dataUpdatedTime;
    }

    public void setDataUpdatedTime(ZonedDateTime dataUpdatedTime) {
        this.dataUpdatedTime = dataUpdatedTime;
    }

    public List<ForecastSectionV2FeatureDto> getFeatures() {
        return features;
    }

    public void setFeatures(List<ForecastSectionV2FeatureDto> features) {
        this.features = features;
    }
}
