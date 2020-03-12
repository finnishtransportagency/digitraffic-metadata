package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.WEATHERCAM_PATH;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.net.URI;
import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Properties;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPresetHistory;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageUpdateService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryDataService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryDataService.HistoryStatus;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Import(CameraImageUpdateService.class)
public class WeathercamControllerTest extends AbstractRestWebTest {

    private static final Logger log = LoggerFactory.getLogger(WeathercamControllerTest.class);

    @MockBean
    private CameraPresetHistoryDataService cameraPresetHistoryDataService;

    @Autowired
    private WeathercamS3Properties weathercamS3Properties;

    private final String imageName = "C7777701.jpg";
    private final String versionId = "qwerty";

    @Test
    public void getPublicImage() throws Exception {

        Mockito.when(cameraPresetHistoryDataService.findHistoryVersionInclSecretInternal(eq(getPresetId(imageName)), eq(versionId)))
            .thenReturn(createHistory(imageName, versionId, true, ZonedDateTime.now()));

        Mockito.when(cameraPresetHistoryDataService.resolveHistoryStatusForVersion(eq(imageName), eq(versionId)))
            .thenReturn(HistoryStatus.PUBLIC);

        MockHttpServletResponse response = requestImage(imageName, versionId);
        assertResponse(response, HttpStatus.FOUND, getVersionedRedirectUrl(imageName, versionId));
    }

    @Test
    public void getSecretImage() throws Exception {

        Mockito.when(cameraPresetHistoryDataService.findHistoryVersionInclSecretInternal(eq(getPresetId(imageName)), eq(versionId)))
            .thenReturn(createHistory(imageName, versionId, false, ZonedDateTime.now()));

        Mockito.when(cameraPresetHistoryDataService.resolveHistoryStatusForVersion(eq(imageName), eq(versionId)))
            .thenReturn(HistoryStatus.SECRET);

        MockHttpServletResponse response = requestImage(imageName, versionId);
        assertResponse(response, HttpStatus.NOT_FOUND, null);
    }

    @Test
    public void getTooOldImage() throws Exception {

        Mockito.when(cameraPresetHistoryDataService.findHistoryVersionInclSecretInternal(eq(getPresetId(imageName)), eq(versionId)))
            .thenReturn(createHistory(imageName, versionId, true, ZonedDateTime.now().minusHours(25)));

        Mockito.when(cameraPresetHistoryDataService.resolveHistoryStatusForVersion(eq(imageName), eq(versionId)))
            .thenReturn(HistoryStatus.TOO_OLD);

        MockHttpServletResponse response = requestImage(imageName, versionId);
        assertResponse(response, HttpStatus.NOT_FOUND, null);
    }

    @Test
    public void getNotExistingImage() throws Exception {

        Mockito.when(cameraPresetHistoryDataService.findHistoryVersionInclSecretInternal(anyString(), anyString()))
            .thenReturn(null);

        Mockito.when(cameraPresetHistoryDataService.resolveHistoryStatusForVersion(eq(imageName), eq(versionId)))
            .thenReturn(HistoryStatus.NOT_FOUND);

        MockHttpServletResponse response = requestImage(imageName, versionId);
        assertResponse(response, HttpStatus.NOT_FOUND, null);
    }

    private void assertResponse(MockHttpServletResponse response, final HttpStatus httpStatus, final String redirectUrl) {
        log.info("HTTP response: {} and redirect: {}", response.getStatus(), response.getRedirectedUrl());
        Assert.assertEquals(httpStatus.value(), response.getStatus());
        Assert.assertEquals(redirectUrl, response.getRedirectedUrl());
    }

    private String getVersionedRedirectUrl(final String imageName, final String versionId) {
        return weathercamS3Properties.getS3UriForVersion(imageName, versionId).toString();
    }

    private CameraPresetHistory createHistory(final String imageName, final String versionId, final boolean publishable, final ZonedDateTime lastModified) {
        return new CameraPresetHistory(getPresetId(imageName), versionId, -1, lastModified, publishable, 10, true);
    }

    private MockHttpServletResponse requestImage(final String imageName, final String versionId) throws Exception {
        final URI uri = URI.create(WEATHERCAM_PATH + "/" + imageName + "?versionId=" + versionId);
        log.info("Request uri: {}", uri);
        final MockHttpServletRequestBuilder get = get(uri);

        return mockMvc.perform(get).andReturn().getResponse();
    }

    private static String getPresetId(String imageName) {
        return imageName.substring(0,8);
    }
}
