package fi.livi.digitraffic.tie.dto.v1.trafficsigns;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataSchema {
    @JsonProperty
    public List<DeviceDataSchema> liikennemerkit;
}
