package fi.livi.digitraffic.tie.model.v1.camera;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicUpdate;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
@DynamicUpdate
public class CameraPresetHistory {

    @EmbeddedId
    private CameraPresetHistoryPK id;
    @Column(nullable = false)
    private Long cameraPresetId;
    @Column(nullable = false)
    private ZonedDateTime lastModified; // Image last modified
    @Column(nullable = false)
    private Boolean publishable;
    @Column(nullable = false)
    private Integer size;
    @Column(nullable = false)
    private String cameraId;
    @Column(nullable = false)
    private Boolean presetPublic;

    @Column(nullable = false, updatable = false, insertable = false)
    private ZonedDateTime created; // history created
    @Column(nullable = false, updatable = false, insertable = false)
    private ZonedDateTime modified; // history modified

    // For Hibernate
    public CameraPresetHistory() {
    }

    public CameraPresetHistory(final @NotNull String presetId, final @NotNull String versionId, final long cameraPresetId,
                               final @NotNull ZonedDateTime lastModified, final boolean publishable, final int size, final boolean presetPublic) {
        this.id = new CameraPresetHistoryPK(presetId, versionId);
        this.cameraPresetId = cameraPresetId;
        this.lastModified = lastModified;
        this.publishable = publishable;
        this.size = size;
        this.cameraId = presetId.substring(0,6);
        this.presetPublic = presetPublic;
    }

    public CameraPresetHistoryPK getId() {
        return id;
    }

    public String getPresetId() {
        return id.getPresetId();
    }

    public String getVersionId() {
        return id.getVersionId();
    }

    public Long getCameraPresetId() {
        return cameraPresetId;
    }

    public ZonedDateTime getLastModified() {
        return lastModified;
    }

    public Boolean getPublishable() {
        return publishable;
    }

    public void setPublishable(Boolean publishable) {
        this.publishable = publishable;
    }

    public Integer getSize() {
        return size;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public ZonedDateTime getModified() {
        return modified;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
