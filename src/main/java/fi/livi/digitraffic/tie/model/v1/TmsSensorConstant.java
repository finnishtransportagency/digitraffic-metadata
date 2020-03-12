package fi.livi.digitraffic.tie.model.v1;

import java.time.LocalDate;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
public class TmsSensorConstant {

    @Id
    private Long lotjuId;

    @NotNull
    private String name;

    @NotNull
    private LocalDate updated;

    private LocalDate obsoleteDate;
    /**
     * RoadStation is same for multiple constants
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="ROAD_STATION_ID")
    @Fetch(FetchMode.SELECT)
    private RoadStation roadStation;

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(Long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getUpdated() {
        return updated;
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    public RoadStation getRoadStation() {
        return roadStation;
    }

    public void setRoadStation(RoadStation roadStation) {
        this.roadStation = roadStation;
    }

    @Override
    public String toString() {
        return ToStringHelper.toString(this);
    }
}
