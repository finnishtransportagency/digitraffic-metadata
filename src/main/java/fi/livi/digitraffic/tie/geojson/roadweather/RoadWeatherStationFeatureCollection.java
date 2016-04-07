package fi.livi.digitraffic.tie.geojson.roadweather;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of Road Weather Stations", value = "FeatureCollection")
public class RoadWeatherStationFeatureCollection implements Iterable<RoadWeatherStationFeature> {

    @ApiModelProperty(value = "\"FeatureCollection\"", required = true, position = 1)
    private final String type = "FeatureCollection";

    private List<RoadWeatherStationFeature> features = new ArrayList<>();

    public String getType() {
        return type;
    }

    public List<RoadWeatherStationFeature> getFeatures() {
        return features;
    }

    public void setFeatures(final List<RoadWeatherStationFeature> features) {
        this.features = features;
    }

    public RoadWeatherStationFeatureCollection add(final RoadWeatherStationFeature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(final Collection<RoadWeatherStationFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<RoadWeatherStationFeature> iterator() {
        return features.iterator();
    }

    @Override
    public String toString() {
        return "RoadWeatherStationFeature{" + "features=" + features + '}';
    }
}
