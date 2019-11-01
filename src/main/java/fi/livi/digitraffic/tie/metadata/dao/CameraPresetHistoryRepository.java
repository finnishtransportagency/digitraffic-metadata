package fi.livi.digitraffic.tie.metadata.dao;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.dto.camera.PresetHistoryStatusDto;
import fi.livi.digitraffic.tie.metadata.model.CameraPresetHistory;
import fi.livi.digitraffic.tie.metadata.model.CameraPresetHistoryPK;

@Repository
public interface CameraPresetHistoryRepository extends JpaRepository<CameraPresetHistory, CameraPresetHistoryPK> {

    Optional<CameraPresetHistory> findByIdPresetIdAndIdVersionId(final String presetId, final String versionId);

    @Query(value = "SELECT DISTINCT ON (preset_id)\n" +
                   "history.*\n" +
                   "FROM camera_preset_history history\n" +
                   "WHERE history.preset_id = :presetId\n" +
                   "ORDER BY history.preset_id, history.last_modified DESC",
           nativeQuery = true)
    Optional<CameraPresetHistory> findLatestByPresetId(final String presetId);

    @Query(value = "SELECT DISTINCT ON (preset_id)\n" +
                   "history.*\n" +
                   "FROM camera_preset_history history\n" +
                   "WHERE history.publishable = true\n" +
                   "  AND history.preset_id = :presetId\n" +
                   "  AND history.last_modified <= :atTime\n" +
                   "  AND history.last_modified >= :maxTime\n" +
                   "ORDER BY history.preset_id, history.last_modified DESC",
           nativeQuery = true)
    Optional<CameraPresetHistory> findLatestPublishableByPresetIdAndTimeOrderByPresetIdAndLastModifiedDesc(
        final String presetId, final Instant atTime, final Instant maxTime);

    @Query(value = "SELECT DISTINCT ON (preset_id)\n" +
                   "history.*\n" +
                   "FROM camera_preset_history history\n" +
                   "WHERE history.publishable = true\n" +
                   "  AND history.camera_id = :cameraId\n" +
                   "  AND history.last_modified <= :atTime\n" +
                   "  AND history.last_modified >= :maxTime\n" +
                   "ORDER BY history.preset_id, history.last_modified DESC",
           nativeQuery = true)
    List<CameraPresetHistory> findLatestPublishableByCameraIdAndTimeOrderByPresetIdAndLastModifiedDesc(
        final String cameraId, final Instant atTime, final Instant maxTime);

    @Query(value = "SELECT history.*\n" +
                   "FROM camera_preset_history history\n" +
                   "WHERE history.publishable = true\n" +
                   "  AND history.preset_id = :presetId\n" +
                   "  AND history.last_modified >= :maxTime\n" +
                   "ORDER BY history.preset_id, history.last_modified DESC",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<CameraPresetHistory> findAllPublishableByPresetIdOrderByLastModifiedDesc(final String presetId, Instant maxTime);

    @Query(value = "SELECT history.*\n" +
                   "FROM camera_preset_history history\n" +
                   "WHERE history.publishable = true\n" +
                   "  AND history.camera_id = :cameraId\n" +
                   "  AND history.last_modified >= :maxTime\n" +
                   "ORDER BY history.preset_id, history.last_modified DESC",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<CameraPresetHistory> findAllPublishableByCameraIdOrderByLastModifiedDesc(final String cameraId, final Instant maxTime);

    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<CameraPresetHistory> findByIdPresetIdOrderByLastModifiedAsc(final String presetId);

    @Modifying
    int deleteByIdPresetId(final String presetId);

    boolean existsByIdPresetId(final String presetId);

    boolean existsByCameraId(final String cameraId);

    @Modifying
    @Query(value = "UPDATE camera_preset_history history\n" +
                   "SET publishable = :isPublic\n" +
                   "WHERE history.publishable <> :isPublic\n" +
                   "  AND history.last_modified >= :fromTime\n" +
                   "  AND history.camera_id = :cameraId",
           nativeQuery = true)
    void updatePresetHistoryPublicityForCameraId(final String cameraId, final boolean isPublic, final Instant fromTime);


    @Query(value = "SELECT h.camera_id as cameraId, " +
                   "       h.preset_id as presetId," +
                   "       bool_or(h.publishable) as history\n"+
                   "FROM camera_preset_history h\n"+
                   "WHERE h.last_modified >= :fromTime\n" +
                   "  AND h.last_modified <= :toTime\n" +
                   "GROUP BY h.camera_id, h.preset_id\n"+
                   "ORDER BY h.camera_id, h.preset_id",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<PresetHistoryStatusDto> findCameraPresetHistoryStatusByTime(final Instant fromTime, final Instant toTime);

    @Query(value = "SELECT h.camera_id as cameraId, " +
                   "       h.preset_id as presetId," +
                   "       bool_or(h.publishable) as history\n"+
                   "FROM camera_preset_history h\n"+
                   "WHERE h.last_modified >= :fromTime\n" +
                   "  AND h.last_modified <= :toTime\n" +
                   "  AND h.preset_id = :presetId\n" +
                   "GROUP BY h.camera_id, h.preset_id\n"+
                   "ORDER BY h.camera_id, h.preset_id",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<PresetHistoryStatusDto> findCameraPresetHistoryStatusByPresetIdAndTime(String presetId, Instant fromTime, Instant toTime);

    @Query(value = "SELECT h.camera_id as cameraId, " +
                   "       h.preset_id as presetId," +
                   "       bool_or(h.publishable) as history\n"+
                   "FROM camera_preset_history h\n"+
                   "WHERE h.last_modified >= :fromTime\n" +
                   "  AND h.last_modified <= :toTime\n" +
                   "  AND h.camera_id = :cameraId\n" +
                   "GROUP BY h.camera_id, h.preset_id\n"+
                   "ORDER BY h.camera_id, h.preset_id",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<PresetHistoryStatusDto> findCameraPresetHistoryStatusByCameraIdAndTime(String cameraId, Instant fromTime, Instant toTime);
}