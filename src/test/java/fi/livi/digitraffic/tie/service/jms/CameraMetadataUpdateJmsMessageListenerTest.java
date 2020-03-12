package fi.livi.digitraffic.tie.service.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.verification.VerificationModeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.Julkisuus;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.JulkisuusTaso;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.service.CameraMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.jms.marshaller.CameraMetadataUpdatedMessageMarshaller;
import fi.livi.digitraffic.tie.service.v1.camera.CameraImageUpdateService;
import fi.livi.digitraffic.tie.service.v1.camera.CameraMetadataMessageHandler;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetService;
import fi.livi.digitraffic.tie.service.v1.lotju.LotjuCameraStationMetadataClient;

public class CameraMetadataUpdateJmsMessageListenerTest extends AbstractJmsMessageListenerTest {
    private static final Logger log = LoggerFactory.getLogger(CameraMetadataUpdateJmsMessageListenerTest.class);

    @Autowired
    private CameraMetadataMessageHandler cameraMetadataMessageHandler;

    @Autowired
    private Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    private CameraPresetService cameraPresetService;

    @MockBean
    private LotjuCameraStationMetadataClient lotjuCameraStationMetadataClient;

    @SpyBean
    private CameraImageUpdateService cameraImageUpdateService;

    private JMSMessageListener.JMSDataUpdater<CameraMetadataUpdatedMessageDto> dataUpdater;
    private JMSMessageListener cameraMetadataJmsMessageListener;

    @Before
    public void initListener() {
        // Create listener
        this.dataUpdater = (data) -> cameraMetadataMessageHandler.updateCameraMetadata(data);
        cameraMetadataJmsMessageListener = new JMSMessageListener(new CameraMetadataUpdatedMessageMarshaller(jaxb2Marshaller), dataUpdater, false, log);
    }

    @Test
    public void cameraMetadataUpdateReceiveMessages() {

        doNothing().when(cameraImageUpdateService).hideCurrentImageForPreset(any(CameraPreset.class));

        // Create camera with preset to lotju
        final KameraVO kamera_T1 = createKamera(Instant.now());
        final List<EsiasentoVO> esiasentos_T1 = createEsiasentos(kamera_T1.getId(), 2);
        final EsiasentoVO esiasento_T1_1 = esiasentos_T1.get(0);
        final EsiasentoVO esiasento_T1_2 = esiasentos_T1.get(1);

        // First camera with 1 preset
        when(lotjuCameraStationMetadataClient.getKamera(kamera_T1.getId())).thenReturn(kamera_T1);
        when(lotjuCameraStationMetadataClient.getEsiasentos(kamera_T1.getId())).thenReturn(Collections.singletonList(esiasentos_T1.get(0)));
        sendMessage(getCameraAddMessage(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getKamera(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getEsiasentos(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(0)).getEsiasento(eq(esiasento_T1_2.getId()));
        verify(cameraImageUpdateService, VerificationModeFactory.times(0)).hideCurrentImagesForCamera(any(RoadStation.class));

        reset(lotjuCameraStationMetadataClient);
        {
            final CameraPreset preset1 = cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_1.getId());
            assertNotNull(preset1);
            assertTrue(preset1.isPublic());
            assertTrue(preset1.getRoadStation().isPublicNow());
            assertNull(cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_2.getId()));
            assertEquals(PRESET_PRESENTATION_NAME + esiasento_T1_1.getId(), preset1.getPresetName1());
        }

        // Update preset 1 to secret
        esiasento_T1_1.setJulkisuus(Julkisuus.VALIAIKAISESTI_SALAINEN);
        esiasento_T1_1.setNimiEsitys("Foo Bar");
        when(lotjuCameraStationMetadataClient.getKamera(kamera_T1.getId())).thenReturn(kamera_T1);
        when(lotjuCameraStationMetadataClient.getEsiasento(esiasento_T1_1.getId())).thenReturn(esiasento_T1_1);
        sendMessage(getPresetUpdateMessage(esiasento_T1_1.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getKamera(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getEsiasento(eq(esiasento_T1_1.getId()));
        verify(lotjuCameraStationMetadataClient, times(0)).getEsiasento(eq(esiasento_T1_2.getId()));
        verify(cameraImageUpdateService, VerificationModeFactory.times(0)).hideCurrentImagesForCamera(any(RoadStation.class));

        reset(lotjuCameraStationMetadataClient);
        {
            final CameraPreset preset1 = cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_1.getId());
            assertNotNull(preset1);
            assertNull(cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_2.getId()));
            assertEquals("Foo Bar", preset1.getPresetName1());
            assertFalse(preset1.isPublic());
            assertTrue(preset1.getRoadStation().isPublicNow());
        }

        // Create preset 2
        when(lotjuCameraStationMetadataClient.getKamera(kamera_T1.getId())).thenReturn(kamera_T1);
        when(lotjuCameraStationMetadataClient.getEsiasento(esiasento_T1_2.getId())).thenReturn(esiasento_T1_2);
        when(lotjuCameraStationMetadataClient.getEsiasentos(kamera_T1.getId())).thenReturn(esiasentos_T1);
        sendMessage(getPresetAddMessage(esiasento_T1_2.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getKamera(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(0)).getEsiasento(eq(esiasento_T1_1.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getEsiasento(eq(esiasento_T1_2.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getEsiasentos(eq(kamera_T1.getId()));
        verify(cameraImageUpdateService, VerificationModeFactory.times(0)).hideCurrentImagesForCamera(any(RoadStation.class));

        reset(lotjuCameraStationMetadataClient);
        {
            final CameraPreset preset1 = cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_1.getId());
            final CameraPreset preset2 = cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_2.getId());
            assertNotNull(preset1);
            assertNotNull(preset2);

            assertEquals(kamera_T1.getId(), preset1.getRoadStation().getLotjuId());
            assertEquals(kamera_T1.getId(), preset2.getRoadStation().getLotjuId());
            assertEquals(esiasento_T1_1.getId(), preset1.getLotjuId());
            assertEquals(esiasento_T1_2.getId(), preset2.getLotjuId());
            assertFalse(preset1.isPublic());
            assertTrue(preset2.isPublic());
            assertEquals("Foo Bar", preset1.getPresetName1());
            assertEquals(PRESET_PRESENTATION_NAME + esiasento_T1_2.getId(), preset2.getPresetName1());
        }

        // Update camera to secret
        kamera_T1.setJulkisuus(createKameraJulkisuus(Instant.now(), JulkisuusTaso.VALIAIKAISESTI_SALAINEN));
        when(lotjuCameraStationMetadataClient.getKamera(kamera_T1.getId())).thenReturn(kamera_T1);
        sendMessage(getCameraUpdateMessage(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getKamera(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(0)).getEsiasentos(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(0)).getEsiasento(eq(esiasento_T1_2.getId()));
        // camera T1 has 2 public presets, camera changes to secret -> 2 presets to secret
        verify(cameraImageUpdateService, VerificationModeFactory.times(1)).hideCurrentImagesForCamera(argThat(rs -> rs.getLotjuId().equals(kamera_T1.getId())));
        verify(cameraImageUpdateService, VerificationModeFactory.times(2)).hideCurrentImageForPreset(any(CameraPreset.class));
        verify(cameraImageUpdateService, VerificationModeFactory.times(0)).hideCurrentImagesForCamera(argThat(rs -> !rs.getLotjuId().equals(kamera_T1.getId())));

        reset(lotjuCameraStationMetadataClient);
        {
            final CameraPreset preset1 = cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_1.getId());
            assertFalse(preset1.getRoadStation().isPublicNow());
        }

        // Also tieosoite will trigger update. Let's make station public again
        kamera_T1.setJulkisuus(createKameraJulkisuus(Instant.now(), JulkisuusTaso.JULKINEN));
        kamera_T1.getTieosoite().setUrakkaAlue("Foo");
        when(lotjuCameraStationMetadataClient.getKamera(kamera_T1.getId())).thenReturn(kamera_T1);
        sendMessage(getTieosoiteUpdateMessageXml(Tyyppi.PAIVITYS, kamera_T1.getId(), kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(1)).getKamera(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(0)).getEsiasentos(eq(kamera_T1.getId()));
        verify(lotjuCameraStationMetadataClient, times(0)).getEsiasento(eq(esiasento_T1_2.getId()));

        // camera T1 has 2 public presets, camera changes to secret -> 2 presets to secret
        verify(cameraImageUpdateService, VerificationModeFactory.times(1)).hideCurrentImagesForCamera(argThat(rs -> rs.getLotjuId().equals(kamera_T1.getId())));
        verify(cameraImageUpdateService, VerificationModeFactory.times(2)).hideCurrentImageForPreset(any(CameraPreset.class));
        verify(cameraImageUpdateService, VerificationModeFactory.times(0)).hideCurrentImagesForCamera(argThat(rs -> !rs.getLotjuId().equals(kamera_T1.getId())));

        reset(lotjuCameraStationMetadataClient);
        {
            final CameraPreset preset1 = cameraPresetService.findCameraPresetByLotjuId(esiasento_T1_1.getId());
            assertTrue(preset1.getRoadStation().isPublicNow());
            assertEquals("Foo", preset1.getRoadStation().getRoadAddress().getContractArea());
        }
    }

    private enum Tyyppi {
        PAIVITYS,
        LISAYS,
        POISTO
    }

    private enum Entiteetti {
        KAMERA,
        VIDEOPALVELIN,
        KAMERAKOKOONPANO,
        ESIASENTO,
        MASTER_TIETOVARASTO,
        TIEOSOITE
    }

    private static String getUpdateMessageXml(final Tyyppi tyyppi, final Entiteetti entiteetti, final long lotjuId) {
        return String.format(
            "<metatietomuutos tyyppi=\"%s\" aika=\"2019-10-21T11:00:00\" entiteetti=\"%s\" id=\"%d\">\n" +
            "    <asemat />\n" +
            "</metatietomuutos>",
            tyyppi.name(), entiteetti.name(), lotjuId);
    }

    private static String getTieosoiteUpdateMessageXml(final Tyyppi tyyppi, final long...lotjuIds) {
        StringBuilder ids = new StringBuilder();
        for(long lotjuId : lotjuIds) {
            ids.append("        <id>" + lotjuId + "</id>\n");
        }
        return String.format(
            "<metatietomuutos tyyppi=\"%s\" aika=\"2019-10-21T11:00:00\" entiteetti=\"TIEOSOITE\" id=\"-1\">\n" +
            "    <asemat>\n" +
            "%s" +
            "    </asemat>\n" +
            "</metatietomuutos>",
            tyyppi.name(), ids.toString());
    }

    private static String getCameraUpdateMessage(final long lotjuId) {
        return getUpdateMessageXml(Tyyppi.PAIVITYS, Entiteetti.KAMERA, lotjuId);
    }

    private static String getCameraAddMessage(final long lotjuId) {
        return getUpdateMessageXml(Tyyppi.LISAYS, Entiteetti.KAMERA, lotjuId);
    }

    private static String getPresetUpdateMessage(final long lotjuId) {
        return getUpdateMessageXml(Tyyppi.PAIVITYS, Entiteetti.ESIASENTO, lotjuId);
    }

    private static String getPresetAddMessage(final long lotjuId) {
        return getUpdateMessageXml(Tyyppi.LISAYS, Entiteetti.ESIASENTO, lotjuId);
    }

    private static String getRoadAddressUpdateMessage(final long lotjuId) {
        return getUpdateMessageXml(Tyyppi.PAIVITYS, Entiteetti.TIEOSOITE, lotjuId);
    }

    private void sendMessage(final String message) {
        try {
            cameraMetadataJmsMessageListener.onMessage(createTextMessage(message, null));
        } catch (final Exception e) {
            log.error("Error with message:\n" + message);
            throw e;
        }
    }
}
