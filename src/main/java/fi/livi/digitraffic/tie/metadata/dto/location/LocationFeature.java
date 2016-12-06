package fi.livi.digitraffic.tie.metadata.dto.location;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class LocationFeature {
    public final String type = "Feature";

    @ApiModelProperty(value = "Unique locationCode for this location", required = true)
    public final int id;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true)
    public final Point geometry;

    @ApiModelProperty(value = "Location properties.", required = true)
    public final LocationProperties properties;

    public LocationFeature(final LocationJson l) {
        this.id = l.getLocationCode();
        this.geometry = getGeometry(l);
        this.properties = new LocationProperties(
                l.getSubtypeCode(),
                l.getRoadJunction(),
                l.getRoadName(), l.getFirstName(), l.getSecondName(), l.getAreaRef(),
                l.getLinearRef(), l.getNegOffset(), l.getPosOffset(), l.getUrban(),
                getEtrsGeometry(l),
                l.getNegDirection(), l.getPosDirection(),
                l.getGeocode(), l.getOrderOfPoint());
    }

    private static Point getGeometry(final LocationJson l) {
        return l.getWgs84Lat() == null ? null : new Point(l.getWgs84Lat().doubleValue(), l.getWgs84Long().doubleValue());
    }

    private static List<Double> getEtrsGeometry(final LocationJson l) {
        return l.getEtrsTm35FinX() == null ? null : Arrays.asList(l.getEtrsTm35FinX().doubleValue(), l.getEtrsTm35FixY().doubleValue());
    }
}