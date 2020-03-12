package fi.livi.digitraffic.tie.conf.amazon;

import java.net.URI;

import fi.livi.digitraffic.tie.service.v1.camera.CameraImageS3Writer;

public class WeathercamS3Properties {

    private final String s3WeathercamBucketName;
    private final String s3WeathercamRegion;
    private final String s3WeathercamKeyRegexp = "^C([0-9]{7})\\.jpg$";
    private final int historyMaxAgeHours;
    private final String weathercamBaseUrl;

    public WeathercamS3Properties(final String s3WeathercamBucketName, final String s3WeathercamRegion,
                                  final int historyMaxAgeHours, final String weathercamBaseUrl) {

        this.s3WeathercamBucketName = s3WeathercamBucketName;
        this.s3WeathercamRegion = s3WeathercamRegion;
        this.historyMaxAgeHours = historyMaxAgeHours;
        this.weathercamBaseUrl = weathercamBaseUrl;
    }

    public String getS3WeathercamBucketName() {
        return s3WeathercamBucketName;
    }

    public String getS3WeathercamRegion() {
        return s3WeathercamRegion;
    }

    public String getS3WeathercamKeyRegexp() {
        return s3WeathercamKeyRegexp;
    }

    public int getHistoryMaxAgeHours() {
        return historyMaxAgeHours;
    }

    public String getWeathercamBaseUrl() {
        return weathercamBaseUrl;
    }

    public String getS3WeathercamBucketUrl() {
        return String.format("http://%s.s3-%s.amazonaws.com", getS3WeathercamBucketName(), getS3WeathercamRegion());
    }

    public String getPublicUrlForVersion(final String presetId, final String versionId) {
        return String.format("%s%s.jpg?versionId=%s", weathercamBaseUrl, presetId, versionId);
    }

    public URI getS3UriForVersion(final String imageName, final String versionId) {
        URI uri = URI.create(String.format("%s/%s?versionId=%s", getS3WeathercamBucketUrl(),
            getImageVersionKey(getPresetIdFromImageName(imageName)), versionId));
        return uri;
    }

    private String getImageVersionKey(final String presetId) {
        return presetId + CameraImageS3Writer.IMAGE_VERSION_KEY_SUFFIX;
    }

    public String getPresetIdFromImageName(final String imageName) {
        return imageName.substring(0,8);
    }
}
