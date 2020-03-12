package fi.livi.digitraffic.tie.service;

import java.time.Instant;
import java.util.Set;

public class MetadataUpdatedMessageDto {

    public enum UpdateType {
        UPDATE, // PAIVITYS
        INSERT, // LISAYS
        DELETE; // POISTO

        public static UpdateType fromNameValue(final String name) {
            switch(name) {
            case "PAIVITYS":
                return UPDATE;
            case "LISAYS":
                return INSERT;
            case "POISTO":
                return DELETE;
            default:
                throw new IllegalArgumentException("Unknown metadata UpdateType " + name);
            }
        }
    }

    private final Long lotjuId;
    private final Set<Long> asemmaLotjuIds;
    private final UpdateType updateType;
    private Instant updateTime;

    public MetadataUpdatedMessageDto(final Long lotjuId, final Set<Long> asemmaLotjuIds, final UpdateType updateType, final Instant updateTime) {
        this.lotjuId = lotjuId;
        this.asemmaLotjuIds = asemmaLotjuIds;
        this.updateType = updateType;
        this.updateTime = updateTime;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public Set<Long> getAsemmaLotjuIds() {
        return asemmaLotjuIds;
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public Instant getUpdateTime() {
        return updateTime;
    }
}
