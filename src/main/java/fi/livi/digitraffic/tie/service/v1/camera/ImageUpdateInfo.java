package fi.livi.digitraffic.tie.service.v1.camera;

import java.time.ZonedDateTime;

import fi.livi.digitraffic.tie.helper.DateHelper;

public class ImageUpdateInfo {

    private final ZonedDateTime updateTime;
    public enum Status {
        SUCCESS, FAILED, NONE;

        public boolean isSuccess() {
            return this.equals(SUCCESS);
        }
    }

    private final String presetId;
    private String versionId;
    private final ZonedDateTime lastUpdated;

    private long readTotalDurationMs = 0;
    private long writeTotalDurationMs = 0;
    private long readDurationMs = 0;
    private long writeDurationMs = 0;
    private String downloadUrl;
    private int sizeBytes = -1;
    private Status readStatus = Status.NONE;
    private Status writeStatus = Status.NONE;
    private Throwable readError;
    private Throwable writeError;

    ImageUpdateInfo(final String presetId, final ZonedDateTime lastUpdated) {
        this.presetId = presetId;
        this.lastUpdated = lastUpdated;
        this.updateTime = DateHelper.getZonedDateTimeNowAtUtc();
    }

    public String getPresetId() {
        return presetId;
    }

    public ZonedDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public String getVersionId() {
        return versionId;
    }

    void updateReadTotalDurationMs(final long currentReadDurationMs) {
        readTotalDurationMs += currentReadDurationMs;
    }

    long getReadTotalDurationMs() {
        return readTotalDurationMs;
    }

    void updateWriteTotalDurationMs(final long currentWriteDurationMs) {
        writeTotalDurationMs += currentWriteDurationMs;
    }

    long getWriteTotalDurationMs() {
        return writeTotalDurationMs;
    }

    void setReadDurationMs(final long readDurationMs) {
        this.readDurationMs = readDurationMs;
    }

    long getReadDurationMs() {
        return readDurationMs;
    }

    void setDownloadUrl(final String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    String getDownloadUrl() {
        return downloadUrl;
    }

    void setSizeBytes(final int sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public int getSizeBytes() {
        return sizeBytes;
    }

    void setWriteDurationMs(final long writeDurationMs) {
        this.writeDurationMs = writeDurationMs;
    }

    long getWriteDurationMs() {
        return writeDurationMs;
    }

    private void setReadStatus(final Status readStatus) {
        this.readStatus = readStatus;
    }

    Status getReadStatus() {
        return readStatus;
    }

    void updateReadStatusSuccess() {
        setReadStatus(Status.SUCCESS);
        setReadError(null);
    }

    private void setWriteStatus(final Status writeStatus) {
        this.writeStatus = writeStatus;
    }

    Status getWriteStatus() {
        return writeStatus;
    }

    void updateWriteStatusSuccess() {
        setWriteStatus(Status.SUCCESS);
        setWriteError(null);
    }

    public boolean isSuccess() {
        return getReadStatus().isSuccess() && getWriteStatus().isSuccess();
    }

    long getDurationMs() {
        return getReadDurationMs() + getWriteDurationMs();
    }

    void updateReadStatusFailed(final Throwable readException) {
        setReadStatus(Status.FAILED);
        setReadError(readException);
        setSizeBytes(-1);
    }

    Throwable getReadError() {
        return readError;
    }

    private void setReadError(final Throwable readError) {
        this.readError = readError;
    }

    void updateWriteStatusFailed(final Throwable writeError) {
        setWriteStatus(Status.FAILED);
        setWriteError(writeError);
    }

    Throwable getWriteError() {
        return writeError;
    }

    private void setWriteError(Throwable writeError) {
        this.writeError = writeError;
    }

    public ZonedDateTime getUpdateTime() {
        return updateTime;
    }

    long getImageTimeInPastSeconds() {
        return updateTime.toEpochSecond() - lastUpdated.toEpochSecond();
    }
}
