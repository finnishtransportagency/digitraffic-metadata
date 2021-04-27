package fi.livi.digitraffic.tie.service.v1.camera;

import static fi.livi.digitraffic.tie.helper.DateHelper.getZonedDateTimeNowAtUtc;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.service.v1.lotju.AbstractMultiDestinationProviderTest;

public class CameraImageReaderTest extends AbstractMultiDestinationProviderTest {

    private static final Logger log = LoggerFactory.getLogger(CameraImageReaderTest.class);

    final byte[] img1 = new byte[] { (byte) 1 };
    final byte[] img2 = new byte[] { (byte) 2 };

    private CameraImageReader cameraImageReader;

    @Before
    public void initCameraImageReader() {
        cameraImageReader = new CameraImageReader(1000, 1000, new String[] { baseUrl1, baseUrl2 },
                                                  dataPath, healthPath, 1, healthOkCheckValueInApplicationSettings);
    }

    @Test
    public void firstHealthOk() throws IOException {
        // Health response from server OK
        server1WhenRequestHealthThenReturn(OK, getOkResponseString());
        // Data request goes to server 1
        final int id = randomId();
        final String presetId = randomPresetId();
        serverWhenRequestUrlThenReturn(wireMockRule1, dataPath + "/" +  id, OK, img1);
        final ImageUpdateInfo info = new ImageUpdateInfo(presetId, getZonedDateTimeNowAtUtc());
        final byte[] img = cameraImageReader.readImage(id, info);

        Assert.assertArrayEquals(img1, img);
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(0);
    }

    @Test
    public void firstHealthNotOk() throws IOException {
        // Health response from server OK
        server1WhenRequestHealthThenReturn(INTERNAL_SERVER_ERROR, NOT_OK_RESPONSE_CONTENT);
        server2WhenRequestHealthThenReturn(OK, getOkResponseString());
        // Data request goes to server 1
        final int id = randomId();
        final String presetId = randomPresetId();
        serverWhenRequestUrlThenReturn(wireMockRule1, dataPath + "/" +  id, OK, img1);
        serverWhenRequestUrlThenReturn(wireMockRule2, dataPath + "/" +  id, OK, img2);
        final ImageUpdateInfo info = new ImageUpdateInfo(presetId, getZonedDateTimeNowAtUtc());
        final byte[] img = cameraImageReader.readImage(id, info);

        Assert.assertArrayEquals(img2, img);
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(1);
        verifyServer1DataCount(0);
        verifyServer2DataCount(1);
    }

    @Test
    public void firstHealthOkDataNotOk() throws IOException {
        // Health response from server OK
        server1WhenRequestHealthThenReturn(OK, getOkResponseString());
        server2WhenRequestHealthThenReturn(OK, getOkResponseString());
        // Data request goes to server 1
        final int id = randomId();
        final String presetId = randomPresetId();
        serverWhenRequestUrlThenReturn(wireMockRule1, dataPath + "/" +  id, INTERNAL_SERVER_ERROR, (String) null);
        serverWhenRequestUrlThenReturn(wireMockRule2, dataPath + "/" +  id, OK, img2);
        final ImageUpdateInfo info = new ImageUpdateInfo(presetId, getZonedDateTimeNowAtUtc());
        try {
            cameraImageReader.readImage(id, info);
            Assert.fail("First request to server1 should fail and throw exception");
        } catch (Exception e) {
            // empty
        }
        final byte[] img = cameraImageReader.readImage(id, info);

        Assert.assertArrayEquals(img2, img);
        verifyServer1HealthCount(1);
        verifyServer2HealthCount(1);
        verifyServer1DataCount(1);
        verifyServer2DataCount(1);
    }

    private String randomPresetId() {
        return "C" + RandomUtils.nextInt(1000000, 10000000);

    }

    private int randomId() {
        return RandomUtils.nextInt();
    }
}
