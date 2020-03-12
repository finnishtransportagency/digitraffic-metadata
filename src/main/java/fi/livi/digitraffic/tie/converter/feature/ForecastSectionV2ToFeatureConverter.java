package fi.livi.digitraffic.tie.converter.feature;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Feature;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Properties;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSection;
import fi.livi.digitraffic.tie.model.v1.forecastsection.ForecastSectionCoordinateList;

@Component
public class ForecastSectionV2ToFeatureConverter {

    public static ForecastSectionV2Feature convert(final ForecastSection forecastSection) {
        return new ForecastSectionV2Feature(forecastSection.getId(), getGeometry(forecastSection.getForecastSectionCoordinateLists()),
                                            getProperties(forecastSection));
    }

    private static ForecastSectionV2Properties getProperties(final ForecastSection forecastSection) {
        return new ForecastSectionV2Properties(forecastSection.getNaturalId(), forecastSection.getDescription(),
                                               forecastSection.getRoadNumber(), forecastSection.getRoadSectionNumber(), forecastSection.getLength(),
                                               forecastSection.getRoadSegments(),
                                               forecastSection.getLinkIds().stream().map(l -> l.getLinkId()).collect(Collectors.toList()));
    }

    private static MultiLineString getGeometry(final List<ForecastSectionCoordinateList> forecastSectionCoordinateLists) {
        final MultiLineString multiLineString = new MultiLineString();
        for (final ForecastSectionCoordinateList list : forecastSectionCoordinateLists) {
            multiLineString.addLineString(list.getListCoordinates());
        }
        return multiLineString;
    }
}
