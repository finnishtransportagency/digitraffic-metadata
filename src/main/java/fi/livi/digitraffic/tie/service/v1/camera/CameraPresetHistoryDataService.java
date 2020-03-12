package fi.livi.digitraffic.tie.service.v1.camera;

import static fi.livi.digitraffic.tie.helper.DateHelper.getZonedDateTimeNowAtUtc;
import static fi.livi.digitraffic.tie.helper.DateHelper.toZonedDateTimeAtUtc;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.conf.amazon.WeathercamS3Properties;
import fi.livi.digitraffic.tie.dao.v1.CameraPresetHistoryRepository;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryChangesDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryPresenceDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryPresencesDto;
import fi.livi.digitraffic.tie.dto.v1.camera.PresetHistoryChangesDto;
import fi.livi.digitraffic.tie.dto.v1.camera.PresetHistoryDataDto;
import fi.livi.digitraffic.tie.dto.v1.camera.PresetHistoryDto;
import fi.livi.digitraffic.tie.dto.v1.camera.PresetHistoryPresenceDto;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPresetHistory;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

@Service
public class CameraPresetHistoryDataService {
    private static final Logger log = LoggerFactory.getLogger(CameraPresetService.class);
    private final CameraPresetHistoryRepository cameraPresetHistoryRepository;
    private final WeathercamS3Properties weathercamS3Properties;

    public static final int MAX_IDS_SIZE = 5000;

    public enum HistoryStatus {
        PUBLIC("History version found and it's publishable"),
        SECRET("History version found but it's not publishable"),
        NOT_FOUND("No history found for preset at all"),
        TOO_OLD("History version is over 24 h old and for that reason not publishable"),
        ILLEGAL_KEY("Preset image name did not match correct regex format ^C([0-9]{7})\\.jpg$ for S3 key");

        private final String description;

        HistoryStatus(final String description) {
            this.description = description;
        }
    }

    @Autowired
    public CameraPresetHistoryDataService(final CameraPresetHistoryRepository cameraPresetHistoryRepository,
                                          final WeathercamS3Properties weathercamS3Properties) {
        this.cameraPresetHistoryRepository = cameraPresetHistoryRepository;
        this.weathercamS3Properties = weathercamS3Properties;
    }

    @Transactional(readOnly = true)
    public CameraPresetHistory findHistoryVersionInclSecretInternal(final String presetId, final String versionId) {
        return cameraPresetHistoryRepository.findByIdPresetIdAndIdVersionId(presetId, versionId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<CameraHistoryDto> findCameraOrPresetPublicHistory(final List<String> cameraOrPresetIds, final ZonedDateTime atTime) {

        final List<String> cameraIds = parseCameraIds(cameraOrPresetIds);
        final List<String> presetIds = parsePresetIds(cameraOrPresetIds);
        checkAllParametersUsedAndNotTooLong(cameraOrPresetIds, cameraIds, presetIds);

        final List<CameraPresetHistory> history =
            atTime != null ?
                cameraPresetHistoryRepository.findLatestPublishableByCameraAndPresetIdsAndTimeOrderByPresetIdAndLastModifiedDesc(
                    cameraIds, presetIds, atTime.toInstant(), getOldestTimeLimit().toInstant()) :
                cameraPresetHistoryRepository.findAllPublishableByCameraAndPresetIdsOrderByPresetIdAndLastModifiedDesc(
                    cameraIds, presetIds, getOldestTimeLimit().toInstant());

        return convertToCameraHistory(history);
    }

    /**
     *
     * @param after Return changes after timestamp
     * @param cameraOrPresetIds List of possible camera and/or preset ids to find. If list is empty all will be included.
     * @return History changes ordered by presetId and lastModified in ascending order
     */
    @Transactional(readOnly = true)
    public CameraHistoryChangesDto findCameraOrPresetHistoryChangesAfter(final ZonedDateTime after, final List<String> cameraOrPresetIds) {

        final List<String> cameraIds = parseCameraIds(cameraOrPresetIds);
        final List<String> presetIds = parsePresetIds(cameraOrPresetIds);
        checkAllParametersUsedAndNotTooLong(cameraOrPresetIds, cameraIds, presetIds);

        final Instant latestChange = cameraPresetHistoryRepository.getLatestChangesTime();
        final List<PresetHistoryChangesDto> changes =
            cameraPresetHistoryRepository.findCameraPresetHistoryChangesAfter(after.toInstant(), cameraIds, presetIds);

        return new CameraHistoryChangesDto(latestChange, changes);
    }

    private void checkAllParametersUsedAndNotTooLong(final List<String> cameraOrPresetIds,
                                                     final List<String> usedCameraIds, final List<String> usedPresetIds) {

        checkCameraOrPresetIdsMaxSize(cameraOrPresetIds);

        final List<String> illegalIds = cameraOrPresetIds.stream().filter(id -> !usedCameraIds.contains(id) && !usedPresetIds.contains(id)).collect(Collectors.toList());

        if (!illegalIds.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("Parameter camera or presetId should be either 6 or 8 chars long. Illegal parameters: %s.",
                    String.join(", ", illegalIds)));
        }
    }

    private void checkCameraOrPresetIdsMaxSize(final List<String> cameraOrPresetIds) {
        if (cameraOrPresetIds.size() > MAX_IDS_SIZE) {
            throw new IllegalArgumentException(
                String.format("Too long list of id parameters. Maximum is %d pcs and was %d pcs.",
                    MAX_IDS_SIZE, cameraOrPresetIds.size()));
        }
    }

    /**
     * Finds cameras' and presets' history status. History status tells if
     * history exists for given time interval.
     *
     * @param cameraOrPresetId camera or preset id to find
     * @param fromTime inclusive
     * @param toTime inclusive
     * @return Presets history presences
     */
    @Transactional(readOnly = true)
    public CameraHistoryPresencesDto findCameraOrPresetHistoryPresences(final String cameraOrPresetId, final ZonedDateTime fromTime,
                                                                        final ZonedDateTime toTime) {

        if (cameraOrPresetId == null) {
            return findCameraHistoryPresences(checkAndFixFromTime(fromTime), checkAndFixToTime(toTime));
        } else if (isPresetId(cameraOrPresetId)) {
            return findCameraPresetHistoryPresences(cameraOrPresetId, checkAndFixFromTime(fromTime), checkAndFixToTime(toTime));
        } else if (isCameraId(cameraOrPresetId)) {
            return findCameraHistoryPresences(cameraOrPresetId, checkAndFixFromTime(fromTime), checkAndFixToTime(toTime));
        } else {
            throw new IllegalArgumentException(String.format("Parameter cameraOrPresetId should be either 6 or 8 chars long. Was %d long.",
                cameraOrPresetId.length()));
        }
    }

    private ZonedDateTime checkAndFixFromTime(final ZonedDateTime fromTime) {
        final ZonedDateTime fromLimit = getOldestTimeLimit();
        if (fromTime == null || fromTime.isBefore(fromLimit)) {
            return fromLimit;
        }
        return toZonedDateTimeAtUtc(fromTime);
    }

    private ZonedDateTime checkAndFixToTime(final ZonedDateTime toTime) {
        if (toTime == null) {
            return getZonedDateTimeNowAtUtc();
        }
        return toZonedDateTimeAtUtc(toTime);
    }

    private CameraHistoryPresencesDto findCameraHistoryPresences(final ZonedDateTime fromTime, final ZonedDateTime toTime) {
        List<PresetHistoryPresenceDto> presetsHistoryStatuses =
            cameraPresetHistoryRepository.findCameraPresetHistoryPresenceByTime(fromTime.toInstant(), toTime.toInstant(),
                                                                              getOldestTimeLimit().toInstant());
        return convertToCameraHistoryPresences(presetsHistoryStatuses, fromTime, toTime);
    }

    private CameraHistoryPresencesDto findCameraHistoryPresences(final String cameraId, final ZonedDateTime fromTime, final ZonedDateTime toTime) {
        if (!cameraPresetHistoryRepository.existsByCameraId(cameraId)) {
            throw new ObjectNotFoundException("CameraHistory", cameraId);
        }
        final List<PresetHistoryPresenceDto> history =
            cameraPresetHistoryRepository.findCameraPresetHistoryPresenceByCameraIdAndTime(cameraId, fromTime.toInstant(), toTime.toInstant(),
                                                                                         getOldestTimeLimit().toInstant());
        return convertToCameraHistoryPresences(history, fromTime, toTime);
    }

    private CameraHistoryPresencesDto findCameraPresetHistoryPresences(final String presetId, final ZonedDateTime fromTime, final ZonedDateTime toTime) {
        if (!cameraPresetHistoryRepository.existsByIdPresetId(presetId)) {
            throw new ObjectNotFoundException("CameraHistory", presetId);
        }
        final List<PresetHistoryPresenceDto> history =
            cameraPresetHistoryRepository.findCameraPresetHistoryPresenceByPresetIdAndTime(presetId, fromTime.toInstant(), toTime.toInstant(),
                                                                                           getOldestTimeLimit().toInstant());
        return convertToCameraHistoryPresences(history, fromTime, toTime);
    }

    private static CameraHistoryPresencesDto convertToCameraHistoryPresences(final List<PresetHistoryPresenceDto> presetsHistoryPresences,
                                                                             final ZonedDateTime fromTime, final ZonedDateTime toTime) {

        final Map<String, List<PresetHistoryPresenceDto>> cameraIdToPresetHistoryPresences = presetsHistoryPresences.parallelStream()
            .collect(Collectors.groupingBy(PresetHistoryPresenceDto::getCameraId));

        final List<CameraHistoryPresenceDto> result =
            cameraIdToPresetHistoryPresences.entrySet().stream().map(e -> new CameraHistoryPresenceDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(o -> o.cameraId))
                .collect(Collectors.toList());
        return new CameraHistoryPresencesDto(fromTime, toTime, result);
    }

    private List<CameraHistoryDto> convertToCameraHistory(List<CameraPresetHistory> history) {
        return history.stream()
            // Map<presetId, List<CameraPresetHistory>
            .collect(Collectors.groupingBy(CameraPresetHistory::getPresetId))
            // Map<cameraId, List<PresetHistoryDto>>
            .entrySet().stream().map(e -> convertToPresetHistory(e.getKey(), e.getValue()))
            .sorted(Comparator.comparing(PresetHistoryDto::getPresetId))
            .collect(Collectors.groupingBy(ph -> StringUtils.substring(ph.getPresetId(), 0, 6)))
            // List<CameraHistoryDto>
            .entrySet().stream().map(e -> new CameraHistoryDto(e.getKey(), e.getValue()))
            .sorted(Comparator.comparing(CameraHistoryDto::getCameraId))
            .collect(Collectors.toList());
    }

    private PresetHistoryDto convertToPresetHistory(final String presetId, final List<CameraPresetHistory> history) {
        return new PresetHistoryDto(
            presetId,
            history.stream().map(h ->
                new PresetHistoryDataDto(toZonedDateTimeAtUtc(h.getLastModified()),
                                         weathercamS3Properties.getPublicUrlForVersion(h.getPresetId(), h.getVersionId()),
                                         h.getSize()))
                .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public CameraPresetHistory findLatestWithPresetIdIncSecretInternal(final String presetId) {
        return cameraPresetHistoryRepository.findLatestByPresetId(presetId).orElse(null);
    }

    /** Orderer from oldest to newest
     * Only for internal use */
    @Transactional(readOnly = true)
    public List<CameraPresetHistory> findAllByPresetIdInclSecretAscInternal(final String presetId) {
        return cameraPresetHistoryRepository.findByIdPresetIdOrderByLastModifiedAsc(presetId);
    }

    /**
     * Resolves history status for given camera preset image version.
     *
     * @param presetImageName in regex format ^C([0-9]{7})\\.jpg$
     * @param versionId version string to check
     * @return PUBLIC - History version found and it's publishable <br />
     *         SECRET - History version found but it's not publishable <br />
     *         NOT_FOUND - No history found for preset at all <br />
     *         TOO_OLD - History version is over 24 h old and for that reason not publishable <br />
     *         ILLEGAL_KEY presetImageName did not match correct regex format ^C([0-9]{7})\\.jpg$ for S3 key
     */
    @Transactional(readOnly = true)
    public HistoryStatus resolveHistoryStatusForVersion(final String presetImageName, final String versionId) {

        if (!presetImageName.matches(weathercamS3Properties.getS3WeathercamKeyRegexp())) {
            return HistoryStatus.ILLEGAL_KEY;
        }
        // C1234567.jpg -> C1234567
        final CameraPresetHistory history = findHistoryVersionInclSecretInternal(weathercamS3Properties.getPresetIdFromImageName(presetImageName), versionId);
        final ZonedDateTime oldestLimit = getOldestTimeLimit();

        if (history == null) {
            return HistoryStatus.NOT_FOUND;
        } else if (!history.getPublishable()) {
            return HistoryStatus.SECRET;
        } else if (history.getLastModified().isBefore(oldestLimit)) {
            return HistoryStatus.TOO_OLD;
        }
        return HistoryStatus.PUBLIC;
    }



    private ZonedDateTime getOldestTimeLimit() {
        return getZonedDateTimeNowAtUtc().minus(weathercamS3Properties.getHistoryMaxAgeHours(), ChronoUnit.HOURS);
    }


    private List<String> parseCameraIds(final List<String> cameraOrPresetIds) {
        if (cameraOrPresetIds == null) {
            return Collections.emptyList();
        }
        return cameraOrPresetIds.stream().filter(CameraPresetHistoryDataService::isCameraId).collect(Collectors.toList());
    }

    private List<String> parsePresetIds(final List<String> cameraOrPresetIds) {
        if (cameraOrPresetIds == null) {
            return Collections.emptyList();
        }
        return cameraOrPresetIds.stream().filter(CameraPresetHistoryDataService::isPresetId).collect(Collectors.toList());
    }

    private static boolean isCameraId(final String cameraId) {
        return cameraId.length() == 6;
    }

    private static boolean isPresetId(final String presetId) {
        return presetId.length() == 8;
    }
}
