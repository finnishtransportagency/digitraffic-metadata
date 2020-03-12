package fi.livi.digitraffic.tie.service.v1.location;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import fi.livi.digitraffic.tie.model.DataType;

public class MetadataVersions {
    private final Map<DataType, MetadataVersion> versionMap = new EnumMap<DataType, MetadataVersion>(DataType.class);

    public void addVersion(final DataType type, final String filename, final String version) {
        versionMap.put(type, new MetadataVersion(filename, version));
    }

    public MetadataVersion getLocationsVersion() {
        return versionMap.get(DataType.LOCATIONS_METADATA);
    }

    public MetadataVersion getLocationTypeVersion() {
        return versionMap.get(DataType.LOCATION_TYPES_METADATA);
    }

    public static class MetadataVersion {
        public final String filename;
        public final String version;

        public MetadataVersion(final String filename, final String version) {
            this.filename = filename;
            this.version = version;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
        }
    }
}
