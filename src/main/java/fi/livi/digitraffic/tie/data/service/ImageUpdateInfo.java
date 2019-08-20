package fi.livi.digitraffic.tie.data.service;

public class ImageUpdateInfo {

    public enum Status { SUCCESS, FAILED, NONE;

        public boolean isSuccess() {
            return this.equals(SUCCESS);
        }
    }

    public ImageUpdateInfo(final String presetId, final String fullPath) {
        this.presetId = presetId;
        this.fullPath = fullPath;
    }

    private long readDurationMs = 0;
    private long writeDurationMs = 0;
    private String downloadUrl;
    private String presetId;
    private String fullPath;
    private int sizeBytes = -1;
    private Status readStatus = Status.NONE;
    private Status writeStatus = Status.NONE;
    private Throwable readError;
    private Throwable writeError;
    private int imageTimestampEpochSecond;

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

    public void setPresetId(final String presetId) {
        this.presetId = presetId;
    }

    public String getPresetId() {
        return presetId;
    }

    void setFullPath(final String fullPath) {
        this.fullPath = fullPath;
    }

    String getFullPath() {
        return fullPath;
    }

    void setSizeBytes(final int sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    int getSizeBytes() {
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

    void setReadStatusSuccess() {
        setReadStatus(Status.SUCCESS);
        setReadError(null);
    }

    private void setWriteStatus(final Status writeStatus) {
        this.writeStatus = writeStatus;
    }

    Status getWriteStatus() {
        return writeStatus;
    }

    void setWriteStatusSuccess() {
        setWriteStatus(Status.SUCCESS);
        setWriteError(null);
    }

    boolean isSuccess() {
        return getReadStatus().isSuccess() && getWriteStatus().isSuccess();
    }

    long getDurationMs() {
        return getReadDurationMs() + getWriteDurationMs();
    }

    public void setImageTimestampEpochSecond(final int imageTimestampEpochSecond) {
        this.imageTimestampEpochSecond = imageTimestampEpochSecond;
    }

    public int getImageTimestampEpochSecond() {
        return imageTimestampEpochSecond;
    }

    void setReadStatusFailed(final Throwable readException) {
        setReadStatus(Status.FAILED);
        setReadError(readException);
        setSizeBytes(-1);
    }

    Throwable getReadError() {
        return readError;
    }

    private void setReadError(Throwable readError) {
        this.readError = readError;
    }

    void setWriteStatusFailed(final Throwable writeError) {
        setWriteStatus(Status.FAILED);
        setWriteError(writeError);
    }

    Throwable getWriteError() {
        return writeError;
    }

    private void setWriteError(Throwable writeError) {
        this.writeError = writeError;
    }
}
