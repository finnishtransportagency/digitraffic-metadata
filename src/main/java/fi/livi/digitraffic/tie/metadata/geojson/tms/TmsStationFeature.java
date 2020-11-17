package fi.livi.digitraffic.tie.metadata.geojson.tms;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON TmsStationFeature Object
 */
@ApiModel(description = "GeoJSON Feature Object", value = "TmsStationFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class TmsStationFeature extends Feature<Point, TmsStationProperties> {

    // TODO: Remove this from next version as it is duplicated in properties
    @ApiModelProperty(value = "Same as TmsStationProperties.roadStationId", required = true, position = 2)
    @JsonPropertyOrder(value = "2")
    public final long id;

    public TmsStationFeature(final Point geometry, final TmsStationProperties properties, long id) {
        super(geometry, properties);
        this.id = id;
    }

    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true, position = 3)
    @Override
    public Point getGeometry() {
        return super.getGeometry();
    }
}
