package fi.livi.digitraffic.tie.metadata.geojson.forecastsection;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootMetadataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of road conditions", value = "ForecastSectionFeatureCollection")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class ForecastSectionFeatureCollection extends RootMetadataObjectDto implements Iterable<ForecastSectionFeature> {

    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    private final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    private List<ForecastSectionFeature> features = new ArrayList<ForecastSectionFeature>();

    public ForecastSectionFeatureCollection(final ZonedDateTime localTimestamp, final ZonedDateTime dataLastCheckedTime) {
        super(localTimestamp, dataLastCheckedTime);
    }

    public String getType() {
        return type;
    }

    public List<ForecastSectionFeature> getFeatures() {
        return features;
    }

    public void setFeatures(final List<ForecastSectionFeature> features) {
        this.features = features;
    }

    public ForecastSectionFeatureCollection add(final ForecastSectionFeature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(final Collection<ForecastSectionFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<ForecastSectionFeature> iterator() {
        return features.iterator();
    }

    @Override
    public String toString() {
        return "ForecastSectionFeatureCollection{" + "features=" + features + '}';
    }
}
