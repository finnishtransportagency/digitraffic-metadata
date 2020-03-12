package fi.livi.digitraffic.tie.service.v1.location;

import java.nio.file.Path;

public class MetadataPathCollection {
    public final Path locationsPath;
    public final Path subtypesPath;
    public final Path typesPath;

    public MetadataPathCollection(final Path locationsPath,
                                  final Path subtypesPath,
                                  final Path typesPath) {
        this.locationsPath = locationsPath;
        this.subtypesPath = subtypesPath;
        this.typesPath = typesPath;
    }
}
