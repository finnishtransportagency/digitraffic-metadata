package fi.livi.digitraffic.tie.service.v1.datex2;

import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.ROADWORK;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.TRAFFIC_INCIDENT;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.WEIGHT_RESTRICTION;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.response.ObservationTimeType;
import fi.livi.digitraffic.tie.datex2.response.RoadworksDatex2Response;
import fi.livi.digitraffic.tie.datex2.response.TimestampedRoadworkDatex2;
import fi.livi.digitraffic.tie.datex2.response.TimestampedTrafficDisorderDatex2;
import fi.livi.digitraffic.tie.datex2.response.TimestampedWeightRestrictionDatex2;
import fi.livi.digitraffic.tie.datex2.response.TrafficDisordersDatex2Response;
import fi.livi.digitraffic.tie.datex2.response.WeightRestrictionsDatex2Response;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

@Service
public class Datex2DataService {
    private static final Logger log = LoggerFactory.getLogger(Datex2DataService.class);

    private final Datex2Repository datex2Repository;
    private final StringToObjectMarshaller<D2LogicalModel> stringToObjectMarshaller;

    @Autowired
    public Datex2DataService(final Datex2Repository datex2Repository,
                             final StringToObjectMarshaller stringToObjectMarshaller) {
        this.datex2Repository = datex2Repository;
        this.stringToObjectMarshaller = stringToObjectMarshaller;
    }

    @Transactional(readOnly = true)
    public RoadworksDatex2Response findRoadworks(final String situationId, final int year, final int month) {
        final List<Datex2> datex2s = findDatex2Messages(ROADWORK, situationId, year, month);

        return convertToRoadworksDatex2Response(datex2s);
    }

    @Transactional(readOnly = true)
    public WeightRestrictionsDatex2Response findWeightRestrictions(final String situationId, final int year, final int month) {
        final List<Datex2> datex2s = findDatex2Messages(WEIGHT_RESTRICTION, situationId, year, month);

        return convertToWeightRestrictionDatex2Response(datex2s);
    }

    @Transactional(readOnly = true)
    public TrafficDisordersDatex2Response findTrafficDisorders(final String situationId, final int year, final int month) {
        final List<Datex2> datex2s = findDatex2Messages(TRAFFIC_INCIDENT, situationId, year, month);

        return convertToTrafficDisordersDatex2Response(datex2s);
    }

    private List<Datex2> findDatex2Messages(final Datex2MessageType messageType, final String situationId,
                                           final int year, final int month) {
        if (situationId != null && !datex2Repository.existsWithSituationId(situationId)) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }

        return situationId != null
            ? datex2Repository.findHistoryBySituationId(messageType.name(), situationId, year, month)
            : datex2Repository.findHistory(messageType.name(), year, month);
    }

    @Transactional(readOnly = true)
    public RoadworksDatex2Response getAllRoadworksBySituationId(final String situationId) {
        final List<Datex2> datex2s = datex2Repository.findBySituationIdAndMessageType(situationId, ROADWORK.name());
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return convertToRoadworksDatex2Response(datex2s);
    }

    @Transactional(readOnly = true)
    public WeightRestrictionsDatex2Response getAllWeightRestrictionsBySituationId(final String situationId) {
        final List<Datex2> datex2s = datex2Repository.findBySituationIdAndMessageType(situationId, WEIGHT_RESTRICTION.name());
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return convertToWeightRestrictionDatex2Response(datex2s);
    }

    @Transactional(readOnly = true)
    public TrafficDisordersDatex2Response getAllTrafficDisordersBySituationId(final String situationId) {
        final List<Datex2> datex2s = datex2Repository.findBySituationIdAndMessageType(situationId, TRAFFIC_INCIDENT.name());
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return convertToTrafficDisordersDatex2Response(datex2s);
    }

    @Transactional(readOnly = true)
    public TrafficDisordersDatex2Response findActiveTrafficDisorders(final int inactiveHours) {
        final List<Datex2> allActive = datex2Repository.findAllActive(TRAFFIC_INCIDENT.name(), inactiveHours);
        return convertToTrafficDisordersDatex2Response(allActive);
    }

    @Transactional(readOnly = true)
    public RoadworksDatex2Response findActiveRoadworks(final int inactiveHours) {
        final List<Datex2> allActive = datex2Repository.findAllActive(ROADWORK.name(), inactiveHours);
        return convertToRoadworksDatex2Response(allActive);
    }

    @Transactional(readOnly = true)
    public WeightRestrictionsDatex2Response findActiveWeightRestrictions(final int inactiveHours) {
        final List<Datex2> allActive = datex2Repository.findAllActive(WEIGHT_RESTRICTION.name(), inactiveHours);
        return convertToWeightRestrictionDatex2Response(allActive);
    }

    private WeightRestrictionsDatex2Response convertToWeightRestrictionDatex2Response(final List<Datex2> list) {
        final List<TimestampedWeightRestrictionDatex2> roadworks = list.stream()
            .map(d2 -> unmarshallWeightRestriction(d2.getMessage(), d2.getImportTime()))
            .collect(Collectors.toList());

        return new WeightRestrictionsDatex2Response().withRestrictions(roadworks);
    }

    private RoadworksDatex2Response convertToRoadworksDatex2Response(final List<Datex2> list) {
        final List<TimestampedRoadworkDatex2> roadworks = list.stream()
            .map(d2 -> unmarshallRoadwork(d2.getMessage(), d2.getImportTime()))
            .collect(Collectors.toList());

        return new RoadworksDatex2Response().withRoadworks(roadworks);
    }

    private TrafficDisordersDatex2Response convertToTrafficDisordersDatex2Response(final List<Datex2> datex2s) {
        final List<TimestampedTrafficDisorderDatex2> timestampedTrafficDisorderDatex2s = new ArrayList<>();
        for (final Datex2 datex2 : datex2s) {
            final String datex2Xml = datex2.getMessage();
            if (!StringUtils.isBlank(datex2Xml)) {
                final TimestampedTrafficDisorderDatex2 tsDatex2 = unmarshallTrafficDisorder(datex2Xml, datex2.getImportTime());

                if (tsDatex2 != null) {
                    timestampedTrafficDisorderDatex2s.add(tsDatex2);
                }
            }
        }
        return new TrafficDisordersDatex2Response().withDisorders(timestampedTrafficDisorderDatex2s);
    }

    private TimestampedTrafficDisorderDatex2 unmarshallTrafficDisorder(final String datex2Xml, final ZonedDateTime importTime) {
        try {
            final D2LogicalModel d2LogicalModel = stringToObjectMarshaller.convertToObject(datex2Xml);
            final ObservationTimeType published =
                    new ObservationTimeType()
                            .withLocaltime(DateHelper.toInstant(importTime))
                            .withUtc(DateHelper.toInstant(importTime));
            return new TimestampedTrafficDisorderDatex2()
                    .withD2LogicalModel(d2LogicalModel)
                    .withPublished(published);
        } catch (final XmlMappingException e) {
            log.error("Failed to unmarshal datex2 message: " + datex2Xml, e);
        }

        return null;
    }

    private TimestampedRoadworkDatex2 unmarshallRoadwork(final String datex2Xml, final ZonedDateTime importTime) {
        try {
            final D2LogicalModel d2LogicalModel = stringToObjectMarshaller.convertToObject(datex2Xml);
            final ObservationTimeType published =
                new ObservationTimeType()
                    .withLocaltime(DateHelper.toInstant(importTime))
                    .withUtc(DateHelper.toInstant(importTime));
            return new TimestampedRoadworkDatex2()
                .withD2LogicalModel(d2LogicalModel)
                .withPublished(published);
        } catch (final XmlMappingException e) {
            log.error("Failed to unmarshal datex2 message: " + datex2Xml, e);
        }

        return null;
    }

    private TimestampedWeightRestrictionDatex2 unmarshallWeightRestriction(final String datex2Xml, final ZonedDateTime importTime) {
        try {
            final D2LogicalModel d2LogicalModel = stringToObjectMarshaller.convertToObject(datex2Xml);
            final ObservationTimeType published =
                new ObservationTimeType()
                    .withLocaltime(DateHelper.toInstant(importTime))
                    .withUtc(DateHelper.toInstant(importTime));
            return new TimestampedWeightRestrictionDatex2()
                .withD2LogicalModel(d2LogicalModel)
                .withPublished(published);
        } catch (final XmlMappingException e) {
            log.error("Failed to unmarshal datex2 message: " + datex2Xml, e);
        }

        return null;
    }
}
