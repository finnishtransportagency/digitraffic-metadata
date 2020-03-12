package fi.livi.digitraffic.tie.model;

import static fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KeruunTILA.KERUUSSA;
import static fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KeruunTILA.POISTETTU_PYSYVASTI;
import static fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KeruunTILA.POISTETTU_TILAPAISESTI;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KeruunTILA;

public enum CollectionStatus {

    GATHERING(KERUUSSA.value()),
    REMOVED_TEMPORARILY(POISTETTU_TILAPAISESTI.value()),
    REMOVED_PERMANENTLY(POISTETTU_PYSYVASTI.value());

    private static final Set<String> INACTIVE_KERUUN_TILAS =
            new HashSet<>(Arrays.asList(POISTETTU_PYSYVASTI.name(),
                                        POISTETTU_TILAPAISESTI.name()));

    private static final Logger log = LoggerFactory.getLogger(CollectionStatus.class);

    private final String fiValue;

    CollectionStatus(final String fiValue) {
        this.fiValue = fiValue;
    }

    public String getFiValue() {
        return fiValue;
    }

    public static CollectionStatus convertKeruunTila(final fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KeruunTILA keruunTila) {
        if (keruunTila != null) {
            return getStatus(keruunTila.value());
        }
        return null;
    }

    public static CollectionStatus convertKeruunTila(final fi.livi.digitraffic.tie.external.lotju.metadata.lam.KeruunTILA keruunTila) {
        if (keruunTila != null) {
            return getStatus(keruunTila.value());
        }
        return null;
    }

    public static CollectionStatus convertKeruunTila(final fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.KeruunTILA keruunTila) {
        if (keruunTila != null) {
            return getStatus(keruunTila.value());
        }
        return null;
    }

    private static CollectionStatus getStatus(final String fiValue) {
        for (final CollectionStatus collectionStatus : CollectionStatus.values()) {
            if (collectionStatus.getFiValue().equals(fiValue)) {
                return collectionStatus;
            }
        }
        log.error("CollectionStatus for KeruunTILA " + fiValue + " not found");
        return null;
    }

    // Keruuntila enum is practically same fo all Kamera, TiesaaAsema and Lamasema, but because
    // it's not sure which version code generation uses, it's safer to test enum's string values
    public static boolean isUnactiveKeruunTila(final KeruunTILA keruunTila) {
        return INACTIVE_KERUUN_TILAS.contains(keruunTila.name());
    }

    public static boolean isPermanentlyDeletedKeruunTila(final fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KeruunTILA keruunTila) {
        return KeruunTILA.POISTETTU_PYSYVASTI.name().equals(keruunTila.name());
    }
    public static boolean isPermanentlyDeletedKeruunTila(final fi.livi.digitraffic.tie.external.lotju.metadata.lam.KeruunTILA keruunTila) {
        return KeruunTILA.POISTETTU_PYSYVASTI.name().equals(keruunTila.name());
    }
    public static boolean isPermanentlyDeletedKeruunTila(final fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.KeruunTILA keruunTila) {
        return KeruunTILA.POISTETTU_PYSYVASTI.name().equals(keruunTila.name());
    }

}
