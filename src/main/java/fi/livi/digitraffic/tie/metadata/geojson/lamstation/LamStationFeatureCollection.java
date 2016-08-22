package fi.livi.digitraffic.tie.metadata.geojson.lamstation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Collection of Lam Stations", value = "LamStationFeatureCollection")
@JsonPropertyOrder({ "type", "features" })
public class LamStationFeatureCollection extends RootDataObjectDto implements Iterable<LamStationFeature> {

    @ApiModelProperty(value = "\"FeatureCollection\": GeoJSON FeatureCollection Object", required = true, position = 1)
    private final String type = "FeatureCollection";

    @ApiModelProperty(value = "Features", required = true, position = 2)
    private List<LamStationFeature> features = new ArrayList<LamStationFeature>();

    public LamStationFeatureCollection(final LocalDateTime localTimestamp) {
        super(localTimestamp);
    }

    public String getType() {
        return type;
    }

    public List<LamStationFeature> getFeatures() {
        return features;
    }

    public void setFeatures(final List<LamStationFeature> features) {
        this.features = features;
    }

    public LamStationFeatureCollection add(final LamStationFeature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(final Collection<LamStationFeature> features) {
        this.features.addAll(features);
    }

    @Override
    public Iterator<LamStationFeature> iterator() {
        return features.iterator();
    }

    @Override
    public String toString() {
        return "LamStationFeatureCollection{" + "features=" + features + '}';
    }
}
