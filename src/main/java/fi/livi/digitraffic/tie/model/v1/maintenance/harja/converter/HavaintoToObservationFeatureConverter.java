package fi.livi.digitraffic.tie.model.v1.maintenance.harja.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.converter.AutoRegisteredConverter;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.ObservationFeature;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.ObservationProperties;
import fi.livi.digitraffic.tie.external.harja.Havainto;
import fi.livi.digitraffic.tie.external.harja.entities.KoordinaattisijaintiSchema;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;

@ConditionalOnWebApplication
@Component
public class HavaintoToObservationFeatureConverter extends AutoRegisteredConverter<Havainto, ObservationFeature> {

    @Override
    public ObservationFeature convert(final Havainto src) {

        Geometry geometry = resolveGeometry(src);

        final ObservationFeature tgt =
            new ObservationFeature(geometry,
                                   convert(src, ObservationProperties.class));
        return tgt;
    }

    private Geometry resolveGeometry(final Havainto src) {

        if (src.getSijainti() != null) {
            if (src.getSijainti().getViivageometria() != null) {
                List<List<Object>> coords = src.getSijainti().getViivageometria().getCoordinates();
                List<List<Double>> lineString =
                coords.stream().map(l -> {
                    final ArrayList<Double> list = new ArrayList<Double>();
                    for (Object o : l) {
                        list.add( Double.valueOf( (Integer) o ));
                    }
                    return list;
                }).collect(Collectors.toList());
                return CoordinateConverter.convertLineStringFromETRS89ToWGS84(lineString);
            } else if (src.getSijainti().getKoordinaatit() != null) {
                final KoordinaattisijaintiSchema coords = src.getSijainti().getKoordinaatit();
                return CoordinateConverter.convertFromETRS89ToWGS84(
                    new Point(coords.getX(), coords.getY(), coords.getZ()));
            }
        }
        return null;
    }
}
