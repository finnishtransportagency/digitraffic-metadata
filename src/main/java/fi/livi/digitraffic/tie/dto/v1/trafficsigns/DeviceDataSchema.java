package fi.livi.digitraffic.tie.dto.v1.trafficsigns;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceDataSchema {
    @JsonProperty
    public String tunnus;
    @JsonProperty
    public String nayttama;
    @JsonProperty
    public String lisatieto;
    @JsonProperty
    public ZonedDateTime voimaan;
    @JsonProperty
    public String syy;
}
