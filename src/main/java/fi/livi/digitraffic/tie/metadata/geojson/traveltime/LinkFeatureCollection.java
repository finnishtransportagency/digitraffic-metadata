package fi.livi.digitraffic.tie.metadata.geojson.traveltime;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of travel time links", value = "LinkFeatureCollection")
@JsonPropertyOrder({ "type", "features" })
public class LinkFeatureCollection extends RootDataObjectDto implements Iterable<LinkFeature> {

    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    public final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    public final List<LinkFeature> features;

    public LinkFeatureCollection(final ZonedDateTime localTimestamp, final List<LinkFeature> linkFeatures) {
        super(localTimestamp);
        this.features = linkFeatures;
    }

    public void addAll(final Collection<LinkFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<LinkFeature> iterator() {
        return features.iterator();
    }

    @Override
    public String toString() {
        return "LinkFeatureCollection{" + "features=" + features + '}';
    }
}
