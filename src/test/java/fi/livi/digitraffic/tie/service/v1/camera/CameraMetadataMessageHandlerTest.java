package fi.livi.digitraffic.tie.service.v1.camera;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.service.CameraMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.CameraMetadataUpdatedMessageDto.EntityType;
import fi.livi.digitraffic.tie.service.MetadataUpdatedMessageDto.UpdateType;

public class CameraMetadataMessageHandlerTest extends AbstractDaemonTestWithoutS3 {

    @Autowired
    private CameraMetadataMessageHandler cameraMetadataMessageHandler;

    @MockBean
    CameraStationUpdater cameraStationUpdater;

    @Test
    public void cameraMessagesShouldTriggerUpdate() {
        for (UpdateType updateType : UpdateType.values()) {
            verifyCameraMessageTriggersUpdate(updateType);
        }
    }

    @Test
    public void presetMessagesShouldTriggerUpdate() {
        for (UpdateType updateType : UpdateType.values()) {
            verifyPresetaMessageTriggersUpdate(updateType);
        }
    }

    @Test
    public void roadAddressMessagesShouldTriggerUpdate() {
        for (UpdateType updateType : UpdateType.values()) {
            verifyRoadAddressMessageTriggersUpdate(updateType);
        }
    }

    @Test
    public void otherMessagesShouldNotTriggerUpdate() {
        ArrayList<EntityType> entityTypes = new ArrayList<>(Arrays.asList(EntityType.values()));
        entityTypes.removeAll(Arrays.asList(EntityType.CAMERA, EntityType.PRESET, EntityType.ROAD_ADDRESS));
        for (EntityType entityType : entityTypes) {
            for (UpdateType updateType : UpdateType.values()) {
                verifyMessageWontTriggersUpdate(updateType, entityType);
            }
        }
    }

    private void verifyMessageWontTriggersUpdate(final UpdateType updateType,
                                                 final EntityType entityType) {
        cameraMetadataMessageHandler.updateCameraMetadata(createMessage(entityType, updateType));
        verifyNoInteractions(cameraStationUpdater);
    }

    private void verifyCameraMessageTriggersUpdate(final UpdateType updateType) {
        when(cameraStationUpdater.updateCameraStationFromJms(1L)).thenReturn(true);
        cameraMetadataMessageHandler.updateCameraMetadata(createMessage(EntityType.CAMERA, updateType));
        verify(cameraStationUpdater, times(1)).updateCameraStationFromJms(eq(1L));
        verifyNoMoreInteractions(cameraStationUpdater);
        reset(cameraStationUpdater);
    }

    private void verifyPresetaMessageTriggersUpdate(final UpdateType updateType) {
        when(cameraStationUpdater.updateCameraPresetFromJms(1L)).thenReturn(true);
        cameraMetadataMessageHandler.updateCameraMetadata(createMessage(EntityType.PRESET, updateType));
        verify(cameraStationUpdater, times(1)).updateCameraPresetFromJms(eq(1L));
        verifyNoMoreInteractions(cameraStationUpdater);
        reset(cameraStationUpdater);
    }

    private void verifyRoadAddressMessageTriggersUpdate(final UpdateType updateType) {
        when(cameraStationUpdater.updateCameraStationFromJms(2L)).thenReturn(true);
        cameraMetadataMessageHandler.updateCameraMetadata(createMessage(EntityType.ROAD_ADDRESS, updateType));
        verify(cameraStationUpdater, times(1)).updateCameraStationFromJms(eq(2L));
        verifyNoMoreInteractions(cameraStationUpdater);
        reset(cameraStationUpdater);
    }

    private List<CameraMetadataUpdatedMessageDto> createMessage(final EntityType entityType, final UpdateType updateType) {
        return Collections.singletonList(
            new CameraMetadataUpdatedMessageDto(1L, Collections.singleton(2L), updateType, Instant.now(), entityType));
    }
}
