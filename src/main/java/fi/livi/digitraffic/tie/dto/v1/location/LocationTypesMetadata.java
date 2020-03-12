package fi.livi.digitraffic.tie.dto.v1.location;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Location types and location subtypes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationTypesMetadata {
    public final ZonedDateTime typesUpdated;
    public final String typesVersion;

    public final List<LocationTypeJson> locationTypes;
    public final List<LocationSubtypeJson> locationSubtypes;

    public LocationTypesMetadata(final ZonedDateTime typesUpdated,
                                 final String typesVersion,
                                 final List<LocationTypeJson> locationTypes,
                                 final List<LocationSubtypeJson> locationSubtypes) {
        this.typesUpdated = typesUpdated;
        this.typesVersion = typesVersion;
        this.locationTypes = locationTypes;
        this.locationSubtypes = locationSubtypes;
    }

    public LocationTypesMetadata(final ZonedDateTime typesUpdated, final String typesVersion) {
        this(typesUpdated, typesVersion, null, null);
    }
}
