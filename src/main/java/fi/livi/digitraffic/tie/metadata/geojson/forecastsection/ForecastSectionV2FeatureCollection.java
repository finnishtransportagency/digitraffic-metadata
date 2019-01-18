package fi.livi.digitraffic.tie.metadata.geojson.forecastsection;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class ForecastSectionV2FeatureCollection implements Iterable<ForecastSectionV2Feature> {

    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    private final String type = "FeatureCollection";

    public final ZonedDateTime dataUpdatedTime;

    public final ZonedDateTime dataLastCheckedTime;

    @ApiModelProperty(value = "Features", required = true, position = 2)
    private List<ForecastSectionV2Feature> features = new ArrayList<>();

    public ForecastSectionV2FeatureCollection(final ZonedDateTime dataUpdatedTime, final ZonedDateTime dataLastCheckedTime) {
        this.dataUpdatedTime = dataUpdatedTime;
        this.dataLastCheckedTime = dataLastCheckedTime;
    }

    public String getType() {
        return type;
    }

    public List<ForecastSectionV2Feature> getFeatures() {
        return features;
    }

    public void setFeatures(final List<ForecastSectionV2Feature> features) {
        this.features = features;
    }

    public ForecastSectionV2FeatureCollection add(final ForecastSectionV2Feature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(final Collection<ForecastSectionV2Feature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<ForecastSectionV2Feature> iterator() {
        return features.iterator();
    }
}
