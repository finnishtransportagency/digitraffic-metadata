package fi.livi.digitraffic.tie.data.s3;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.IOException;

import org.junit.Rule;
import org.springframework.test.context.TestPropertySource;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithS3;

@TestPropertySource(properties = { "logging.level.org.springframework.test.context.transaction.TransactionContext=WARN" })
public abstract class AbstractCameraTestWithS3 extends AbstractDaemonTestWithS3 {

    protected static final String REQUEST_PATH = "/kamerakuva/";
    protected final static Integer TEST_PORT = 62859;

    // NOTE! Rules uses fixed port. see DPO-489
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(TEST_PORT));

    protected S3Object readWeathercamS3Object(final String key) {
        return readWeathercamS3ObjectVersion(key, null);
    }

    protected S3Object readWeathercamS3ObjectVersion(final String key, final String versionId) {
        final GetObjectRequest gor = new GetObjectRequest(weathercamBucketName, key);
        if (versionId != null) {
            gor.setVersionId(versionId);
        }
        return s3.getObject(gor);
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
