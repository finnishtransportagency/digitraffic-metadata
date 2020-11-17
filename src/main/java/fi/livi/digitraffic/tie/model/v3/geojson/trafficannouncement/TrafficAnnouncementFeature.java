package fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "TrafficAnnouncement GeoJSON Feature Object", value = "TrafficAnnouncementFeatureV3")
@JsonPropertyOrder({
    "type",
    "id",
    "geometry",
    "properties"
})
public class TrafficAnnouncementFeature extends Feature<Geometry<?>, TrafficAnnouncementProperties> {
    public TrafficAnnouncementFeature(final Geometry<?> geometry,
                                      final TrafficAnnouncementProperties properties) {
        super(geometry, properties);
    }
}
