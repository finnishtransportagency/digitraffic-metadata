package fi.livi.digitraffic.tie.dto.v1.tms;

import java.time.ZonedDateTime;
import java.util.List;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Immutable
@ApiModel(value = "TmsData", description = "Latest measurement data from TMS Stations", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataUpdatedTime", "tmsStations"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsRootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "TMS Stations data")
    @JsonProperty(value = "tmsStations")
    private final List<TmsStationDto> tmsStations;

    public TmsRootDataObjectDto(final List<TmsStationDto> tmsStations, final ZonedDateTime updated) {
        super(updated);
        this.tmsStations = tmsStations;
    }

    public TmsRootDataObjectDto(final ZonedDateTime updated) {
        this(null, updated);
    }

    public List<TmsStationDto> getTmsStations() {
        return tmsStations;
    }

}

