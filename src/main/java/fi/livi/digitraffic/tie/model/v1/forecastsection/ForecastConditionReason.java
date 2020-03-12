package fi.livi.digitraffic.tie.model.v1.forecastsection;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@DynamicUpdate
public class ForecastConditionReason {
    @EmbeddedId
    private ForecastSectionWeatherPK forecastSectionWeatherPK;

    @Enumerated(EnumType.STRING)
    private PrecipitationCondition precipitationCondition;

    @Enumerated(EnumType.STRING)
    private RoadCondition roadCondition;

    @Enumerated(EnumType.STRING)
    private WindCondition windCondition;

    private Boolean freezingRainCondition;

    private Boolean winterSlipperiness;

    @Enumerated(EnumType.STRING)
    private VisibilityCondition visibilityCondition;

    @Enumerated(EnumType.STRING)
    private FrictionCondition frictionCondition;

    @OneToOne
    @JoinColumns({
            @JoinColumn(name="forecast_section_id", referencedColumnName="forecast_section_id"),
            @JoinColumn(name="forecast_name", referencedColumnName="forecast_name")
    })
    @Fetch(FetchMode.JOIN)
    private ForecastSectionWeather forecastSectionWeather;

    public ForecastConditionReason() {
    }

    public ForecastConditionReason(ForecastSectionWeatherPK forecastSectionWeatherPK,
                                   PrecipitationCondition precipitationCondition,
                                   RoadCondition roadCondition,
                                   WindCondition windCondition,
                                   Boolean freezingRainCondition,
                                   Boolean winterSlipperiness,
                                   VisibilityCondition visibilityCondition,
                                   FrictionCondition frictionCondition) {
        this.forecastSectionWeatherPK = forecastSectionWeatherPK;
        this.precipitationCondition = precipitationCondition;
        this.roadCondition = roadCondition;
        this.windCondition = windCondition;
        this.freezingRainCondition = freezingRainCondition;
        this.winterSlipperiness = winterSlipperiness;
        this.visibilityCondition = visibilityCondition;
        this.frictionCondition = frictionCondition;
    }

    public ForecastSectionWeatherPK getForecastSectionWeatherPK() {
        return forecastSectionWeatherPK;
    }

    public void setForecastSectionWeatherPK(ForecastSectionWeatherPK forecastSectionWeatherPK) {
        this.forecastSectionWeatherPK = forecastSectionWeatherPK;
    }

    public PrecipitationCondition getPrecipitationCondition() {
        return precipitationCondition;
    }

    public void setPrecipitationCondition(PrecipitationCondition precipitationCondition) {
        this.precipitationCondition = precipitationCondition;
    }

    public RoadCondition getRoadCondition() {
        return roadCondition;
    }

    public void setRoadCondition(RoadCondition roadCondition) {
        this.roadCondition = roadCondition;
    }

    public WindCondition getWindCondition() {
        return windCondition;
    }

    public void setWindCondition(WindCondition windCondition) {
        this.windCondition = windCondition;
    }

    public Boolean getFreezingRainCondition() {
        return freezingRainCondition;
    }

    public void setFreezingRainCondition(Boolean freezingRainCondition) {
        this.freezingRainCondition = freezingRainCondition;
    }

    public Boolean getWinterSlipperiness() {
        return winterSlipperiness;
    }

    public void setWinterSlipperiness(Boolean winterSlipperiness) {
        this.winterSlipperiness = winterSlipperiness;
    }

    public VisibilityCondition getVisibilityCondition() {
        return visibilityCondition;
    }

    public void setVisibilityCondition(VisibilityCondition visibilityCondition) {
        this.visibilityCondition = visibilityCondition;
    }

    public FrictionCondition getFrictionCondition() {
        return frictionCondition;
    }

    public void setFrictionCondition(FrictionCondition frictionCondition) {
        this.frictionCondition = frictionCondition;
    }

    public ForecastSectionWeather getForecastSectionWeather() {
        return forecastSectionWeather;
    }

    public void setForecastSectionWeather(ForecastSectionWeather forecastSectionWeather) {
        this.forecastSectionWeather = forecastSectionWeather;
    }
}
