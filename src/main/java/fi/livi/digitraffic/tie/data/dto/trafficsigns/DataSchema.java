package fi.livi.digitraffic.tie.data.dto.trafficsigns;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataSchema {
    @JsonProperty
    List<DeviceDataSchema> liikennemerkit;
}
