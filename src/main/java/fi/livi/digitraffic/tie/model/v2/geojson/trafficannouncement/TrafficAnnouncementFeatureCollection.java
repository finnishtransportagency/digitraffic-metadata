package fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootMetadataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of Traffic Announcements", value = "TrafficAnnouncementFeatureCollection")
@JsonPropertyOrder({
    "type",
    "dataUpdatedTime",
    "dataLastCheckedTime",
    "features"
})
public class TrafficAnnouncementFeatureCollection extends RootMetadataObjectDto implements Iterable<TrafficAnnouncementFeature> {

    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    public final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    private List<TrafficAnnouncementFeature> features;

    public TrafficAnnouncementFeatureCollection(final ZonedDateTime dataUpdatedTime, final ZonedDateTime dataLastCheckedTime, final List<TrafficAnnouncementFeature> features) {
        super(dataUpdatedTime, dataLastCheckedTime);
        this.features = features;
    }

    public String getType() {
        return type;
    }

    public List<TrafficAnnouncementFeature> getFeatures() {
        return features;
    }

    public void setFeatures(final List<TrafficAnnouncementFeature> features) {
        this.features = features;
    }

    public TrafficAnnouncementFeatureCollection add(final TrafficAnnouncementFeature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(final Collection<TrafficAnnouncementFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<TrafficAnnouncementFeature> iterator() {
        return features.iterator();
    }

    @Override
    public String toString() {
        return "TrafficAnnouncementFeatureCollection{" + "features=" + features + '}';
    }
}
