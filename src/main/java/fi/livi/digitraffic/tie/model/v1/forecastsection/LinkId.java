package fi.livi.digitraffic.tie.model.v1.forecastsection;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class LinkId {

    @JsonIgnore
    @EmbeddedId
    private LinkIdPK linkIdPK;

    private Long linkId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="forecast_section_id", nullable = false, referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    private ForecastSection forecastSection;

    public LinkId() {
    }

    public LinkId(LinkIdPK linkIdPK, Long linkId, ForecastSection forecastSection) {
        this.linkIdPK = linkIdPK;
        this.linkId = linkId;
        this.forecastSection = forecastSection;
    }

    public LinkIdPK getLinkIdPK() {
        return linkIdPK;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(final Long linkId) {
        this.linkId = linkId;
    }

    public ForecastSection getForecastSection() {
        return forecastSection;
    }
}
