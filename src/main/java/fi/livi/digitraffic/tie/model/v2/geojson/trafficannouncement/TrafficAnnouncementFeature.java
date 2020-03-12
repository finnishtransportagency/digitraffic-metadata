package fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Object", value = "TrafficAnnouncementFeature")
@JsonPropertyOrder({
    "type",
    "id",
    "geometry",
    "properties"
})
public class TrafficAnnouncementFeature implements Feature<Geometry> {

    @ApiModelProperty(value = "\"Feature\": GeoJSON Feature Object", required = true, position = 1, allowableValues = "Feature")
    @JsonPropertyOrder(value = "1")
    private final String type = "Feature";

    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true, position = 3)
    @JsonPropertyOrder(value = "3")
    private Geometry geometry;

    @ApiModelProperty(value = "Traffic Announcement properties", required = true, position = 4)
    @JsonPropertyOrder(value = "4")
    private TrafficAnnouncementProperties properties;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public TrafficAnnouncementProperties getProperties() {
        return properties;
    }

    public void setProperties(final TrafficAnnouncementProperties properties) {
        this.properties = properties;
    }

}
