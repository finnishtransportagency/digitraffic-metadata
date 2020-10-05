package fi.livi.digitraffic.tie.service.v2.datex2;

import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.TRAFFIC_INCIDENT;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.conf.jms.ExternalIMSMessage;
import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.Comment;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.MultilingualString;
import fi.livi.digitraffic.tie.datex2.MultilingualStringValue;
import fi.livi.digitraffic.tie.datex2.OverallPeriod;
import fi.livi.digitraffic.tie.datex2.PayloadPublication;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.datex2.Validity;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2Situation;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2SituationRecord;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2SituationRecordType;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2SituationRecordValidyStatus;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationRecordCommentI18n;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.datex2.Datex2Helper;
import fi.livi.digitraffic.tie.service.v1.datex2.Datex2MessageDto;
import fi.livi.digitraffic.tie.service.v1.datex2.StringToObjectMarshaller;

@Service
public class V2Datex2UpdateService {
    private static final Logger log = LoggerFactory.getLogger(V2Datex2UpdateService.class);

    private final Datex2Repository datex2Repository;
    private final StringToObjectMarshaller<D2LogicalModel> stringToObjectMarshaller;
    private final DataStatusService dataStatusService;

    @Autowired
    public V2Datex2UpdateService(final Datex2Repository datex2Repository,
                                 final StringToObjectMarshaller<D2LogicalModel> stringToObjectMarshaller,
                                 final DataStatusService dataStatusService) {
        this.datex2Repository = datex2Repository;
        this.stringToObjectMarshaller = stringToObjectMarshaller;
        this.dataStatusService = dataStatusService;
    }

    @Transactional
    public int updateTrafficIncidentImsMessages(final List<ExternalIMSMessage> imsMessages) {
        return updateTrafficImsMessages(imsMessages, TRAFFIC_INCIDENT);
    }

    @Transactional
    public int updateTrafficImsMessages(final List<ExternalIMSMessage> imsMessages, final Datex2MessageType messageType) {
        return (int)imsMessages.stream()
            .filter(imsMessage -> isNewOrUpdatedSituation(stringToObjectMarshaller.convertToObject(imsMessage.getMessageContent().getD2Message()), messageType))
            .map(imsMessage -> convertToDatex2MessageDto(imsMessage, messageType))
            .filter(this::updateDatex2Data)
            .count();
    }

    private Datex2MessageDto convertToDatex2MessageDto(final ExternalIMSMessage imsMessage, final Datex2MessageType messageType) {
        final String jsonValue = StringUtils.trimToNull(imsMessage.getMessageContent().getJMessage());
        if (log.isDebugEnabled()) {
            log.debug("IMS JSON: \n{}", jsonValue);
        }
        final D2LogicalModel d2 = stringToObjectMarshaller.convertToObject(imsMessage.getMessageContent().getD2Message());
        return createModelWithJson(d2, jsonValue, messageType, ZonedDateTime.now());
    }

    private boolean isNewOrUpdatedSituation(final D2LogicalModel d2, final Datex2MessageType messageType) {
        final SituationPublication sp = Datex2Helper.getSituationPublication(d2);
        final Situation situation = sp.getSituations().get(0);
        final Instant versionTime = findSituationLatestVersionTime(situation.getId(), messageType);
        return Datex2Helper.isNewOrUpdatedSituation(versionTime, situation);
    }

    private Instant findSituationLatestVersionTime(final String situationId, final Datex2MessageType messageType) {
        return datex2Repository.findDatex2SituationLatestVersionTime(situationId, messageType.name());
    }

    /**
     * Expects
     * @param d2 with only one situation inside
     * @param jsonValue simple json value
     * @param messageType type of message
     * @param importTime when was the import done
     * @return dto containing JSON
     */
    private Datex2MessageDto createModelWithJson(final D2LogicalModel d2, final String jsonValue,
                                                 final Datex2MessageType messageType, final ZonedDateTime importTime) {
        Datex2Helper.checkD2HasOnlyOneSituation(d2);
        final SituationPublication sp = Datex2Helper.getSituationPublication(d2);
        final Situation situation = sp.getSituations().get(0);
        final Instant versionTime = findSituationLatestVersionTime(situation.getId(), messageType);
        final boolean update = versionTime != null && Datex2Helper.isNewOrUpdatedSituation(versionTime, situation);
        final boolean isNew = versionTime == null;

        log.info("method=createModelWithJson situationUpdated={} situationNew={}", update, isNew);

        return convert(d2, sp, situation, importTime, jsonValue, messageType);
    }

    public Datex2MessageDto convert(final D2LogicalModel main, final SituationPublication sp,
                                    final Situation situation, final ZonedDateTime importTime,
                                    final String jsonValue, final Datex2MessageType messageType) {
        final D2LogicalModel d2 = new D2LogicalModel();
        final SituationPublication newSp = new SituationPublication();

        newSp.setPublicationTime(sp.getPublicationTime());
        newSp.setPublicationCreator(sp.getPublicationCreator());
        newSp.setLang(sp.getLang());
        newSp.withSituations(situation);

        d2.setModelBaseVersion(main.getModelBaseVersion());
        d2.setExchange(main.getExchange());
        d2.setPayloadPublication(newSp);

        final String messageValue = stringToObjectMarshaller.convertToString(d2);
        return new Datex2MessageDto(d2, messageType, messageValue, jsonValue, importTime);
    }

    /* COPIED */

    /**
     *
     * @param message datex2 message
     * @return true if message was new or updated otherwise false
     */
    @Transactional
    public boolean updateDatex2Data(final Datex2MessageDto message) {

        Datex2Helper.checkD2HasOnlyOneSituation(message.model);

        if (isNewOrUpdatedSituation(message.model, message.messageType)) {
            final Datex2 datex2 = new Datex2();
            final D2LogicalModel d2 = message.model;

            final ZonedDateTime latestVersionTime = getLatestSituationRecordVersionTime(d2);

            datex2.setImportTime(
                Objects.requireNonNullElseGet(message.importTime, () -> latestVersionTime != null ? latestVersionTime : ZonedDateTime.now()));
            datex2.setMessage(message.message);
            datex2.setJsonMessage(message.jsonMessage);
            datex2.setMessageType(message.messageType);
            parseAndAppendPayloadPublicationData(d2.getPayloadPublication(), datex2);
            datex2Repository.save(datex2);
            if (message.jsonMessage != null) {
                dataStatusService.updateDataUpdated(DataType.typeFor(message.messageType));
            }
            final String situationId = Datex2Helper.getSituationPublication(d2).getSituations().get(0).getId();
            log.info("Update Datex2 messageType={} for situationId={} with importTime={}", message.messageType, situationId, datex2.getImportTime());
            return true;
        }
        return false;
    }

    private ZonedDateTime getLatestSituationRecordVersionTime(final D2LogicalModel d2) {
        final Instant latest = Datex2Helper.getSituationPublication(d2).getSituations().stream()
            .map(s -> s.getSituationRecords().stream()
                .map(SituationRecord::getSituationRecordVersionTime).max(Comparator.naturalOrder()).orElseThrow())
            .max(Comparator.naturalOrder()).orElseThrow();
        return DateHelper.toZonedDateTimeAtUtc(latest);
    }

    private static void parseAndAppendPayloadPublicationData(final PayloadPublication payloadPublication, final Datex2 datex2) {
        datex2.setPublicationTime(DateHelper.toZonedDateTimeAtUtc(payloadPublication.getPublicationTime()));
        if (payloadPublication instanceof SituationPublication) {
            parseAndAppendSituationPublicationData((SituationPublication) payloadPublication, datex2);
        } else {
            log.error("Not implemented handling for Datex2 PayloadPublication type " + payloadPublication.getClass());
        }
    }

    private static void parseAndAppendSituationPublicationData(final SituationPublication situationPublication, final Datex2 datex2) {
        final List<Situation> situations = situationPublication.getSituations();
        for (final Situation situation : situations) {
            final Datex2Situation d2Situation = new Datex2Situation();

            datex2.addSituation(d2Situation);

            d2Situation.setSituationId(situation.getId());
            d2Situation.setVersionTime(DateHelper.toZonedDateTimeWithoutMillisAtUtc(situation.getSituationVersionTime()));

            parseAndAppendSituationRecordData(situation.getSituationRecords(), d2Situation);
        }
    }

    private static void parseAndAppendSituationRecordData(final List<SituationRecord> situationRecords, final Datex2Situation d2Situation) {
        for (final SituationRecord record : situationRecords) {
            final Datex2SituationRecord d2SituationRecord = new Datex2SituationRecord();

            d2Situation.addSituationRecord(d2SituationRecord);
            d2SituationRecord.setType(Datex2SituationRecordType.fromRecord(record.getClass()));

            // Only first comment seems to be valid
            final List<Comment> pc = record.getGeneralPublicComments();
            if (pc != null && !pc.isEmpty()) {
                final Comment comment = pc.get(0);
                final MultilingualString.Values values = comment.getComment().getValues();
                final List<SituationRecordCommentI18n> comments = joinComments(values.getValues());
                d2SituationRecord.setPublicComments(comments);
            }

            d2SituationRecord.setSituationRecordId(record.getId());
            d2SituationRecord.setCreationTime(DateHelper.toZonedDateTimeWithoutMillisAtUtc(record.getSituationRecordCreationTime()));
            d2SituationRecord.setVersionTime(DateHelper.toZonedDateTimeWithoutMillisAtUtc(record.getSituationRecordVersionTime()));
            d2SituationRecord.setObservationTime(DateHelper.toZonedDateTimeWithoutMillisAtUtc(record.getSituationRecordObservationTime()));

            final Validity validy = record.getValidity();
            d2SituationRecord.setValidyStatus(Datex2SituationRecordValidyStatus.fromValue(validy.getValidityStatus().name()));
            final OverallPeriod period = validy.getValidityTimeSpecification();
            d2SituationRecord.setOverallStartTime(DateHelper.toZonedDateTimeWithoutMillisAtUtc(period.getOverallStartTime()));
            d2SituationRecord.setOverallEndTime(DateHelper.toZonedDateTimeWithoutMillisAtUtc(period.getOverallEndTime()));
        }
    }

    /**
     * Joins comments of same language as one comment
     * @param value comments to join
     * @return joined comments
     */
    private static List<SituationRecordCommentI18n> joinComments(final List<MultilingualStringValue> value) {
        if (value == null) {
            return Collections.emptyList();
        }
        final Map<String, SituationRecordCommentI18n> langToCommentMap = new HashMap<>();
        for (final MultilingualStringValue msv : value) {
            final String lang = msv.getLang();
            SituationRecordCommentI18n i18n = langToCommentMap.get(lang);
            if (i18n == null) {
                i18n = new SituationRecordCommentI18n(lang);
                langToCommentMap.put(lang, i18n);
            }
            i18n.setValue(StringUtils.join(i18n.getValue(), msv.getValue()));
        }

        return langToCommentMap.values().stream()
            .filter(situationRecordCommentI18n -> !StringUtils.isBlank(situationRecordCommentI18n.getValue()))
            .collect(Collectors.toList());
    }
}
