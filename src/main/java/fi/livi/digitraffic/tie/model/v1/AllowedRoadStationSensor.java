package fi.livi.digitraffic.tie.model.v1;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.model.RoadStationType;

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

    @Enumerated(EnumType.STRING)
    @Column(name="ROAD_STATION_TYPE")
    private RoadStationType roadStationType;

}
