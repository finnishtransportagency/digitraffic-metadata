package fi.livi.digitraffic.tie.service.v1.camera;

import static fi.livi.digitraffic.tie.helper.DateHelper.getZonedDateTimeNowAtUtc;
import static fi.livi.digitraffic.tie.helper.DateHelper.toZonedDateTimeAtUtc;

import java.net.URI;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dto.camera.CameraHistoryDto;
import fi.livi.digitraffic.tie.data.dto.camera.CameraHistoryPresenceDto;
import fi.livi.digitraffic.tie.data.dto.camera.CameraHistoryPresencesDto;
import fi.livi.digitraffic.tie.data.dto.camera.PresetHistoryDataDto;
import fi.livi.digitraffic.tie.data.dto.camera.PresetHistoryDto;
import fi.livi.digitraffic.tie.data.dto.camera.PresetHistoryPresenceDto;
import fi.livi.digitraffic.tie.data.service.CameraImageS3Writer;
import fi.livi.digitraffic.tie.data.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.dao.v1.CameraPresetHistoryRepository;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPresetHistory;
import fi.livi.digitraffic.tie.model.v1.RoadStation;

@Service
public class CameraPresetHistoryService {
    private static final Logger log = LoggerFactory.getLogger(CameraPresetService.class);
    private CameraPresetHistoryRepository cameraPresetHistoryRepository;
    private final String s3WeathercamKeyRegexp;
    private final String s3WeathercamBucketUrl;
    private final int historyMaxAgeHours;
    private final String weathercamBaseUrl;

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
    public CameraPresetHistoryService(final CameraPresetHistoryRepository cameraPresetHistoryRepository,
                                      @Value("${dt.amazon.s3.weathercam.bucketName}") final String s3WeathercamBucketName,
                                      @Value("${dt.amazon.s3.weathercam.region}") final String s3WeathercamRegion,
                                      @Value("${dt.amazon.s3.weathercam.key.regexp}") final String s3WeathercamKeyRegexp,
                                      @Value("${dt.amazon.s3.weathercam.history.maxAgeHours}") final int historyMaxAgeHours,
                                      @Value("${weathercam.baseUrl}") final String weathercamBaseUrl) {
        this.cameraPresetHistoryRepository = cameraPresetHistoryRepository;
        this.s3WeathercamKeyRegexp = s3WeathercamKeyRegexp;
        this.historyMaxAgeHours = historyMaxAgeHours;
        this.weathercamBaseUrl = weathercamBaseUrl;
        this.s3WeathercamBucketUrl = createS3WeathercamBucketUrl(s3WeathercamBucketName, s3WeathercamRegion);
    }

    private String createS3WeathercamBucketUrl(
            String s3WeathercamBucketName,
            String s3WeathercamRegion) {
        return String.format("http://%s.s3-%s.amazonaws.com", s3WeathercamBucketName, s3WeathercamRegion);
    }

    @Transactional
    public void saveHistory(final CameraPresetHistory history) {
        cameraPresetHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public CameraPresetHistory findHistoryVersionInclSecret(final String presetId, final String versionId) {
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
                    fixEmptyIdsList(cameraIds), fixEmptyIdsList(presetIds), atTime.toInstant(), getOldestTimeLimit().toInstant()) :
                cameraPresetHistoryRepository.findAllPublishableByCameraAndPresetIdsOrderByPresetIdAndLastModifiedDesc(
                    fixEmptyIdsList(cameraIds), fixEmptyIdsList(presetIds), getOldestTimeLimit().toInstant());

        return convertToCameraHistory(history);
    }

    private void checkAllParametersUsedAndNotTooLong(final List<String> cameraOrPresetIds,
                                                     final List<String> usedCameraIds, final List<String> usedPresetIds) {

        if (cameraOrPresetIds.size() > MAX_IDS_SIZE) {
            throw new IllegalArgumentException(
                String.format("Too long list of id parameters. Maximum is %d pcs and was %d pcs.",
                              MAX_IDS_SIZE, cameraOrPresetIds.size()));
        }

        final List<String> illegalIds = cameraOrPresetIds.stream().filter(id -> !usedCameraIds.contains(id) && !usedPresetIds.contains(id)).collect(Collectors.toList());

        if (!illegalIds.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("Parameter camera or presetId should be either 6 or 8 chars long. Illegal parameters: %s.",
                    illegalIds.stream().collect(Collectors.joining(", "))));
        }
    }

    private List<String> fixEmptyIdsList(List<String> cameraOrPresetIds) {
        return cameraOrPresetIds.isEmpty() ? Collections.singletonList("NONE") : cameraOrPresetIds;
    }

    private List<String> parseCameraIds(final List<String> cameraOrPresetIds) {
        return cameraOrPresetIds.stream().filter(id -> id.length() == 6).collect(Collectors.toList());
    }
    private List<String> parsePresetIds(final List<String> cameraOrPresetIds) {
        return cameraOrPresetIds.stream().filter(id -> id.length() == 8).collect(Collectors.toList());
    }

    private ZonedDateTime getOldestTimeLimit() {
        return getZonedDateTimeNowAtUtc().minus(historyMaxAgeHours, ChronoUnit.HOURS);
    }

    /**
     * Finds cameras' and presets' history status. History status tells if
     * history exists for given time interval.
     *
     * @param cameraOrPresetId
     * @param fromTime
     * @param toTime
     * @return
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
                new PresetHistoryDataDto(DateHelper.toZonedDateTimeAtUtc(h.getLastModified()),
                                         createPublicUrlForVersion(h.getPresetId(), h.getVersionId()),
                                         h.getSize()))
                .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public CameraPresetHistory findLatestWithPresetIdIncSecret(final String presetId) {
        return cameraPresetHistoryRepository.findLatestByPresetId(presetId).orElse(null);
    }

    /** Orderer from oldest to newest
     * Only for internal use */
    @Transactional(readOnly = true)
    public List<CameraPresetHistory> findAllByPresetIdInclSecretAsc(final String presetId) {
        return cameraPresetHistoryRepository.findByIdPresetIdOrderByLastModifiedAsc(presetId);
    }

    @Transactional
    public int deleteAllWithPresetId(final String presetId) {
        return cameraPresetHistoryRepository.deleteByIdPresetId(presetId);
    }

    @Transactional
    public void updatePresetHistoryPublicityForCamera(final RoadStation rs) {
        // If statTime is null it means now -> no history to update or
        // if startTime is in the future -> no history to update
        if (rs.getPublicityStartTime() != null && !rs.getPublicityStartTime().isAfter(ZonedDateTime.now())) {
            final String cameraId = CameraHelper.convertNaturalIdToCameraId(rs.getNaturalId());
            cameraPresetHistoryRepository.updatePresetHistoryPublicityForCameraId(
                cameraId, rs.internalIsPublic(), rs.getPublicityStartTime().toInstant());
        }
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

        if (!presetImageName.matches(s3WeathercamKeyRegexp)) {
            return HistoryStatus.ILLEGAL_KEY;
        }
        // C1234567.jpg -> C1234567
        final CameraPresetHistory history = findHistoryVersionInclSecret(getPresetIdFromImageName(presetImageName), versionId);
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

    public URI createS3UriForVersion(final String imageName, final String versionId) {
        return URI.create(String.format("%s/%s?versionId=%s", s3WeathercamBucketUrl,
            createImageVersionKey(getPresetIdFromImageName(imageName)), versionId));
    }

    private String createPublicUrlForVersion(final String presetId, final String versionId) {
        return String.format("%s%s.jpg?versionId=%s", weathercamBaseUrl, presetId, versionId);
    }

    private static boolean isCameraId(final String cameraId) {
        return cameraId.length() == 6;
    }

    private static boolean isPresetId(final String presetId) {
        return presetId.length() == 8;
    }

    private static String getPresetIdFromImageName(final String imageName) {
        return imageName.substring(0,8);
    }

    private static String createImageVersionKey(String presetId) {
        return presetId + CameraImageS3Writer.IMAGE_VERSION_KEY_SUFFIX;
    }
}