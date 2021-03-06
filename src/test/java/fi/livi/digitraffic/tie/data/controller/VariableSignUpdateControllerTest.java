package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_VARIABLE_SIGN_UPDATE_PART_PATH;
import static fi.livi.digitraffic.tie.controller.v1.VariableSignUpdateController.DATA_PATH;
import static fi.livi.digitraffic.tie.controller.v1.VariableSignUpdateController.METADATA_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.v2.V2DeviceDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2DeviceRepository;
import fi.livi.digitraffic.tie.model.v2.trafficsigns.Device;
import fi.livi.digitraffic.tie.model.v2.trafficsigns.DeviceData;

public class VariableSignUpdateControllerTest extends AbstractRestWebTest {
    @Autowired
    public V2DeviceRepository v2DeviceRepository;

    @Autowired
    public V2DeviceDataRepository v2DeviceDataRepository;

    @BeforeEach
    public void setUp() {
        v2DeviceDataRepository.deleteAllInBatch();
        v2DeviceRepository.deleteAllInBatch();
    }

    private void postJson(final String fileName, final String function) throws Exception {
        postJson(fileName, function, status().isOk());
    }

    private void postJson(final String fileName, final String function, final ResultMatcher expectResult) throws Exception {
        final String jsonContent = readResourceContent("classpath:lotju/variable_signs/" + fileName);

        final MockHttpServletRequestBuilder post = post(API_V1_BASE_PATH + API_VARIABLE_SIGN_UPDATE_PART_PATH + function)
            .content(jsonContent);

        post.contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(post).andExpect(expectResult);
    }

    private void assertDeviceCountInDb(final int count) {
        assertEquals(count, v2DeviceRepository.count());
    }

    private void assertDeviceDataCountInDb(final int count) {
        assertEquals(count, v2DeviceDataRepository.count());
    }

    @Test
    @Rollback
    public void okMetadataFile() throws Exception {
        assertDeviceCountInDb(0);
        postJson("ok_metadata.json", METADATA_PATH);
        assertDeviceCountInDb(209);
    }

    @Test
    @Rollback
    public void brokenMetadataFile() throws Exception {
        assertDeviceCountInDb(0);
        postJson("broken_metadata.json", METADATA_PATH, status().is4xxClientError());
        assertDeviceCountInDb(0);
    }

    @Test
    @Rollback
    public void okDataFile() throws Exception {
        assertDeviceDataCountInDb(0);
        postJson("ok_data.json", DATA_PATH);
        assertDeviceDataCountInDb(1);
    }

    @Test
    @Rollback
    public void brokenDataFile() throws Exception {
        assertDeviceDataCountInDb(0);
        postJson("broken_data.json", DATA_PATH, status().is4xxClientError());
        assertDeviceDataCountInDb(0);
    }

    @Test
    @Rollback
    public void updateDeviceData() throws Exception {
        // insert first data
        assertDeviceDataCountInDb(0);
        postJson("update_data_1.json", DATA_PATH);
        assertDeviceDataCountInDb(1);

        final List<Long> id1 = v2DeviceDataRepository.findLatestData("t1");
        final List<DeviceData> data1 = v2DeviceDataRepository.findDistinctByIdIn(id1);
        assertEquals(1, data1.size());
        assertEquals(2, data1.get(0).getRows().size());
        // update data
        postJson("update_data_2.json", DATA_PATH);
        assertDeviceDataCountInDb(2);

        final List<Long> id2 = v2DeviceDataRepository.findLatestData("t1");
        final List<DeviceData> data2 = v2DeviceDataRepository.findDistinctByIdIn(id2);
        assertEquals(1, data2.size());
        assertEquals(0, data2.get(0).getRows().size());

        final DeviceData d1 = data1.get(0);
        final DeviceData d2 = data2.get(0);

        // latest data has changed
        assert(d2.getEffectDate().isAfter(d1.getEffectDate()));
        assert(d2.getCreatedDate().isAfter(d1.getCreatedDate()));
        assertEquals("100", d1.getDisplayValue());
        assertEquals("120", d2.getDisplayValue());
    }

    @Test
    @Rollback
    public void updateDevice() throws Exception {
        // insert first device
        assertDeviceCountInDb(0);
        postJson("update_device_1.json", METADATA_PATH);
        assertDeviceCountInDb(1);
        final Device d1 = v2DeviceRepository.getOne("t1");
        assertNotNull(d1);
        final ZonedDateTime update1 = d1.getUpdatedDate();
        final String direction1 = d1.getDirection();

        // update device
        postJson("update_device_2.json", METADATA_PATH);
        assertDeviceCountInDb(1);
        final Device d2 = v2DeviceRepository.getOne("t1");
        assertNotNull(d2);

        // data has changed
        assert(d2.getUpdatedDate().isAfter(update1));
        assertEquals("KASVAVA", direction1);
        assertEquals("LASKEVA", d2.getDirection());
    }
}
