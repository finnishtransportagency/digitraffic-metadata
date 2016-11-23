package fi.livi.digitraffic.tie.data.service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.dto.datex2.Datex2RootDataObjectDto;
import fi.livi.digitraffic.tie.data.model.Datex2;
import fi.livi.digitraffic.tie.data.model.Datex2Situation;
import fi.livi.digitraffic.tie.data.model.Datex2SituationRecord;
import fi.livi.digitraffic.tie.data.model.Datex2SituationRecordType;
import fi.livi.digitraffic.tie.data.model.Datex2SituationRecordValidyStatus;
import fi.livi.digitraffic.tie.data.model.SituationRecordCommentI18n;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Comment;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MultilingualString;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.MultilingualStringValue;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.OverallPeriod;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.PayloadPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Situation;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationRecord;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.Validity;

@Service
public class Datex2DataService {

    private static final Logger log = LoggerFactory.getLogger(Datex2DataService.class);
    private final Datex2Repository datex2Repository;

    @Autowired
    public Datex2DataService(Datex2Repository datex2Repository) {
        this.datex2Repository = datex2Repository;
    }

    @Transactional
    public void updateDatex2Data(List<Pair<D2LogicalModel, String>> data) {

        for (Pair<D2LogicalModel, String> pair : data) {

            Datex2 datex2 = new Datex2();
            datex2.setImportTime(ZonedDateTime.now());
            datex2.setMessage(pair.getRight());

            D2LogicalModel datex = pair.getLeft();

            parseAndAppendPayloadPublicationData(datex.getPayloadPublication(), datex2);

            datex2Repository.save(datex2);
        }
    }

    private void parseAndAppendPayloadPublicationData(final PayloadPublication payloadPublication, final Datex2 datex2) {
        datex2.setPublicationTime(DateHelper.toZonedDateTime(payloadPublication.getPublicationTime()));
        if (payloadPublication instanceof SituationPublication) {
            parseAndAppendSituationPublicationData((SituationPublication) payloadPublication, datex2);
        } else {
            log.error("Not implemented handling for Datex2 PayloadPublication type " + payloadPublication.getClass());
        }
    }

    private void parseAndAppendSituationPublicationData(final SituationPublication situationPublication, final Datex2 datex2) {
        List<Situation> situations = situationPublication.getSituation();
        for (Situation situation : situations) {
            Datex2Situation d2Situation = new Datex2Situation();
            datex2.addSituation(d2Situation);

            d2Situation.setSituationId(situation.getId());
            d2Situation.setVersionTime(DateHelper.toZonedDateTime(situation.getSituationVersionTime()));

            parseAndAppendSituationRecordData(situation.getSituationRecord(), d2Situation);
        }
    }

    private void parseAndAppendSituationRecordData(List<SituationRecord> situationRecords, Datex2Situation d2Situation) {
        for (SituationRecord record : situationRecords) {
            Datex2SituationRecord d2SituationRecord = new Datex2SituationRecord();
            d2Situation.addSituationRecord(d2SituationRecord);
            d2SituationRecord.setType(Datex2SituationRecordType.fromRecord(record.getClass()));

            // Only 1. comment seems to be valid
            List<Comment> pc = record.getGeneralPublicComment();
            if (pc != null && !pc.isEmpty()) {
                Comment comment = pc.get(0);
                MultilingualString.Values values = comment.getComment().getValues();
                List<SituationRecordCommentI18n> comments = joinComments(values.getValue());
                d2SituationRecord.setPublicComments(comments);
            }

            d2SituationRecord.setSituationRecordId(record.getId());
            d2SituationRecord.setCreationTime(DateHelper.toZonedDateTime(record.getSituationRecordCreationTime()));
            d2SituationRecord.setVersionTime(DateHelper.toZonedDateTime(record.getSituationRecordVersionTime()));
            d2SituationRecord.setObservationTime(DateHelper.toZonedDateTime(record.getSituationRecordObservationTime()));

            Validity validy = record.getValidity();
            d2SituationRecord.setValidyStatus(Datex2SituationRecordValidyStatus.fromValue(validy.getValidityStatus().name()));
            OverallPeriod period = validy.getValidityTimeSpecification();
            d2SituationRecord.setOverallStartTime(DateHelper.toZonedDateTime(period.getOverallStartTime()));
            d2SituationRecord.setOverallEndTime(DateHelper.toZonedDateTime(period.getOverallEndTime()));
        }
    }

    /**
     * Joins comments of same language as one comment
     * @param value
     * @return
     */
    private List<SituationRecordCommentI18n> joinComments(List<MultilingualStringValue> value) {
        if (value == null) {
            return Collections.emptyList();
        }
        Map<String, SituationRecordCommentI18n> langToCommentMap = new HashMap<>();
        for (MultilingualStringValue msv : value) {
            String lang = msv.getLang();
            SituationRecordCommentI18n i18n = langToCommentMap.get(lang);
            if (i18n == null) {
                i18n = new SituationRecordCommentI18n(lang);
                langToCommentMap.put(lang, i18n);
            }
            i18n.setValue(StringUtils.join(i18n.getValue(), msv.getValue()));
        }

        return langToCommentMap.entrySet().stream()
                .filter(a -> !StringUtils.isBlank(a.getValue().getValue()) )
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Datex2RootDataObjectDto findDatex2Data(final String situationId, final int year, final int month) {
        final LocalDateTime updated = datex2Repository.getLatestMeasurementTime();
        return new Datex2RootDataObjectDto(
                situationId != null ? datex2Repository.findHistory(situationId, year, month) : datex2Repository.findHistory(year, month),
                updated);
    }

    @Transactional(readOnly = true)
    public Datex2RootDataObjectDto findActiveDatex2Data(boolean onlyUpdateInfo) {
        final LocalDateTime updated = datex2Repository.getLatestMeasurementTime();
        if (onlyUpdateInfo) {
            return new Datex2RootDataObjectDto(updated);
        } else {
            return new Datex2RootDataObjectDto(
                    datex2Repository.findAllActive(),
                    updated);
        }
    }

    public Datex2RootDataObjectDto findAllDatex2DataBySituationId(String situationId) {
        final LocalDateTime updated = datex2Repository.getLatestMeasurementTime();
        List<Datex2> datex2s = datex2Repository.findBySituationId(situationId);
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return new Datex2RootDataObjectDto(
                datex2s,
                updated);

    }
}
