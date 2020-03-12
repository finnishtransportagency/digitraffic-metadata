package fi.livi.digitraffic.tie.converter.feature;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeature;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeatureCollection;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionProperties;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSection;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSectionCoordinate;

@Component
public class ForecastSectionV1ToFeatureConverter extends AbstractMetadataToFeatureConverter {

    protected ForecastSectionV1ToFeatureConverter(CoordinateConverter coordinateConverter) {
        super(coordinateConverter);
    }

    public ForecastSectionFeatureCollection convert(List<ForecastSection> forecastSections, final ZonedDateTime lastUpdated, final ZonedDateTime dataLastCheckedTime) {

        final ForecastSectionFeatureCollection forecastSectionFeatures = new ForecastSectionFeatureCollection(lastUpdated, dataLastCheckedTime);

        for (ForecastSection fs : forecastSections) {
            forecastSectionFeatures.add(convert(fs));
        }
        return forecastSectionFeatures;
    }

    private ForecastSectionFeature convert(final ForecastSection fs) {
        final List<ForecastSectionCoordinate> collect = fs.getForecastSectionCoordinateLists().stream()
            .map(l -> l.getForecastSectionCoordinates())
            .flatMap(f -> f.stream())
            .collect(Collectors.toList());

        final List<List<Double>> coordinates =
            collect.stream().map(c -> Arrays.asList(c.getLongitude().doubleValue(), c.getLatitude().doubleValue()))
                                                       .collect(Collectors.toList());

        return new ForecastSectionFeature(fs.getId(), new LineString(coordinates), createProperties(fs));
    }

    private ForecastSectionProperties createProperties(ForecastSection fs) {
        final ForecastSectionProperties properties = new ForecastSectionProperties();

        properties.setNaturalId(fs.getNaturalId());
        properties.setDescription(fs.getDescription());
        properties.setRoadSectionNumber(fs.getRoadSectionNumber());
        properties.setRoadNumber(fs.getRoadNumber());
        properties.setRoadSectionVersionNumber(fs.getRoadSectionVersionNumber());
        properties.setStartDistance(fs.getStartDistance());
        properties.setEndDistance(fs.getEndDistance());
        properties.setLength(fs.getLength());
        properties.setRoad(fs.getRoad());
        properties.setStartRoadSection(fs.getStartRoadSection());
        properties.setEndRoadSection(fs.getEndRoadSection());
        return properties;
    }
}
