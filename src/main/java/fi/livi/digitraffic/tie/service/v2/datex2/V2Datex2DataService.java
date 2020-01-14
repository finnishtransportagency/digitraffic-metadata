package fi.livi.digitraffic.tie.service.v2.datex2;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v1.Datex2Repository;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.ImsGeoJsonFeature;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeatureCollection;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.v1.datex2.StringToObjectMarshaller;

@Service
public class V2Datex2DataService {
    private static final Logger log = LoggerFactory.getLogger(V2Datex2DataService.class);

    private final Datex2Repository datex2Repository;
    private final StringToObjectMarshaller<D2LogicalModel> stringToObjectMarshaller;
    private final V2Datex2HelperService v2Datex2HelperService;

    @Autowired
    public V2Datex2DataService(final Datex2Repository datex2Repository,
                               final StringToObjectMarshaller stringToObjectMarshaller,
                               final V2Datex2HelperService v2Datex2HelperService) {
        this.datex2Repository = datex2Repository;
        this.stringToObjectMarshaller = stringToObjectMarshaller;
        this.v2Datex2HelperService = v2Datex2HelperService;
    }

    @Transactional(readOnly = true)
    public D2LogicalModel findAllBySituationId(final String situationId, final Datex2MessageType datex2MessageType) {
        final List<Datex2> datex2s = findBySituationIdAndMessageType(situationId, datex2MessageType.name());
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return convertToD2LogicalModel(datex2s);
    }

    @Transactional(readOnly = true)
    public TrafficAnnouncementFeatureCollection findAllBySituationIdJson(final String situationId, final Datex2MessageType datex2MessageType) {
        final List<Datex2> datex2s = findBySituationIdAndMessageType(situationId, datex2MessageType.name());
        if (datex2s.isEmpty()) {
            throw new ObjectNotFoundException("Datex2", situationId);
        }
        return convertToJson(datex2s);
    }

    @Transactional(readOnly = true)
    public D2LogicalModel findActive(final int inactiveHours,
                                     final Datex2MessageType datex2MessageType) {
        final List<Datex2> allActive = findAllActive(datex2MessageType.name(), inactiveHours);
        return convertToD2LogicalModel(allActive);
    }

    @Transactional(readOnly = true)
    public TrafficAnnouncementFeatureCollection findActiveJson(final int inactiveHours,
                                                               final Datex2MessageType datex2MessageType) {
        final List<Datex2> allActive = findAllActive(datex2MessageType.name(), inactiveHours);
        return convertToJson(allActive);
    }

    private List<Datex2> findAllActive(final String messageType, final int activeInPastHours) {
        return datex2Repository.findAllActive(messageType, activeInPastHours);
    }

    private List<Datex2> findBySituationIdAndMessageType(final String situationId, final String messageType) {
        return datex2Repository.findBySituationIdAndMessageType(situationId, messageType);
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

    private TrafficAnnouncementFeatureCollection convertToJson(final List<Datex2> datex2s) {

        // conver Datex2s to Json objects, newest first
        final List<TrafficAnnouncementFeature> features = datex2s.stream()
            .filter(d2 -> d2.getJsonMessage() != null)
            .map(d2 -> v2Datex2HelperService.convertToFeatureJsonObject(d2.getJsonMessage()))
            .sorted(Comparator.comparing((TrafficAnnouncementFeature json) -> json.getProperties().releaseTime).reversed())
            .collect(Collectors.toList());
        // TODO Times
        return new TrafficAnnouncementFeatureCollection(ZonedDateTime.now(), ZonedDateTime.now(), features);
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
}
