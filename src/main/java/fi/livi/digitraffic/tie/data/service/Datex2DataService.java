package fi.livi.digitraffic.tie.data.service;

import static fi.livi.digitraffic.tie.data.model.Datex2MessageType.ROADWORK;
import static fi.livi.digitraffic.tie.data.model.Datex2MessageType.TRAFFIC_DISORDER;
import static fi.livi.digitraffic.tie.data.model.Datex2MessageType.WEIGHT_RESTRICTION;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
import fi.livi.digitraffic.tie.data.model.Datex2;
import fi.livi.digitraffic.tie.data.model.Datex2MessageType;
import fi.livi.digitraffic.tie.data.service.datex2.StringToObjectMarshaller;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.response.ObservationTimeType;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.response.RoadworksDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.response.TimestampedRoadworkDatex2;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.response.TimestampedTrafficDisorderDatex2;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.response.TimestampedWeightRestrictionDatex2;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.response.TrafficDisordersDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.response.WeightRestrictionsDatex2Response;

@Service
public class Datex2DataService {
    private static final Logger log = LoggerFactory.getLogger(Datex2DataService.class);

    private final Datex2Repository datex2Repository;
    private final StringToObjectMarshaller stringToObjectMarshaller;

    @Autowired
    public Datex2DataService(final Datex2Repository datex2Repository, final StringToObjectMarshaller stringToObjectMarshaller) {
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
        final List<Datex2> datex2s = findDatex2Messages(TRAFFIC_DISORDER, situationId, year, month);

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

    private ZonedDateTime findLatestImportTime(final Datex2MessageType messageType) {
        return DateHelper.toZonedDateTimeAtUtc(datex2Repository.findLatestImportTime(messageType.name()));
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
    public TrafficDisordersDatex2Response getAllTrafficDisordersBySituationId(final
    String situationId) {
        final List<Datex2> datex2s = datex2Repository.findBySituationIdAndMessageType(situationId, TRAFFIC_DISORDER.name());
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return convertToTrafficDisordersDatex2Response(datex2s);
    }


    @Transactional(readOnly = true)
    public TrafficDisordersDatex2Response findActiveTrafficDisorders(final int inactiveHours) {
        final List<Datex2> allActive = datex2Repository.findAllActive(TRAFFIC_DISORDER.name(), inactiveHours);
        return convertToTrafficDisordersDatex2Response(allActive);
    }

    @Transactional(readOnly = true)
    public D2LogicalModel findActiveTrafficDisordersAsD2LogicalModel(final int inactiveHours) {
        final List<Datex2> allActive = datex2Repository.findAllActive(TRAFFIC_DISORDER.name(), inactiveHours);
        return convertToD2LogicalModel(allActive);
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

    private D2LogicalModel convertToD2LogicalModel(final List<Datex2> datex2s) {

        // conver Datex2s to D2LogicalModels
        final List<D2LogicalModel> modelsNewestFirst = datex2s.stream()
            .map(datex2 -> (D2LogicalModel) stringToObjectMarshaller.convertToObject(datex2.getMessage()))
            .filter(d2 -> d2.getPayloadPublication() != null)
            .sorted(Comparator.comparing((D2LogicalModel d2) -> d2.getPayloadPublication().getPublicationTime()).reversed())
            .collect(Collectors.toList());

        if (modelsNewestFirst.isEmpty()) {
            return new D2LogicalModel();
        }

        // Append all older situations to newest and return newest that combines all situations
        final D2LogicalModel newesModel = modelsNewestFirst.remove(0);
        SituationPublication situationPublication = getSituationPublication(newesModel);
        modelsNewestFirst.forEach(d2 -> {
            final SituationPublication toAdd = getSituationPublication(d2);
            situationPublication.getSituations().addAll(toAdd.getSituations());
        });
        return newesModel;
    }

    static SituationPublication getSituationPublication(final D2LogicalModel model) {
        if (model.getPayloadPublication() instanceof SituationPublication) {
            return (SituationPublication) model.getPayloadPublication();
        } else {
            final String err = "Not SituationPublication available for " + model.getPayloadPublication().getClass();
            log.error(err);
            throw new RuntimeException(err);
        }
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
