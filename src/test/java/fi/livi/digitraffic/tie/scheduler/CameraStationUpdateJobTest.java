package fi.livi.digitraffic.tie.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageUpdateService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraStationUpdater;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuKameraPerustiedotServiceEndpointMock;

public class CameraStationUpdateJobTest extends AbstractDaemonTestWithoutS3 {

    @Autowired
    private CameraStationUpdater cameraStationUpdater;

    @SpyBean
    private CameraPresetService cameraPresetService;

    @Autowired
    private LotjuKameraPerustiedotServiceEndpointMock lotjuKameraPerustiedotServiceMock;

    @SpyBean
    private CameraImageUpdateService cameraImageUpdateService;

    @Test
    public void testUpdateKameras() {

        lotjuKameraPerustiedotServiceMock.initStateAndService();

        // initial state cameras with lotjuId 443 has public and non public presets, 121 has 2 and 56 has 1 non public preset
        cameraStationUpdater.updateCameras();

        final List<CameraPreset> presetsInitial = cameraPresetService.findAllPublishableCameraPresets();
        final long cameraCountIntitial = presetsInitial.stream().map(cp -> cp.getCameraId()).distinct().count();
        // cameras with lotjuId 443 in collection, 56 (no public presets) and 121 removed temporary, 2 public with 5 presets
        assertEquals(3, cameraCountIntitial);
        // initial state cameras with lotjuId 443 has public and non public presets, 121 has 2 public and 56 has 1 non public preset -> 3 public, 2 has 5 public
        assertEquals(8, presetsInitial.size());
        presetsInitial.forEach(cp -> entityManager.detach(cp));

        doNothing().when(cameraImageUpdateService).hideCurrentImageForPreset(any(CameraPreset.class));

        // Update 121 camera to active, 56 removed and 2 not public
        lotjuKameraPerustiedotServiceMock.setStateAfterChange(true);
        cameraStationUpdater.updateCameras();

        // 2 has 5 public but camera is not public -> 5 presets to secret
        verify(cameraImageUpdateService, times(1)).hideCurrentImagesForCamera(argThat(rs -> rs.getLotjuId().equals(2L)));
        verify(cameraImageUpdateService, times(5)).hideCurrentImageForPreset(any(CameraPreset.class));
        verify(cameraImageUpdateService, times(0)).hideCurrentImagesForCamera(argThat(rs -> !rs.getLotjuId().equals(2L)));

        final List<CameraPreset> presetsAfterUpdate = cameraPresetService.findAllPublishableCameraPresets();
        final long cameraCountAfterUpdate = presetsAfterUpdate.stream().map(cp -> cp.getCameraId()).distinct().count();

        // 443 has 3 presets, 121 has 2
        assertEquals(2, cameraCountAfterUpdate);
        // cameras with lotjuId 443 has 2 and 56 has 1 preset
        assertEquals(5, presetsAfterUpdate.size());


        /*  Before:
            443: C0852001
            443: C0852002
            56:  C0155600
            After:
            443: C0852001
            443: C0852002/C0852003 resoluutio 704x576 -> 1200x900, viive 10 -> 20, Suunta 2 -> 3
            121: C0162801
            121: C0162802

         */

        // lotjuId 443
        // C0852001 is not public
        assertNotNull(findWithPresetId(presetsInitial, "C0852001"));
        assertNull(findWithPresetId(presetsInitial, "C0852002"));
        assertNull(findWithPresetId(presetsInitial, "C0852009")); // preset not exists
        // lotjuId 121
        assertNotNull(findWithPresetId(presetsInitial, "C0162801"));
        assertNotNull(findWithPresetId(presetsInitial, "C0162802"));
        // lotjuId 2
        assertNotNull(findWithPresetId(presetsInitial, "C0150200"));
        assertNotNull(findWithPresetId(presetsInitial, "C0150202"));
        assertNotNull(findWithPresetId(presetsInitial, "C0150201"));
        assertNotNull(findWithPresetId(presetsInitial, "C0150204"));
        assertNotNull(findWithPresetId(presetsInitial, "C0150209"));
        // 56: C0155600
        assertNull(findWithPresetId(presetsInitial, "C0155600"));

        // 443: C0852001, C0852002 C0852009
        assertNotNull(findWithPresetId(presetsAfterUpdate, "C0852001"));
        assertNotNull(findWithPresetId(presetsAfterUpdate, "C0852002"));
        assertNotNull(findWithPresetId(presetsAfterUpdate, "C0852009"));
        // 121: C0162801, C0162802
        assertNotNull(findWithPresetId(presetsAfterUpdate, "C0162801"));
        assertNotNull(findWithPresetId(presetsAfterUpdate, "C0162802"));
        // 2
        assertNull(findWithPresetId(presetsAfterUpdate, "C0150200"));
        assertNull(findWithPresetId(presetsAfterUpdate, "C0150202"));
        assertNull(findWithPresetId(presetsAfterUpdate, "C0150201"));
        assertNull(findWithPresetId(presetsAfterUpdate, "C0150204"));
        assertNull(findWithPresetId(presetsAfterUpdate, "C0150209"));
        // 56
        assertNull(findWithPresetId(presetsAfterUpdate, "C0155600")); // removed from data set

        // Test C0852002/C0852003 changes
        final CameraPreset before = findWithPresetId(presetsInitial, "C0852001");
        final CameraPreset after = findWithPresetId(presetsAfterUpdate, "C0852002");

        assertTrue(EqualsBuilder.reflectionEquals(before,
                                                  after,
             false,
             CameraPreset.class,
             "id", "compression", "direction", "resolution", "presetId", "directionCode", "imageUrl", "roadStation", "nearestWeatherStation"));
        assertEquals("704x576", before.getResolution());
        assertEquals("1200x900", after.getResolution());
        assertEquals("1", before.getDirection());
        assertEquals("2", after.getDirection());


        // 443 Kunta changed
        final CameraPreset beforeCam = findWithCameraId(presetsInitial, "C08520");
        final CameraPreset afterCam = findWithCameraId(presetsAfterUpdate, "C08520");
        assertEquals("Iisalmi", beforeCam.getRoadStation().getMunicipality());
        assertEquals("Iidensalmi", afterCam.getRoadStation().getMunicipality());

        assertEquals("keli", beforeCam.getRoadStation().getPurpose());
        assertEquals("liikenne", afterCam.getRoadStation().getPurpose());
    }

    private CameraPreset findWithPresetId(final List<CameraPreset> collection, final String presetId) {
        return collection.stream().filter(cp -> cp.getPresetId().equals(presetId)).findFirst().orElse( null);
    }

    private CameraPreset findWithCameraId(final List<CameraPreset> collection, final String cameraId) {
        return collection.stream().filter(cp -> cp.getCameraId().equals(cameraId)).findFirst().orElse(null);
    }
}
