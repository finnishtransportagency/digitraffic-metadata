package fi.livi.digitraffic.tie.metadata.dao;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.metadata.model.CameraPreset;

@Repository
public interface CameraPresetRepository extends JpaRepository<CameraPreset, Long> {

    List<CameraPreset> findByPublishableIsTrueAndRoadStationPublishableIsTrueOrderByPresetId();

    List<CameraPreset> findByCameraIdAndPublishableIsTrueAndRoadStationPublishableIsTrueOrderByPresetId(final String cameraId);


    @Query(value =
            "SELECT CP.*\n" +
            "FROM CAMERA_PRESET CP\n" +
            "WHERE NOT EXISTS (\n" +
            "  SELECT NULL\n" +
            "  FROM ROAD_STATION RS\n" +
            "  WHERE CP.ROAD_STATION_ID = RS.ID\n" +
            "    AND RS.TYPE = 3" +
            ")",
            nativeQuery = true)
    List<CameraPreset> findAllCameraPresetsWithoutRoadStation();


    @Query(value =
            "SELECT MAX(CP.PIC_LAST_MODIFIED) UPDATED\n" +
            "FROM CAMERA_PRESET CP",
            nativeQuery = true)
    LocalDateTime getLatestMeasurementTime();

    CameraPreset findCameraPresetByPresetId(String presetId);

    List<CameraPreset> findByLotjuIdIn(Collection<Long> presetIds);

    List<CameraPreset> findByCameraLotjuIdIsNullOrLotjuIdIsNull();
}
