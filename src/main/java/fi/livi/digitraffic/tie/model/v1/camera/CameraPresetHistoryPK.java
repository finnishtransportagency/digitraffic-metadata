package fi.livi.digitraffic.tie.model.v1.camera;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Embeddable
public class CameraPresetHistoryPK implements Serializable {

    @Column(nullable = false)
    private String presetId;
    @Column(nullable = false)
    private String versionId;

    public CameraPresetHistoryPK() {
    }

    CameraPresetHistoryPK(final @NotNull String presetId, final @NotNull String versionId) {
        this.presetId = presetId;
        this.versionId = versionId;
    }

    public String getPresetId() {
        return presetId;
    }

    public String getVersionId() {
        return versionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CameraPresetHistoryPK that = (CameraPresetHistoryPK) o;

        return new EqualsBuilder()
                .append(presetId, that.presetId)
                .append(versionId, that.versionId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(presetId)
                .append(versionId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
