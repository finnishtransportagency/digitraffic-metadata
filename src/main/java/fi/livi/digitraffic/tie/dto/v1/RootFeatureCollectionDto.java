package fi.livi.digitraffic.tie.dto.v1;

import java.time.ZonedDateTime;
import java.util.List;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.FeatureCollection;
import io.swagger.annotations.ApiModelProperty;

@Immutable
@JsonPropertyOrder({ "dataUpdatedTime", "dataLastCheckedTime", "type", "features" })
public class RootFeatureCollectionDto<FeatureType> extends FeatureCollection<FeatureType> {

    @ApiModelProperty(value = "Data last updated date time", required = true)
    private final ZonedDateTime dataUpdatedTime;

    @ApiModelProperty(value = "Data last checked date time", required = true)
    private final ZonedDateTime dataLastCheckedTime;

    public RootFeatureCollectionDto(final ZonedDateTime dataUpdatedTime,
                                    final ZonedDateTime dataLastCheckedTime,
                                    final List<FeatureType> features) {
        super(features);
        this.dataUpdatedTime = dataUpdatedTime;
        this.dataLastCheckedTime = dataLastCheckedTime;
    }

    public ZonedDateTime getDataLastCheckedTime() {
        return dataLastCheckedTime;
    }

    public ZonedDateTime getDataUpdatedTime() {
        return dataUpdatedTime;
    }
}
