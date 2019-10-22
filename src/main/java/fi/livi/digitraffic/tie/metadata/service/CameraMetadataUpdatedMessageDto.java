package fi.livi.digitraffic.tie.metadata.service;

import java.time.Instant;
import java.util.Set;

public class CameraMetadataUpdatedMessageDto extends MetadataUpdatedMessageDto {

    public enum EntityType {
        CAMERA,                 // KAMERA
        VIDEO_SERVER,           // VIDEOPALVELIN
        CAMERA_CONFIGURATION,   // KAMERAKOKOONPANO
        PRESET,                 // ESIASENTO
        MASTER_STORAGE,         // MASTER_TIETOVARASTO
        ROAD_ADDRESS;           // TIEOSOITE

        public static EntityType fromNameValue(final String name) {
            switch(name) {
            case "KAMERA":
                return CAMERA;
            case "VIDEOPALVELIN":
                return VIDEO_SERVER;
            case "KAMERAKOKOONPANO":
                return CAMERA_CONFIGURATION;
            case "ESIASENTO":
                return PRESET;
            case "MASTER_TIETOVARASTO":
                return MASTER_STORAGE;
            case "TIEOSOITE":
                return ROAD_ADDRESS;
            default:
                throw new IllegalArgumentException("Unknown metadata EntityType " + name);
            }
        }
    }

    private final EntityType entityType;

    public CameraMetadataUpdatedMessageDto(final Long messageLotjuId, final Set<Long> lotjuIds, final UpdateType updateType, final Instant updateTime, final EntityType entityType) {
        super(messageLotjuId, lotjuIds, updateType, updateTime);
        this.entityType = entityType;
    }

    public EntityType getEntityType() {
        return entityType;
    }
}
