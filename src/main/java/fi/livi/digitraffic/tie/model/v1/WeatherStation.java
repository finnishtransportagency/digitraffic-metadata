package fi.livi.digitraffic.tie.model.v1;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.WeatherStationType;

@Entity
@DynamicUpdate
public class WeatherStation {

    @Id
    @NotNull
    @GenericGenerator(name = "SEQ_ROAD_WEATHER_STATION", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD_WEATHER_STATION"))
    @GeneratedValue(generator = "SEQ_ROAD_WEATHER_STATION")
    private Long id;

    @NotNull
    private Long lotjuId;

    @Enumerated(EnumType.STRING)
    private WeatherStationType weatherStationType;

    private boolean master;

    @OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="road_station_id", nullable = false)
    @Fetch(FetchMode.SELECT)
    private RoadStation roadStation;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(final Long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public RoadStation getRoadStation() {
        return roadStation;
    }

    public void setRoadStation(final RoadStation roadStation) {
        this.roadStation = roadStation;
    }

    public void obsolete() {
        roadStation.obsolete();
    }

    public WeatherStationType getWeatherStationType() {
        return weatherStationType;
    }

    public void setWeatherStationType(final WeatherStationType weatherStationType) {
        this.weatherStationType = weatherStationType;
    }

    public boolean isMaster() {
        return master;
    }

    public void setMaster(final boolean master) {
        this.master = master;
    }

    @Override
    public String toString() {
        return new ToStringHelper(this)
                .appendField("id", getId())
                .appendField("lotjuId", getLotjuId())
                .appendField("roadStationId", getRoadStationId())
                .appendField("roadStationNaturalId", getRoadStationNaturalId())
                .toString();
    }

    public Long getRoadStationId() {
        return roadStation != null ? roadStation.getId() : null;
    }

    public Long getRoadStationNaturalId() {
        return roadStation != null ? roadStation.getNaturalId() : null;
    }

}
