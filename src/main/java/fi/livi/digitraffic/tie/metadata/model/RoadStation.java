package fi.livi.digitraffic.tie.metadata.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.converter.RoadStationTypeIntegerConverter;

@Entity
@DynamicUpdate
public class RoadStation {

    @Id
    @GenericGenerator(name = "SEQ_ROAD_STATION", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD_STATION"))
    @GeneratedValue(generator = "SEQ_ROAD_STATION")
    private Long id;

    @NotNull
    private Long naturalId;

    @NotNull
    private Long lotjuId;

    private String name;

    @Convert(converter = RoadStationTypeIntegerConverter.class)
    private RoadStationType type;

    /**
     * This is used only in db queries
     */
    @Enumerated(EnumType.STRING)
    private RoadStationType roadStationType;

    @Enumerated(EnumType.STRING)
    private RoadStationState state;

    private LocalDate obsoleteDate;

    @Column(name="IS_PUBLIC")
    private boolean isPublic;

    /**
     * Previous value for publicity. Used in case when new value is in the future.
     */
    @Column(name="IS_PUBLIC_PREVIOUS")
    private boolean isPublicPrevious;

    private ZonedDateTime publicityStartTime;

    private String nameFi, nameSv, nameEn;

    /** ETRS89 coordinates */
    private BigDecimal latitude, longitude, altitude;

    private Integer collectionInterval;

    @Enumerated(EnumType.STRING)
    private CollectionStatus collectionStatus;

    private String municipality;

    private String municipalityCode;

    private String province;

    private String provinceCode;

    private String location;
    private ZonedDateTime startDate;
    private String country;
    private String liviId;
    private ZonedDateTime repairMaintenanceDate;
    private ZonedDateTime annualMaintenanceDate;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name="ROAD_ADDRESS_ID", unique=true)
    @Fetch(FetchMode.SELECT)
    private RoadAddress roadAddress;

    private String purpose;

    @ManyToMany
    @JoinTable(name = "ROAD_STATION_SENSORS",
               joinColumns = @JoinColumn(name = "ROAD_STATION_ID", referencedColumnName = "ID"),
               inverseJoinColumns = @JoinColumn(name = "ROAD_STATION_SENSOR_ID", referencedColumnName = "ID"))
    private List<RoadStationSensor> roadStationSensors = new ArrayList<>();

    /**
     * This value is calculated by db so it's value is not
     * reliable if entity is modified after fetch from db.
     */
    @Column(updatable = false, insertable = false) // virtual column
    private boolean publishable;

    protected RoadStation() {
        setPublic(true);
        setPublicPrevious(true);
    }

    public RoadStation(final RoadStationType type) {
        this();
        setType(type);
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final Long naturalId) {
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

    public RoadStationType getType() {
        return type;
    }

    public void setType(final RoadStationType type) {
        if (this.type != null && !this.type.equals(type)) {
            throw new IllegalArgumentException("RoadStationType can not be changed once set. (" + this.type + " -> " + type + " )");
        }
        this.type = type;
        this.roadStationType = type;
    }

    private void setPublic(final boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isPublicPrevious() {
        return isPublicPrevious;
    }

    private void setPublicPrevious(boolean publicPrevious) {
        isPublicPrevious = publicPrevious;
    }

    private void setPublicityStartTime(final ZonedDateTime publicityStartTime) {
        this.publicityStartTime = publicityStartTime;
    }

    public ZonedDateTime getPublicityStartTime() {
        return publicityStartTime;
    }

    public boolean isObsolete() {
        return obsoleteDate != null;
    }
    public void obsolete() {
        if (obsoleteDate == null) {
            setObsoleteDate(LocalDate.now());
        }
    }

    public void unobsolete() {
        setObsoleteDate(null);
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    private void setObsoleteDate(final LocalDate obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public String getNameFi() {
        return nameFi;
    }

    public void setNameFi(final String nameFi) {
        this.nameFi = nameFi;
    }

    public String getNameSv() {
        return nameSv;
    }

    public void setNameSv(final String nameSv) {
        this.nameSv = nameSv;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(final String nameEn) {
        this.nameEn = nameEn;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(final BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(final BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getAltitude() {
        return altitude;
    }

    public void setAltitude(final BigDecimal altitude) {
        this.altitude = altitude;
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

    public List<RoadStationSensor> getRoadStationSensors() {
        return roadStationSensors;
    }

    public void setRoadStationSensors(final List<RoadStationSensor> roadStationSensors) {
        this.roadStationSensors = roadStationSensors;
    }

    public void setRepairMaintenanceDate(final ZonedDateTime repairMaintenanceDate) {
        this.repairMaintenanceDate = repairMaintenanceDate;
    }

    public ZonedDateTime getRepairMaintenanceDate() {
        return repairMaintenanceDate;
    }

    public void setAnnualMaintenanceDate(final ZonedDateTime annualMaintenanceDate) {
        this.annualMaintenanceDate = annualMaintenanceDate;
    }

    public ZonedDateTime getAnnualMaintenanceDate() {
        return annualMaintenanceDate;
    }

    public RoadAddress getRoadAddress() {
        return roadAddress;
    }

    public void setRoadAddress(final RoadAddress roadAddress) {
        this.roadAddress = roadAddress;
    }

    public RoadStationState getState() {
        return state;
    }

    public void setState(final RoadStationState state) {
        this.state = state;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setStartDate(final ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setLiviId(final String liviId) {
        this.liviId = liviId;
    }

    public String getLiviId() {
        return liviId;
    }

    public boolean isPublishable() {
        return publishable;
    }

    public void setPurpose(final String purpose) {
        this.purpose = purpose;
    }

    public String getPurpose() {
        return purpose;
    }

    @Override
    public String toString() {
        return new ToStringHelper(this)
                .appendField("id", id)
                .appendField("naturalId", naturalId)
                .appendField("lotjuId", this.getLotjuId())
                .appendField("name", name)
                .appendField("type", type)
                .appendField("collectionStatus", collectionStatus)
                .toString();
    }

    /**
     * Gets current publicity status
     * @return Is station public at the moment
     */
    public boolean isPublicNow() {
        // If current value is valid now, let's use it
        if (publicityStartTime == null || !publicityStartTime.isAfter(ZonedDateTime.now())) {
            return isPublic;
        }
        return isPublicPrevious;
    }

    /**
     * Updates fields: isPublic, publicityStartTime and isPublicPrevious
     *
     * @param isPublicNew new publicity value
     * @param publicityStartTimeNew time when new publicity value is valid from
     *
     * @return was there status change
     */
    public boolean updatePublicity(final boolean isPublicNew, final ZonedDateTime publicityStartTimeNew) {
        final boolean changed = isPublic != isPublicNew || !Objects.equals(publicityStartTime, publicityStartTimeNew);
        // If publicity status changes and current value hasn't become valid, then previous publicity status will remain unchanged
        // currentPublicityStartTime == null -> Valid all the time OR !inFuture -> Valid already
        if ( isPublic != isPublicNew &&
            (publicityStartTime == null || !publicityStartTime.isAfter(ZonedDateTime.now())) ) {
            setPublicPrevious(isPublic);
        }
        setPublic(isPublicNew);
        setPublicityStartTime(publicityStartTimeNew);
        return changed;
    }

    public void updatePublicity(final boolean isPublicNew) {
        updatePublicity(isPublicNew, null);
    }
}
