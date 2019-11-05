package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.matcher.ZonedDateTimeMatcher;
import fi.livi.digitraffic.tie.metadata.controller.BetaController;

// Methods are in BetaController now
public class PresetHistoryControllerTest extends AbstractRestWebTest {

    @Value("${weathercam.baseUrl}")
    private String weathercamBaseUrl;

    private static final int SIZE = 100000;

    private ResultActions getJson(final String url) throws Exception {
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(RoadWebApplicationConfiguration.API_BETA_BASE_PATH + url);

        get.contentType(MediaType.APPLICATION_JSON);

        return mockMvc.perform(get);
    }

    /** @return versionId */
    private String insertTestData(final String presetId, final ZonedDateTime lastModified) {
        return insertTestData(presetId, lastModified, true);
    }

    /** @return versionId */
    private String insertTestData(final String presetId, final ZonedDateTime lastModified, final boolean isPublic) {
        final String versionId = RandomStringUtils.randomAlphanumeric(32);
        final String cameraId = presetId.substring(0,6);
        entityManager.createNativeQuery(
            "insert into camera_preset_history(preset_id, camera_id, version_id, camera_preset_id, last_modified, publishable, size, created)\n" +
            "VALUES ('" + presetId + "', '" + cameraId + "', '" + versionId + "',  31575, timestamp with time zone '" + lastModified.toInstant() + "', " + isPublic + ", " + SIZE + ", NOW())")
            .executeUpdate();
        return versionId;
    }

    private Matcher<String> matchUrl(String presetId, String versionId) {
        return Matchers.is(String.format("%s%s.jpg?versionId=%s", weathercamBaseUrl, presetId, versionId));
    }

    @Test
    public void notFoundForNotExistingPreset() throws Exception {
        getJson(BetaController.CAMERA_HISTORY_PATH + "/history/C0000000")
            .andExpect(status().isNotFound());
    }

    @Test
    public void emptyForTooOldHistory() throws Exception {
        final String presetId = "C0000001";
        insertTestData(presetId, ZonedDateTime.now().minusHours(25));

        getJson(BetaController.CAMERA_HISTORY_PATH + "/history/" + presetId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraId", Matchers.is(presetId.substring(0,6))))
            .andExpect(jsonPath("cameraHistory", Matchers.hasSize(0)))
        ;
    }

    @Test
    public void historyExistsWithoutSecret() throws Exception {
        final String presetId = "C0000002";
        final ZonedDateTime now = ZonedDateTime.now();
        final String versionId0 = insertTestData(presetId, now);
        insertTestData(presetId, now.minusMinutes(30), false); // this is not public
        final String versionId1 = insertTestData(presetId, now.minusHours(1));
        insertTestData(presetId, now.minusHours(25)); // This is too old
        insertTestData("C1234567", now); // This is for another preset

        getJson(BetaController.CAMERA_HISTORY_PATH + "/history/" + presetId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraId", Matchers.is(presetId.substring(0,6))))
            .andExpect(jsonPath("cameraHistory[0].presetId", Matchers.is(presetId)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory", Matchers.hasSize(2)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].imageUrl", matchUrl(presetId, versionId0)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].sizeBytes", Matchers.is(SIZE)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[1].imageUrl", matchUrl(presetId, versionId1)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[1].lastModified", ZonedDateTimeMatcher.of(now.minusHours(1))))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[1].sizeBytes", Matchers.is(SIZE)))
        ;
    }

    @Test
    public void historyAtGivenTimeTooOld() throws Exception {
        final String presetId = "C0000003";
        final ZonedDateTime tooOld = ZonedDateTime.now().minusHours(25);
        insertTestData(presetId, tooOld);

        getJson(BetaController.CAMERA_HISTORY_PATH + "/history/" + presetId + "?atTime=" +
            DateHelper.toZonedDateTimeAtUtc(ZonedDateTime.now()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraId", Matchers.is(presetId.substring(0,6))))
            .andExpect(jsonPath("cameraHistory", Matchers.hasSize(0)))
        ;
    }

    @Test
    public void historyAtGivenTime() throws Exception {
        final String presetId = "C0000003";
        final ZonedDateTime now = ZonedDateTime.now();
        insertTestData(presetId, now);
        final String versionId = insertTestData(presetId, now.minusHours(1));

        getJson(BetaController.CAMERA_HISTORY_PATH + "/history/" + presetId + "?atTime=" +
                DateHelper.toZonedDateTimeAtUtc(now.minusHours(1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraId", Matchers.is(presetId.substring(0,6))))
            .andExpect(jsonPath("cameraHistory[0].presetId", Matchers.is(presetId)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory", Matchers.hasSize(1)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].imageUrl", matchUrl(presetId, versionId)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now.minusHours(1))))
        ;
    }

    @Test
    public void historyAtGivenTimeWithSecret() throws Exception {
        final String presetId = "C0000003";
        final ZonedDateTime now = ZonedDateTime.now();
        insertTestData(presetId, now);
        insertTestData(presetId, now.minusHours(1), false); // This is skipped as not public
        final String versionId = insertTestData(presetId, now.minusHours(2));

        getJson(BetaController.CAMERA_HISTORY_PATH + "/history/" + presetId + "?atTime=" +
            DateHelper.toZonedDateTimeAtUtc(now.minusSeconds(1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraId", Matchers.is(presetId.substring(0,6))))
            .andExpect(jsonPath("cameraHistory[0].presetId", Matchers.is(presetId)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory", Matchers.hasSize(1)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].imageUrl", matchUrl(presetId, versionId)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now.minusHours(2))))
        ;
    }

    @Test
    public void historyForCamera() throws Exception {
        final String cameraId = "C00000";
        final String presetId1 = cameraId + "01";
        final String presetId2 = cameraId + "02";
        final ZonedDateTime now = ZonedDateTime.now();
        insertTestData(presetId1, now);
        insertTestData(presetId1, now.minusHours(1), false); // This is skipped as not public
        insertTestData(presetId1, now.minusHours(2));
        insertTestData(presetId2, now);
        insertTestData(presetId2, now.minusHours(1));
        insertTestData(presetId2, now.minusHours(2));

        getJson(BetaController.CAMERA_HISTORY_PATH + "/history/" + cameraId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraId", Matchers.is(cameraId)))
            .andExpect(jsonPath("cameraHistory[0].presetId", Matchers.is(presetId1)))
            .andExpect(jsonPath("cameraHistory[1].presetId", Matchers.is(presetId2)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory", Matchers.hasSize(2)))
            .andExpect(jsonPath("cameraHistory[1].presetHistory", Matchers.hasSize(3)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[1].lastModified", ZonedDateTimeMatcher.of(now.minusHours(2))))
            .andExpect(jsonPath("cameraHistory[1].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now)))
            .andExpect(jsonPath("cameraHistory[1].presetHistory[1].lastModified", ZonedDateTimeMatcher.of(now.minusHours(1))))
            .andExpect(jsonPath("cameraHistory[1].presetHistory[2].lastModified", ZonedDateTimeMatcher.of(now.minusHours(2))))
        ;
    }


}
