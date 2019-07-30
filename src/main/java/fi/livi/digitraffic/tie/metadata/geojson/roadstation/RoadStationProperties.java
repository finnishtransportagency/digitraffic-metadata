package fi.livi.digitraffic.tie.metadata.geojson.roadstation;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStationState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Properties", description = "Roadstation properties")
public abstract class RoadStationProperties {

    private static final int LONGITUDE_IDX = 0;
    private static final int LATITUDE_IDX = 1;
    private static final int ALTITUDE_IDX = 2;

    @JsonIgnore
    @ApiModelProperty(value = "Road station's lotju id")
    private Long lotjuId;

    @ApiModelProperty(value = "Road station's id (naturalId)", required = true)
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

    @ApiModelProperty(value = "Map of names [fi, sv, en]",
                      example = "\"names\": {\n" +
                                "          \"fi\": \"Tie 7 Porvoo\",\n" +
                                "          \"sv\": \"Väg 7 Borgå\",\n" +
                                "          \"en\": \"Road 7 Porvoo\"\n" +
                                "        },")
    private Map<String, String> names = new HashMap<>();

    private RoadAddress roadAddress = new RoadAddress();

    @ApiModelProperty(value = "Id in road registry")
    private String liviId;

    @ApiModelProperty(value = "Country where station is located")
    private String country;

    @ApiModelProperty(value = "Station established " + ToStringHelper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE)
    private ZonedDateTime startTime;

    @ApiModelProperty(value = "Repair maintenance " + ToStringHelper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE)
    private ZonedDateTime repairMaintenanceTime;

    @ApiModelProperty(value = "Annual maintenance " + ToStringHelper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE)
    private ZonedDateTime annualMaintenanceTime;

    // Removed temporary until LOTJU data is fixed
    @JsonIgnore
    @ApiModelProperty(value = "Location of the station")
    private String location;

    @ApiModelProperty(value = "Road station state")
    private RoadStationState state;

    @ApiModelProperty(value = "Purpose of the road station")
    private String purpose;

    @ApiModelProperty(value = "Road station coordinates (LONGITUDE, LATITUDE, ALTITUDE. Coordinates are in ETRS89 / ETRS-TM35FIN format. Altitude is optional and measured in metres.)" +
                              "Point's coordinates  (Coordinates in WGS84. Altitude is optional [m])", required = true)
    private List<Double> coordinatesETRS89 = new ArrayList<>(3);

    public long getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final long naturalId) {
        this.naturalId = naturalId;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(final Long lotjuId) {
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

    public void setRoadAddress(final RoadAddress roadAddress) {
        this.roadAddress = roadAddress;
    }

    public void setLiviId(final String liviId) {
        this.liviId = liviId;
    }

    public String getLiviId() {
        return liviId;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setStartTime(final ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setState(final RoadStationState state) {
        this.state = state;
    }

    public RoadStationState getState() {
        return state;
    }

    public void setRepairMaintenanceTime(final ZonedDateTime repairMaintenanceTime) {
        this.repairMaintenanceTime = repairMaintenanceTime;
    }

    public ZonedDateTime getRepairMaintenanceTime() {
        return repairMaintenanceTime;
    }

    public void setAnnualMaintenanceTime(final ZonedDateTime annualMaintenanceTime) {
        this.annualMaintenanceTime = annualMaintenanceTime;
    }

    public ZonedDateTime getAnnualMaintenanceTime() {
        return annualMaintenanceTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final RoadStationProperties that = (RoadStationProperties) o;

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
                .append(startTime, that.startTime)
                .append(location, that.location)
                .append(state, that.state)
                .append(repairMaintenanceTime, that.repairMaintenanceTime)
                .append(annualMaintenanceTime, that.annualMaintenanceTime)
                .append(coordinatesETRS89, that.coordinatesETRS89)
                .append(purpose, that.purpose)
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
                .append(startTime)
                .append(location)
                .append(state)
                .append(repairMaintenanceTime)
                .append(annualMaintenanceTime)
                .append(coordinatesETRS89)
                .append(purpose)
                .toHashCode();
    }

    public void setCoordinatesETRS89(Point coordinatesETRS89Point) {
        this.coordinatesETRS89.clear();
        if (coordinatesETRS89Point != null) {
            setCoordinateETRS89(LONGITUDE_IDX, coordinatesETRS89Point.getLongitude());
            setCoordinateETRS89(LATITUDE_IDX, coordinatesETRS89Point.getLatitude());
            setCoordinateETRS89(ALTITUDE_IDX, coordinatesETRS89Point.getAltitude());
        }
    }

    private void setCoordinateETRS89(int index, Double coordinate) {
        if (coordinate != null) {
            while (coordinatesETRS89.size() <= index) {
                coordinatesETRS89.add(null);
            }
            coordinatesETRS89.set(index, coordinate);
        }
    }

    private Double getCoordinateETRS89(int index) {
        if ( index < coordinatesETRS89.size() ) {
            return coordinatesETRS89.get(index);
        }
        return null;
    }

    public List<Double> getCoordinatesETRS89() {
        return coordinatesETRS89;
    }

    @JsonIgnore
    public Double getAltitudeETRS89() {
        return getCoordinateETRS89(ALTITUDE_IDX);
    }

    @JsonIgnore
    public Double getLongitudeETRS89() {
        return getCoordinateETRS89(LONGITUDE_IDX);
    }

    @JsonIgnore
    public Double getLatitudeETRS89() {
        return getCoordinateETRS89(LATITUDE_IDX);
    }
}
