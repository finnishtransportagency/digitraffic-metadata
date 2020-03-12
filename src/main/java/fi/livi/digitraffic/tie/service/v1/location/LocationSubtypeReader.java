package fi.livi.digitraffic.tie.service.v1.location;

import java.util.Arrays;

import fi.livi.digitraffic.tie.model.v1.location.LocationSubtype;
import fi.livi.digitraffic.tie.model.v1.location.LocationSubtypeKey;

public class LocationSubtypeReader extends AbstractReader<LocationSubtype> {
    protected LocationSubtypeReader(final String version) {
        super(version);
    }

    @Override protected LocationSubtype convert(final String[] components) {
        final LocationSubtype newType = new LocationSubtype();

        try {
            newType.setId(new LocationSubtypeKey(version, components[4]));
            newType.setDescriptionEn(components[3]);
            newType.setDescriptionFi(components[5]);
        } catch(final Exception e) {
            log.info("Exception when reading line " + Arrays.toString(components), e);

            return null;
        }

        if(!newType.validate()) {
            log.error("Could not validate new LocationSubType:" + Arrays.toString(components));

            return null;
        }

        return newType;
    }
}
