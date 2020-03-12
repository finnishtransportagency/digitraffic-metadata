package fi.livi.digitraffic.tie.model.v1.forecastsection;

import java.sql.Timestamp;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@DynamicUpdate
public class ForecastSectionWeather {
    @EmbeddedId
    private ForecastSectionWeatherPK forecastSectionWeatherPK;

    private Timestamp time;

    private Boolean daylight;

    @Enumerated(EnumType.STRING)
    private OverallRoadCondition overallRoadCondition;

    @Enumerated(EnumType.STRING)
    private Reliability reliability;

    private String roadTemperature;

    private String temperature;

    private String weatherSymbol;

    private Integer windDirection;

    private Double windSpeed;

    @Column(insertable = false, updatable = false)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="forecast_section_id", nullable = false, referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    private ForecastSection forecastSection;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "forecastSectionWeather")
    @PrimaryKeyJoinColumns({@PrimaryKeyJoinColumn(name="forecast_section_id", referencedColumnName = "forecast_section_id"),
                            @PrimaryKeyJoinColumn(name="forecast_name", referencedColumnName = "forecast_name")})
    @Fetch(FetchMode.JOIN)
    private ForecastConditionReason forecastConditionReason;

    public ForecastSectionWeather() {
    }

    public ForecastSectionWeather(ForecastSectionWeatherPK forecastSectionWeatherPK, Timestamp time, Boolean daylight, OverallRoadCondition overallRoadCondition,
                                  Reliability reliability, String roadTemperature, String temperature, String weatherSymbol, Integer windDirection,
                                  Double windSpeed, ForecastConditionReason forecastConditionReason) {
        this.forecastSectionWeatherPK = forecastSectionWeatherPK;
        this.time = time;
        this.daylight = daylight;
        this.overallRoadCondition = overallRoadCondition;
        this.reliability = reliability;
        this.roadTemperature = roadTemperature;
        this.temperature = temperature;
        this.weatherSymbol = weatherSymbol;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.forecastConditionReason = forecastConditionReason;
    }

    public ForecastSectionWeatherPK getForecastSectionWeatherPK() {
        return forecastSectionWeatherPK;
    }

    @JsonIgnore
    public long getForecastSectionId() {
        return forecastSectionWeatherPK.getForecastSectionId();
    }

    public String getForecastName() {
        return new String(forecastSectionWeatherPK.getForecastName());
    }

    public void setForecastSectionWeatherPK(ForecastSectionWeatherPK forecastSectionWeatherPK) {
        this.forecastSectionWeatherPK = forecastSectionWeatherPK;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public Boolean getDaylight() {
        return daylight;
    }

    public void setDaylight(Boolean daylight) {
        this.daylight = daylight;
    }

    public OverallRoadCondition getOverallRoadCondition() {
        return overallRoadCondition;
    }

    public void setOverallRoadCondition(OverallRoadCondition overallRoadCondition) {
        this.overallRoadCondition = overallRoadCondition;
    }

    public Reliability getReliability() {
        return reliability;
    }

    public void setReliability(Reliability reliability) {
        this.reliability = reliability;
    }

    public String getRoadTemperature() {
        return roadTemperature;
    }

    public void setRoadTemperature(String roadTemperature) {
        this.roadTemperature = roadTemperature;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getWeatherSymbol() {
        return weatherSymbol;
    }

    public void setWeatherSymbol(String weatherSymbol) {
        this.weatherSymbol = weatherSymbol;
    }

    public Integer getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(Integer windDirection) {
        this.windDirection = windDirection;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getType() {
        return type;
    }

    public ForecastSection getForecastSection() {
        return forecastSection;
    }

    public void setForecastSection(ForecastSection forecastSection) {
        this.forecastSection = forecastSection;
    }

    public ForecastConditionReason getForecastConditionReason() {
        return forecastConditionReason;
    }

    public void setForecastConditionReason(ForecastConditionReason forecastConditionReason) {
        this.forecastConditionReason = forecastConditionReason;
    }
}
