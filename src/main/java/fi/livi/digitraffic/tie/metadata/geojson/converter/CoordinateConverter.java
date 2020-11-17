package fi.livi.digitraffic.tie.metadata.geojson.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.metadata.geojson.Point;

@Component
public class CoordinateConverter {

    private static final Logger log = LoggerFactory.getLogger(CoordinateConverter.class);

    private static final BlockingQueue<CoordinateTransform> transformersFromWGS84ToETRS89 = new LinkedBlockingQueue<>();
    private static final BlockingQueue<CoordinateTransform> transformersFromETRS89ToWGS84 = new LinkedBlockingQueue<>();

    static {
        final CRSFactory crsFactory = new CRSFactory();

        // WGS84: http://spatialreference.org/ref/epsg/4326/ -> Proj4
        final CoordinateReferenceSystem wgs84 = crsFactory.createFromName("EPSG:4326");

        // ETRS89-TM35FIN/EUREF-FIN http://spatialreference.org/ref/epsg/etrs89-etrs-tm35fin/ -> Proj4
        final CoordinateReferenceSystem etrs89tm35fin = crsFactory.createFromName("EPSG:3067");

        final CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();

        // Create 5 CoordinateTransform -objects as they are not thread safe. Put them in Queue so that different
        // threads can use them and if all are used at the moment of the method call they will wait for next one
        // to become available.
        IntStream.range(0,5).forEach(i -> {
            // ETRS89-TM35FIN <-> WGS84 transformers
            transformersFromETRS89ToWGS84.add(coordinateTransformFactory.createTransform(etrs89tm35fin, wgs84));
            transformersFromWGS84ToETRS89.add(coordinateTransformFactory.createTransform(wgs84, etrs89tm35fin));
        });

    }

    public static Point convertFromETRS89ToWGS84(final Point fromETRS89) {
        CoordinateTransform transformer = null;
        try {
            // Take/wait transformer from the queue
            transformer = transformersFromETRS89ToWGS84.take();
            return convert(fromETRS89, transformer);
        } catch (final Exception e) {
            log.error("method=convertFromETRS89ToWGS84 ERROR", e);
            throw new RuntimeException(e);
        } finally {
            // Put transformer back to queue
            if (transformer != null) {
                transformersFromETRS89ToWGS84.add(transformer);
            }
        }
    }

    public static Point convertFromWGS84ToETRS89(Point fromWGS84) {
        CoordinateTransform transformer = null;
        try {
            // Take/wait transformer from the queue
            transformer = transformersFromWGS84ToETRS89.take();
            return convert(fromWGS84, transformer);
        } catch (final Exception e) {
            log.error("method=convertFromWGS84ToETRS89 ERROR", e);
            throw new RuntimeException(e);
        } finally {
            // Put transformer back to queue
            if (transformer != null) {
                transformersFromWGS84ToETRS89.add(transformer);
            }
        }
    }

    // TODO: change List<List<Double>> to List<List<Integer>> and remove int->double conversion from HavaintoToObservationFeatureConverter and do it here.
    public static LineString convertLineStringFromETRS89ToWGS84(List<List<Double>> fromETRS89Coordinates) {
        List<List<Double>> coords =
            fromETRS89Coordinates.stream()
                .map(l -> (List<Double>) new ArrayList<>(convertFromETRS89ToWGS84(new Point(l)).getCoordinates()))
                .collect(Collectors.toList());
        return new LineString(coords);
    }

    public static LineString convertLineStringCoordinatesFromWGS84ToETRS89LineString(final List<List<Double>> fromWGS84Coordinates) {
        return new LineString(convertLineStringCoordinatesFromWGS84ToETRS89(fromWGS84Coordinates));
    }

    public static List<List<Double>> convertLineStringCoordinatesFromWGS84ToETRS89(final List<List<Double>> fromWGS84Coordinates) {
        return fromWGS84Coordinates.stream()
                .map(l -> (List<Double>) new ArrayList<>(convertFromWGS84ToETRS89(new Point(l)).getCoordinates()))
                .collect(Collectors.toList());
    }

    private static Point convert(final Point fromPoint, final CoordinateTransform transformer) {
        ProjCoordinate to = new ProjCoordinate();
        ProjCoordinate from = new ProjCoordinate(fromPoint.getLongitude(),
                                                 fromPoint.getLatitude());
        // CoordinateTransform is not thread safe and this can be called from multiple threads
        synchronized (transformer) {
            transformer.transform(from, to);
        }
        Point point = new Point(to.x, to.y, fromPoint.getAltitude());

        if (log.isDebugEnabled()) {
            log.debug("From: " + fromPoint + " to " + point);
        }
        return point;
    }
}
