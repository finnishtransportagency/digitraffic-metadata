package fi.livi.digitraffic.tie.service.v1.forecastsection.dto.v2;

public class ForecastSectionV2FeatureDto {

    private String type;

    private ForecastSectionV2Geometry geometry;

    private ForecastSectionV2PropertiesDto properties;

    public ForecastSectionV2FeatureDto() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ForecastSectionV2Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(ForecastSectionV2Geometry geometry) {
        this.geometry = geometry;
    }

    public ForecastSectionV2PropertiesDto getProperties() {
        return properties;
    }

    public void setProperties(ForecastSectionV2PropertiesDto properties) {
        this.properties = properties;
    }
}
