package fi.livi.digitraffic.tie.model.v1.location;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@IdClass(LocationKey.class)
public class Location {
    @Id
    private Integer locationCode;
    @Id
    private String version;

    private String subtypeCode;
    private String roadJunction;
    private String roadName;
    private String firstName;
    private String secondName;

    private Integer areaRef;
    private Integer linearRef;

    private Integer negOffset;
    private Integer posOffset;

    private Boolean urban;

    @Column(name = "wgs84_lat")
    private BigDecimal wgs84Lat;
    @Column(name = "wgs84_long")
    private BigDecimal wgs84Long;

    @Column(name = "etrs_tm35fin_x")
    private BigDecimal etrsTm35FinX;
    @Column(name = "etrs_tm35fin_y")
    private BigDecimal etrsTm35FixY;

    private String negDirection;
    private String posDirection;

    private String geocode;
    private Integer orderOfPoint;

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public Integer getNegOffset() {
        return negOffset;
    }

    public void setNegOffset(Integer negOffset) {
        this.negOffset = negOffset;
    }

    public Integer getPosOffset() {
        return posOffset;
    }

    public void setPosOffset(Integer posOffset) {
        this.posOffset = posOffset;
    }

    public Boolean getUrban() {
        return urban;
    }

    public void setUrban(Boolean urban) {
        this.urban = urban;
    }

    public BigDecimal getWgs84Lat() {
        return wgs84Lat;
    }

    public void setWgs84Lat(BigDecimal wgs84Lat) {
        this.wgs84Lat = wgs84Lat;
    }

    public BigDecimal getWgs84Long() {
        return wgs84Long;
    }

    public void setWgs84Long(BigDecimal wgs84Long) {
        this.wgs84Long = wgs84Long;
    }

    public String getRoadJunction() {
        return roadJunction;
    }

    public void setRoadJunction(String roadJunction) {
        this.roadJunction = roadJunction;
    }

    public String getNegDirection() {
        return negDirection;
    }

    public void setNegDirection(String negDirection) {
        this.negDirection = negDirection;
    }

    public String getPosDirection() {
        return posDirection;
    }

    public void setPosDirection(String posDirection) {
        this.posDirection = posDirection;
    }

    public String getGeocode() {
        return geocode;
    }

    public void setGeocode(String geocode) {
        this.geocode = geocode;
    }

    public Integer getOrderOfPoint() {
        return orderOfPoint;
    }

    public void setOrderOfPoint(Integer orderOfPoint) {
        this.orderOfPoint = orderOfPoint;
    }

    public BigDecimal getEtrsTm35FinX() {
        return etrsTm35FinX;
    }

    public void setEtrsTm35FinX(BigDecimal etrsTm35FinX) {
        this.etrsTm35FinX = etrsTm35FinX;
    }

    public BigDecimal getEtrsTm35FixY() {
        return etrsTm35FixY;
    }

    public void setEtrsTm35FixY(BigDecimal etrsTm35FixY) {
        this.etrsTm35FixY = etrsTm35FixY;
    }

    public Integer getLocationCode() {
        return locationCode;
    }

    public String getSubtypeCode() {
        return subtypeCode;
    }

    public void setSubtypeCode(String subtypeCode) {
        this.subtypeCode = subtypeCode;
    }

    public Integer getAreaRef() {
        return areaRef;
    }

    public void setAreaRef(Integer areaRef) {
        this.areaRef = areaRef;
    }

    public Integer getLinearRef() {
        return linearRef;
    }

    public void setLinearRef(Integer linearRef) {
        this.linearRef = linearRef;
    }

    public void setLocationCode(Integer locationCode) {
        this.locationCode = locationCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
