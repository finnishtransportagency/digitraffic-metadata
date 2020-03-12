
package fi.livi.digitraffic.tie.model.v1.maintenance.harja;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "number",
    "startDistance",
    "startPart",
    "endDistance",
    "endPart",
    "carriageway",
    "lane",
    "side",
    "startDate",
    "endDate",
    "mapDate"
})
public class Road implements Serializable {

    private String name;

    @JsonProperty(required = true)
    private Integer number;

    /**
     * aet
     */
    @JsonProperty(required = true)
    private Integer startDistance;

    /**
     * aosa
     */
    @JsonProperty(required = true)
    private Integer startPart;

    /**
     * let
     */
    private Integer endDistance;

    /**
     * losa
     */
    private Integer endPart;

    /**
     * ajr
     */
    private Integer carriageway;

    /**
     * kaistta
     */
    private Lane lane;

    private Integer side;

    private ZonedDateTime startDate;

    private ZonedDateTime endDate;

    private ZonedDateTime mapDate;

    @JsonIgnore
    private final Map<String, Object> additionalProperties;

    @JsonCreator
    public Road(final String name, final Integer number, final Integer startDistance, final Integer startPart, final Integer endDistance,
                final Integer endPart, final Integer carriageway, final Lane lane, final Integer side, final ZonedDateTime startDate,
                final ZonedDateTime endDate, final ZonedDateTime mapDate, final Map<String, Object> additionalProperties) {
        this.name = name;
        this.number = number;
        this.startDistance = startDistance;
        this.startPart = startPart;
        this.endDistance = endDistance;
        this.endPart = endPart;
        this.carriageway = carriageway;
        this.lane = lane;
        this.side = side;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mapDate = mapDate;
        this.additionalProperties = additionalProperties;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(final Integer number) {
        this.number = number;
    }

    public Integer getStartDistance() {
        return startDistance;
    }

    public void setStartDistance(final Integer startDistance) {
        this.startDistance = startDistance;
    }

    public Integer getStartPart() {
        return startPart;
    }

    public void setStartPart(final Integer startPart) {
        this.startPart = startPart;
    }

    public Integer getEndDistance() {
        return endDistance;
    }

    public void setEndDistance(final Integer endDistance) {
        this.endDistance = endDistance;
    }

    public Integer getEndPart() {
        return endPart;
    }

    public void setEndPart(final Integer endPart) {
        this.endPart = endPart;
    }

    public Integer getCarriageway() {
        return carriageway;
    }

    public void setCarriageway(final Integer carriageway) {
        this.carriageway = carriageway;
    }

    public Lane getLane() {
        return lane;
    }

    public void setLane(final Lane lane) {
        this.lane = lane;
    }

    public Integer getSide() {
        return side;
    }

    public void setSide(final Integer side) {
        this.side = side;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(final ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(final ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public ZonedDateTime getMapDate() {
        return mapDate;
    }

    public void setMapDate(final ZonedDateTime mapDate) {
        this.mapDate = mapDate;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(final String name, final Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Road)) {
            return false;
        }

        Road road = (Road) o;

        return new EqualsBuilder()
            .append(getName(), road.getName())
            .append(getNumber(), road.getNumber())
            .append(getStartDistance(), road.getStartDistance())
            .append(getStartPart(), road.getStartPart())
            .append(getEndDistance(), road.getEndDistance())
            .append(getEndPart(), road.getEndPart())
            .append(getCarriageway(), road.getCarriageway())
            .append(getLane(), road.getLane())
            .append(getSide(), road.getSide())
            .append(getStartDate(), road.getStartDate())
            .append(getEndDate(), road.getEndDate())
            .append(getMapDate(), road.getMapDate())
            .append(getAdditionalProperties(), road.getAdditionalProperties())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getName())
            .append(getNumber())
            .append(getStartDistance())
            .append(getStartPart())
            .append(getEndDistance())
            .append(getEndPart())
            .append(getCarriageway())
            .append(getLane())
            .append(getSide())
            .append(getStartDate())
            .append(getEndDate())
            .append(getMapDate())
            .append(getAdditionalProperties())
            .toHashCode();
    }
}