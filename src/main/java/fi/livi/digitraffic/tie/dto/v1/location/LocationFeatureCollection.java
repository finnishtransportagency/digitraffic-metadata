package fi.livi.digitraffic.tie.dto.v1.location;

import java.time.ZonedDateTime;
import java.util.List;

public final class LocationFeatureCollection {
    public final String type = "FeatureCollection";

    public final ZonedDateTime locationsUpdateTime;
    public final String locationsVersion;

    public final List<LocationFeature> features;

    public LocationFeatureCollection(final ZonedDateTime locationsUpdateTime,
                                     final String locationsVersion,
                                     final List<LocationFeature> features) {
        this.locationsUpdateTime = locationsUpdateTime;
        this.locationsVersion = locationsVersion;
        this.features = features;
    }

    public LocationFeatureCollection(final ZonedDateTime locationsUpdateTime, final String locationsVersion) {
        this(locationsUpdateTime, locationsVersion, null);
    }
}
