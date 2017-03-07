package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CO1 {

    public final Long y;

    public final Long x;

    public CO1(@JsonProperty("Y") final String y,
               @JsonProperty("X") final String x) {

        this.y = NumberUtils.isParsable(y) ? Long.valueOf(y) : null;
        this.x = NumberUtils.isParsable(x) ? Long.valueOf(x) : null;
    }
}
