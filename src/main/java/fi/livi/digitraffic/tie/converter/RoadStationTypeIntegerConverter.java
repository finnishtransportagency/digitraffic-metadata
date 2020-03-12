package fi.livi.digitraffic.tie.metadata.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import fi.livi.digitraffic.tie.model.RoadStationType;

@Converter
public class RoadStationTypeIntegerConverter implements AttributeConverter<RoadStationType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(final RoadStationType attribute) {
        return attribute != null ? attribute.getTypeNumber() : null;
    }

    @Override
    public RoadStationType convertToEntityAttribute(final Integer typeNumber) {
        return typeNumber != null ? RoadStationType.fromTypeNumber(typeNumber) : null;
    }
}
