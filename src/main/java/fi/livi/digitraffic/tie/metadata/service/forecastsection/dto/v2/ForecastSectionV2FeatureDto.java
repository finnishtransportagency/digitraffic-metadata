package fi.livi.digitraffic.tie.metadata.service.forecastsection.dto.v2;

public class ForecastSectionV2FeatureDto {

    private String type;

    private ForecastSectionV2Geometry geometry;

    private ForecastSectionV2Properties properties;

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

    public ForecastSectionV2Properties getProperties() {
        return properties;
    }

    public void setProperties(ForecastSectionV2Properties properties) {
        this.properties = properties;
    }
}
