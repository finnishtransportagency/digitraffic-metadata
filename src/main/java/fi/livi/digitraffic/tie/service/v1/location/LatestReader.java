package fi.livi.digitraffic.tie.service.v1.location;

import org.apache.commons.lang3.StringUtils;

import fi.livi.digitraffic.tie.model.DataType;

public class LatestReader extends AbstractReader<Void> {
    private final MetadataVersions latestMetadataVersions = new MetadataVersions();

    protected LatestReader() {
        super("");
    }

    @Override
    protected Void convert(final String[] line) {
        final DataType type = getMetadataType(line[0]);

        latestMetadataVersions.addVersion(type, line[1], line[2]);

        return null;
    }

    private DataType getMetadataType(final String type) {
        if(StringUtils.equals(type, "csv")) {
            return DataType.LOCATIONS_METADATA;
        } else if(StringUtils.equals(type, "dat")) {
            return DataType.LOCATION_TYPES_METADATA;
        }

        throw new IllegalArgumentException("Unknown metadatatype " + type);
    }

    public MetadataVersions getLatestMetadataVersions()  {
        return latestMetadataVersions;
    }
}
