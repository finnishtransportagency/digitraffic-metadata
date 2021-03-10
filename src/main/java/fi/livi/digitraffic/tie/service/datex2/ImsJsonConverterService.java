package fi.livi.digitraffic.tie.service.datex2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType;

@Service
public class ImsJsonConverterService {
    private static final Logger log = LoggerFactory.getLogger(ImsJsonConverterService.class);

    protected final ObjectReader genericJsonReader;

    @Autowired
    public ImsJsonConverterService(final ObjectMapper objectMapper) {
        this.genericJsonReader = objectMapper.reader();
    }

    /**
     * If given json is GeoJSON FeatureCollection returns it's features otherwise returns the single feature json.
     * @param imsJson GeoJSON string
     * @return Map of situationId to GeoJSON feature JSON-string, SituationType and TrafficAnnouncementType. Empty if no features is found.
     */
    @NotTransactionalServiceMethod
    public Map<String, Triple<String, SituationType, TrafficAnnouncementType>> parseFeatureJsonsFromImsJson(final String imsJson) {

        if (StringUtils.isBlank(imsJson)) {
            return Collections.emptyMap();
        }

        final JsonNode root;
        try {
            root = genericJsonReader.readTree(imsJson);
        } catch (final JsonProcessingException e) {
            log.error(String.format("method=parseFeatureJsonsFromImsJson Failed to read Json tree of imsJson: %s", imsJson), e);
            return Collections.emptyMap();
        }

        if ( isFeatureCollection(root) ) {
            return parseFeatureCollection(root);
        } else if ( isFeature(root) ){
            return parseFeature(root);
        } else {
            log.error("method=parseFeatureJsonsFromImsJson IMS Json doesn't contain valid GeoJson object type [Feature|FeatureCollection]. Json: {}", imsJson);
            return Collections.emptyMap();
        }
    }

    private Map<String, Triple<String, SituationType, TrafficAnnouncementType>> parseFeature(final JsonNode root) {
        final String situationId = getSituationId(root);
        final SituationType situationType = getSituationType(root);
        final TrafficAnnouncementType trafficAnnouncementType = getTrafficAnnouncementType(root, situationType);

        if (StringUtils.isNotBlank(situationId)) {
            return Collections.singletonMap(situationId, Triple.of(root.toPrettyString(), situationType, trafficAnnouncementType));
        }
        return Collections.emptyMap();
    }

    private Map<String, Triple<String, SituationType, TrafficAnnouncementType>> parseFeatureCollection(final JsonNode root) {
        final JsonNode features = root.get("features");
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> featureJsons = new HashMap<>();
        for (int i = 0; i < features.size(); i++) {
            final String json = features.get(i).toPrettyString();
            final String situationId = getSituationId(features.get(i));
            final SituationType situationType = getSituationType(features.get(i));
            final TrafficAnnouncementType trafficAnnouncementType = getTrafficAnnouncementType(features.get(i), situationType);
            if (StringUtils.isNotBlank(situationId)) {
                featureJsons.put(situationId, Triple.of(json, situationType, trafficAnnouncementType));
            }
        }
        return featureJsons;
    }

    private String getSituationId(final JsonNode feature) {
        final JsonNode properties = feature.get("properties");
        if (properties == null) {
            log.error("method=getSituationId No properties property for feature json: {}", feature.toPrettyString());
            return null;
        }
        final JsonNode situationId = properties.get("situationId");
        if (situationId == null) {
            log.error("method=getSituationId No situationId property for feature json: {}", feature.toPrettyString());
            return null;
        }
        return situationId.asText();
    }

    private SituationType getSituationType(final JsonNode feature) {
        final JsonNode properties = feature.get("properties");
        if (properties == null) {
            return resolveSituationTypeFromTextWithError(feature);
        }
        final JsonNode situationType = properties.get("situationType");
        if (situationType == null) {
            return resolveSituationTypeFromTextWithError(feature);
        }
        try {
            return SituationType.fromValue(situationType.asText());
        } catch (final Exception e) {
            log.error("method=getSituationType Error while trying to resolve json SituationType", e);
            return resolveSituationTypeFromTextWithError(feature);
        }
    }

    private static SituationType resolveSituationTypeFromTextWithError(final JsonNode featureNode) {
        final SituationType resolvedType = Datex2Helper.resolveSituationTypeFromText(featureNode.toString());
        log.error("method=getSituationType No situationType property for feature json. Resolved type from text {}. Json: {}", resolvedType, featureNode.toPrettyString());
        return resolvedType;
    }

    private TrafficAnnouncementType getTrafficAnnouncementType(final JsonNode feature,
                                                               final SituationType situationType) {
        if (situationType != SituationType.TRAFFIC_ANNOUNCEMENT) {
            return null;
        }
        final JsonNode properties = feature.get("properties");
        if (properties == null) {
            return resolveTrafficAnnouncementTypeTypeFromTextWithError(feature);
        }
        final JsonNode trafficAnnouncementType = properties.get("trafficAnnouncementType");
        if (trafficAnnouncementType == null) {
            return resolveTrafficAnnouncementTypeTypeFromTextWithError(feature);
        }
        try {
            return TrafficAnnouncementType.fromValue(trafficAnnouncementType.asText());
        } catch (Exception e) {
            log.error("method=getTrafficAnnouncementType Error while trying to resolve json TrafficAnnouncementType", e);
            return resolveTrafficAnnouncementTypeTypeFromTextWithError(feature);
        }
    }

    private static TrafficAnnouncementType resolveTrafficAnnouncementTypeTypeFromTextWithError(final JsonNode featureNode) {
        final TrafficAnnouncementType resolvedType = Datex2Helper.resolveTrafficAnnouncementTypeFromText(featureNode.toString());
        log.error("method=getTrafficAnnouncementType No trafficAnnouncementType property for feature json. Resolved type from text {}. Json: {}", resolvedType, featureNode.toPrettyString());
        return resolvedType;
    }

    private boolean isFeatureCollection(final JsonNode root) {
        final JsonNode type = root.get("type");
        return type != null && StringUtils.equals(type.asText(), "FeatureCollection");
    }
    private boolean isFeature(final JsonNode root) {
        final JsonNode type = root.get("type");
        return type != null && StringUtils.equals(type.asText(), "Feature");
    }
}
