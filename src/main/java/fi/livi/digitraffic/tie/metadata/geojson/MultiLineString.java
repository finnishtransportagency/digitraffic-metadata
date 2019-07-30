package fi.livi.digitraffic.tie.metadata.geojson;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJson MultiLineString Geometry Object", value = "MultiLineStringGeometry", parent = Geometry.class)
@JsonPropertyOrder({ "type", "coordinates" })
public class MultiLineString extends Geometry<List<List<Double>>> {

    public MultiLineString() {
        super(Type.MultiLineString, new ArrayList<>());
    }

    public MultiLineString(final List<List<List<Double>>> coordinates) {
        super(Type.MultiLineString, coordinates);
    }

    @ApiModelProperty(required = true, allowableValues = "MultiLineString", example = "MultiLineString")
    @Override
    public Type getType() {
        return super.getType();
    }

    @ApiModelProperty(required = true, position = 2, example = "\"[ [100.0, 0.0], [101.0, 1.0] ], [ [102.0, 2.0], [103.0, 3.0] ]\"",
                      value = "Array of LineString coordinates [LONGITUDE, LATITUDE, {ALTITUDE}]. " +
                              "Coordinates are in WGS84 format in decimal degrees. Altitude is optional and measured in meters.",
                      dataType = "List")
    @Override
    public List<List<List<Double>>> getCoordinates() {
        return super.getCoordinates();
    }


    public void addLineString(final List<List<Double>> coordinates) {
        getCoordinates().add(coordinates);
    }
}
