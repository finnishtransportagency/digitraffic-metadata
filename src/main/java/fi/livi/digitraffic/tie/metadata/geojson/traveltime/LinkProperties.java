package fi.livi.digitraffic.tie.metadata.geojson.traveltime;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.model.Direction;
import fi.livi.digitraffic.tie.metadata.model.RoadDistrict;
import fi.livi.digitraffic.tie.metadata.model.Site;
import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({ "id", "nameFi", "nameSv", "nameEn", "sites", "summerFreeFlowSpeed", "winterFreeFlowSpeed",
                     "length", "roadDistrict", "linkDirection" })
public class LinkProperties {

    @ApiModelProperty("Link id")
    public final Long id;

    @ApiModelProperty("Link name in Finnish")
    public final String nameFi;

    @ApiModelProperty("Link name in Swedish")
    public final String nameSv;

    @ApiModelProperty("Link name in English")
    public final String nameEn;

    @ApiModelProperty("List of sites (i.e. camera locations) along the link in order. First site is the starting point of a link.")
    public final List<Site> sites;

    @ApiModelProperty("Length of the link in meters (m)")
    public final Long length;

    @ApiModelProperty("Typical speed at which vehicles travel along the link when there is no congestion")
    public final BigDecimal summerFreeFlowSpeed;

    @ApiModelProperty("Typical speed at which vehicles travel along the link when there is no congestion")
    public final BigDecimal winterFreeFlowSpeed;

    @ApiModelProperty("Road district where the link is located")
    public final RoadDistrict roadDistrict;

    @ApiModelProperty("Direction of the link")
    public final Direction linkDirection;

    public LinkProperties(final Long id, final List<Site> sites, final String name, final String nameSv, final String nameEn,
                          final Long length, final BigDecimal summerFreeFlowSpeed, final BigDecimal winterFreeFlowSpeed,
                          final RoadDistrict roadDistrict, final Direction linkDirection) {
        this.id = id;
        this.sites = sites;
        this.nameFi = name;
        this.nameSv = nameSv;
        this.nameEn = nameEn;
        this.length = length;
        this.summerFreeFlowSpeed = summerFreeFlowSpeed;
        this.winterFreeFlowSpeed = winterFreeFlowSpeed;
        this.roadDistrict = roadDistrict;
        this.linkDirection = linkDirection;
    }
}