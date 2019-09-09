package fi.livi.digitraffic.tie.metadata.geojson.trafficsigns;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "Properties", description = "Traffic Sign properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrafficSignProperties {
    // device properties
    public final String id;
    public final String type;
    public final String roadAddress;
    public final String direction;
    public final Integer lane;

    // data properties
    public final String displayInformation;
    public final String additionalInformation;
    public final ZonedDateTime effectDate;
    public final String cause;

    public TrafficSignProperties(final String id, final String type, final String roadAddress, final String direction, final Integer lane,
        final String displayInformation, final String additionalInformation, final ZonedDateTime effectDate, final String cause) {
        this.id = id;
        this.type = type;
        this.roadAddress = roadAddress;
        this.direction = direction;
        this.lane = lane;
        this.displayInformation = displayInformation;
        this.additionalInformation = additionalInformation;
        this.effectDate = effectDate;
        this.cause = cause;
    }
}
