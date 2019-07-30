package fi.livi.digitraffic.tie.metadata.geojson;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Point.class, name = "Point"),
    @JsonSubTypes.Type(value = LineString.class, name = "LineString"),
    @JsonSubTypes.Type(value = MultiLineString.class, name = "MultiLineString")
})
@ApiModel(description = "GeoJson Geometry Object", value = "Geometry", discriminator = "type", subTypes = {Point.class, LineString.class, MultiLineString.class})
@JsonPropertyOrder({ "type", "coordinates"})
public abstract class Geometry<T> implements Serializable {

    private Type type;

    private final List<T> coordinates;


    public Geometry(final Type type, final List<T> coordinates) {
        this.type = type;
        this.coordinates = coordinates;
    }

    public Geometry(final Type type, final T...coordinates) {
        this.type = type;
        this.coordinates = Arrays.asList(coordinates);
    }

    @ApiModelProperty(value = "GeoJson Geometry Object type", required = true, position = 1, allowableValues = "Point,LineString,Polygon,MultiPoint,MultiLineString,MultiPolygon")
    public Type getType() {
        return type;
    }

    @ApiModelProperty(value = "GeoJson Geometry Object coordinates", required = true, position = 2)
    public List<T> getCoordinates() {
        return coordinates;
    }

    public enum Type {
        Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Geometry)) {
            return false;
        }

        Geometry<?> geometry = (Geometry<?>) o;

        return new EqualsBuilder()
            .append(getType(), geometry.getType())
            .append(getCoordinates(), geometry.getCoordinates())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getType())
            .append(getCoordinates())
            .toHashCode();
    }
}
