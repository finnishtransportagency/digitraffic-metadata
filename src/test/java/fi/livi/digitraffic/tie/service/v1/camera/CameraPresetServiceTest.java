package fi.livi.digitraffic.tie.service.v1.camera;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;

public class CameraPresetServiceTest extends AbstractServiceTest {

    @Autowired
    private CameraPresetService cameraPresetService;

    @Test
    public void testFindAll() {
        final Map<Long, CameraPreset> all = cameraPresetService.findAllCameraPresetsMappedByLotjuId();
        assertTrue(all.size() > 0);
    }
}
