package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson LineString Geometry Object", value = "LineStringGeometry", parent = Geometry.class)
@JsonPropertyOrder({ "type", "coordinates"})
public class LineString extends Geometry<List<Double>> {

    @JsonCreator
    public LineString(List<List<Double>> coordinates) {
        super(Type.LineString, coordinates);
    }

    @ApiModelProperty(required = true, allowableValues = "LineString", example = "LineString")
    @Override
    public Type getType() {
        return super.getType();
    }

    @ApiModelProperty(required = true, position = 2, example = "[[26.976774926733796, 65.34673850731987], [26.984330656240413, 65.35836767060651]]",
        value = "List of coordinates [[LONGITUDE, LATITUDE, {ALTITUDE}], [LONGITUDE, LATITUDE, {ALTITUDE}]]. " +
                "Coordinates are in WGS84 format in decimal degrees. Altitude is optional and measured in meters.",
                      dataType = "List")
    @Override
    public List<List<Double>> getCoordinates() {
        return super.getCoordinates();
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this );
    }
}
