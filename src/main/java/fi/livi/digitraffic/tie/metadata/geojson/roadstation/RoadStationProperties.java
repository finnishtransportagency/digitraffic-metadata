package fi.livi.digitraffic.tie.metadata.geojson.roadstation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStationState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Properties", description = "Roadstation properties")
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class RoadStationProperties {

    @JsonIgnore
    @ApiModelProperty(value = "Road station's lotju id")
    private Long lotjuId;

    @ApiModelProperty(value = "Road station's natural id", required = true)
    @JsonProperty("roadStationId")
    private long naturalId;

    @ApiModelProperty(value = "Common name of road station")
    private String name;

    @ApiModelProperty(value = "Data collection interval [s]")
    private Integer collectionInterval;

    @ApiModelProperty(value = "Data collection status")
    private CollectionStatus collectionStatus;

    @ApiModelProperty(value = "Municipality")
    private String municipality;

    @ApiModelProperty(value = "Municipality code")
    private String municipalityCode;

    @ApiModelProperty(value = "Province")
    private String province;

    @ApiModelProperty(value = "Province code")
    private String provinceCode;

    @ApiModelProperty(value = "Map of namess [fi, sv, en]")
    private Map<String, String> names = new HashMap<>();

    private RoadAddress roadAddress = new RoadAddress();

    @ApiModelProperty(value = "Id in road registry")
    private String liviId;

    @ApiModelProperty(value = "Country where station is located")
    private String country;

    @JsonIgnore
    private LocalDateTime startDate;

    @JsonIgnore
    private LocalDateTime repairMaintenanceDate;
    @JsonIgnore
    private LocalDateTime annualMaintenanceDate;

    @ApiModelProperty(value = "Location of the station")
    private String location;

    @ApiModelProperty(value = "Road station state")
    private RoadStationState state;

    public long getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final long naturalId) {
        this.naturalId = naturalId;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(Long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getCollectionInterval() {
        return collectionInterval;
    }

    public void setCollectionInterval(final Integer collectionInterval) {
        this.collectionInterval = collectionInterval;
    }

    public CollectionStatus getCollectionStatus() {
        return collectionStatus;
    }

    public void setCollectionStatus(final CollectionStatus collectionStatus) {
        this.collectionStatus = collectionStatus;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(final String municipality) {
        this.municipality = municipality;
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(final String municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(final String province) {
        this.province = province;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(final String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public void setNames(final Map<String, String> names) {
        this.names = names;
    }

    public void addName(final String lang, final String name) {
        if (name != null) {
            this.names.put(lang, name);
        }
    }

    public RoadAddress getRoadAddress() {
        return roadAddress;
    }

    public void setRoadAddress(RoadAddress roadAddress) {
        this.roadAddress = roadAddress;
    }

    public void setLiviId(String liviId) {
        this.liviId = liviId;
    }

    public String getLiviId() {
        return liviId;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setState(RoadStationState state) {
        this.state = state;
    }

    public RoadStationState getState() {
        return state;
    }

    public void setRepairMaintenanceDate(LocalDateTime repairMaintenanceDate) {
        this.repairMaintenanceDate = repairMaintenanceDate;
    }

    public LocalDateTime getRepairMaintenanceDate() {
        return repairMaintenanceDate;
    }

    public void setAnnualMaintenanceDate(LocalDateTime annualMaintenanceDate) {
        this.annualMaintenanceDate = annualMaintenanceDate;
    }

    public LocalDateTime getAnnualMaintenanceDate() {
        return annualMaintenanceDate;
    }

    @ApiModelProperty(value = "Station established " + ToStringHelpper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE)
    public String getStartLocalTime() {
        return ToStringHelpper.toString(getStartDate(), ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Station established " + ToStringHelpper.ISO_8601_UTC_TIMESTAMP_EXAMPLE)
    public String getStartUtc() {
        return ToStringHelpper.toString(getStartDate(), ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }

    @ApiModelProperty(value = "Repair maintenance " + ToStringHelpper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE)
    public String getRepairMaintenanceLocalTime() {
        return ToStringHelpper.toString(getRepairMaintenanceDate(), ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Repair maintenance " + ToStringHelpper.ISO_8601_UTC_TIMESTAMP_EXAMPLE)
    public String getRepairMaintenanceUtc() {
        return ToStringHelpper.toString(getRepairMaintenanceDate(), ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }

    @ApiModelProperty(value = "Annual maintenance " + ToStringHelpper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE)
    public String getAnnualMaintenanceLocalTime() {
        return ToStringHelpper.toString(getAnnualMaintenanceDate(), ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Annual maintenance " + ToStringHelpper.ISO_8601_UTC_TIMESTAMP_EXAMPLE)
    public String getAnnualMaintenanceUtc() {
        return ToStringHelpper.toString(getAnnualMaintenanceDate(), ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        RoadStationProperties that = (RoadStationProperties) o;

        return new EqualsBuilder()
                .append(naturalId, that.naturalId)
                .append(lotjuId, that.lotjuId)
                .append(name, that.name)
                .append(collectionInterval, that.collectionInterval)
                .append(collectionStatus, that.collectionStatus)
                .append(municipality, that.municipality)
                .append(municipalityCode, that.municipalityCode)
                .append(province, that.province)
                .append(provinceCode, that.provinceCode)
                .append(names, that.names)
                .append(roadAddress, that.roadAddress)
                .append(liviId, that.liviId)
                .append(country, that.country)
                .append(startDate, that.startDate)
                .append(location, that.location)
                .append(state, that.state)
                .append(repairMaintenanceDate, that.repairMaintenanceDate)
                .append(annualMaintenanceDate, that.annualMaintenanceDate)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(lotjuId)
                .append(naturalId)
                .append(name)
                .append(collectionInterval)
                .append(collectionStatus)
                .append(municipality)
                .append(municipalityCode)
                .append(province)
                .append(provinceCode)
                .append(names)
                .append(roadAddress)
                .append(liviId)
                .append(country)
                .append(startDate)
                .append(location)
                .append(state)
                .append(repairMaintenanceDate)
                .append(annualMaintenanceDate)
                .toHashCode();
    }
}
