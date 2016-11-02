package fi.livi.digitraffic.tie.metadata.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.metadata.converter.RoadStationTypeEnumConverter;

@Entity
@Immutable
public class AllowedRoadStationSensor implements Serializable {

    @Id
    @GenericGenerator(name = "SEQ_ALLOWED_SENSOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_ALLOWED_SENSOR"))
    @GeneratedValue(generator = "SEQ_ALLOWED_SENSOR")
    private Long id;

    @Column(name="NATURAL_ID")
    private long naturalId;

    @Convert(converter = RoadStationTypeEnumConverter.class)
    @Column(name="ROAD_STATION_TYPE")
    private RoadStationType roadStationType;

}
