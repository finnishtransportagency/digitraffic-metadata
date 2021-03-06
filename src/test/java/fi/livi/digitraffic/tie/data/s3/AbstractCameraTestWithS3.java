package fi.livi.digitraffic.tie.data.s3;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import fi.livi.digitraffic.tie.AbstractDaemonTestWithS3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

@TestPropertySource(properties = { "logging.level.org.springframework.test.context.transaction.TransactionContext=WARN" })
public abstract class AbstractCameraTestWithS3 extends AbstractDaemonTestWithS3 {

    @Value("${metadata.server.path.image}")
    protected String LOTJU_IMAGE_PATH;

    protected S3Object readWeathercamS3Object(final String key) {
        return readWeathercamS3ObjectVersion(key, null);
    }

    protected S3Object readWeathercamS3ObjectVersion(final String key, final String versionId) {
        final GetObjectRequest gor = new GetObjectRequest(weathercamBucketName, key);
        if (versionId != null) {
            gor.setVersionId(versionId);
        }
        return amazonS3.getObject(gor);
    }

    protected byte[] readWeathercamS3Data(final String key) {
        return readWeathercamS3DataVersion(key, null);
    }

    protected byte[] readWeathercamS3DataVersion(final String key, final String versionId) {
        final S3Object version = getS3ObjectVersionVersion(key, versionId);
        try {
            return version.getObjectContent().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected S3Object getS3ObjectVersionVersion(final String key, final String versionId) {
        return readWeathercamS3ObjectVersion(key, versionId);
    }
}
