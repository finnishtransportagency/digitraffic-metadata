package fi.livi.digitraffic.tie.model.v1.camera;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.model.v1.WeatherStation;

@Entity
@DynamicUpdate
public class CameraPreset {

    @Id
    @GenericGenerator(name = "SEQ_CAMERA_PRESET", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_CAMERA_PRESET"))
    @GeneratedValue(generator = "SEQ_CAMERA_PRESET")
    private Long id;

    private String presetId;

    @NotNull
    private Long lotjuId;

    /**
     * presetName1 == presentationName == nimiEsitys
     */
    @Column(name="PRESET_NAME_1")
    private String presetName1;

    /**
     * presetName2 == nameOnDevice == nimiLaitteella
     * Not for public use
     */
    @Column(name="PRESET_NAME_2")
    private String presetName2;

    /**
     * Not for public use
     */
    private Integer presetOrder;

    /**
     * Lotju's Esiasento#isJulkinen()
     */
    @Column(name="IS_PUBLIC")
    private boolean isPublic;

    private Boolean inCollection;
    private Integer compression;
    private Boolean defaultDirection;
    private String resolution;
    private String direction;

    @Column(name="PIC_LAST_MODIFIED")
    private ZonedDateTime pictureLastModified;

    private LocalDate obsoleteDate;

    // Camera properties
    private Long cameraLotjuId;

    private String cameraId;

    @Enumerated(EnumType.STRING)
    private CameraType cameraType;

    /**
     * RoadStation is same for one camera all presets
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="ROAD_STATION_ID")
    @Fetch(FetchMode.SELECT)
    private RoadStation roadStation;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="NEAREST_RD_WEATHER_STATION_ID")
    @Fetch(FetchMode.SELECT)
    private WeatherStation nearestWeatherStation;

    /**
     * This value is calculated by db so it's value is not
     * reliable if entity is modified after fetch from db.
     */
    @Column(updatable = false, insertable = false) // virtual column
    private boolean publishable;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(final String cameraId) {
        this.cameraId = cameraId;
    }

    public String getPresetId() {
        return presetId;
    }

    public void setPresetId(final String presetId) {
        this.presetId = presetId;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(final Long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public Long getCameraLotjuId() {
        return cameraLotjuId;
    }

    public void setCameraLotjuId(final Long cameraLotjuId) {
        this.cameraLotjuId = cameraLotjuId;
    }

    public CameraType getCameraType() {
        return cameraType;
    }

    public void setCameraType(final CameraType cameraType) {
        this.cameraType = cameraType;
    }

    public String getPresetName1() {
        return presetName1;
    }

    public void setPresetName1(final String presetName1) {
        this.presetName1 = presetName1;
    }

    public String getPresetName2() {
        return presetName2;
    }

    public void setPresetName2(final String presetName2) {
        this.presetName2 = presetName2;
    }

    public Integer getPresetOrder() {
        return presetOrder;
    }

    public void setPresetOrder(final Integer presetOrder) {
        this.presetOrder = presetOrder;
    }

    /**
     * Lotju's Esiasento#isJulkinen()
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Lotju's Esiasento#isJulkinen()
     */
    public void setPublic(final Boolean aPublic) {
        this.isPublic = aPublic;
    }

    public Boolean isInCollection() {
        return inCollection;
    }

    public void setInCollection(final Boolean inCollection) {
        this.inCollection = inCollection;
    }

    public Integer getCompression() {
        return compression;
    }

    public void setCompression(final Integer compression) {
        this.compression = compression;
    }

    public Boolean getDefaultDirection() {
        return defaultDirection;
    }

    public void setDefaultDirection(final Boolean defaultDirection) {
        this.defaultDirection = defaultDirection;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(final String resolution) {
        this.resolution = resolution;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(final String direction) {
        this.direction = direction;
    }

    public RoadStation getRoadStation() {
        return roadStation;
    }

    public void setRoadStation(final RoadStation roadStation) {
        this.roadStation = roadStation;
    }

    public WeatherStation getNearestWeatherStation() {
        return nearestWeatherStation;
    }

    public void setNearestWeatherStation(final WeatherStation nearestWeatherStation) {
        this.nearestWeatherStation = nearestWeatherStation;
    }

    /**
     * @return true if state changed
     */
    public boolean obsolete() {
        if (obsoleteDate == null) {
            setObsoleteDate(LocalDate.now());
            return true;
        }
        return false;
    }

    /**
     * @return true if state changed
     */
    public boolean unobsolete() {
        if (obsoleteDate != null) {
            setObsoleteDate(null);
            return true;
        }
        return false;
    }

    public Long getRoadStationNaturalId() {
        return roadStation != null ? roadStation.getNaturalId() : null;
    }

    public Long getRoadStationLotjuId() {
        return roadStation != null ? roadStation.getLotjuId() : null;
    }

    public Long getNearestWeatherStationNaturalId() {
        return nearestWeatherStation != null ? nearestWeatherStation.getRoadStationNaturalId() : null;
    }

    public ZonedDateTime getPictureLastModified() {
        return pictureLastModified;
    }

    public void setPictureLastModified(final ZonedDateTime pictureLastModified) {
        this.pictureLastModified = pictureLastModified;
    }

    public LocalDate getObsoleteDate() {
        return obsoleteDate;
    }

    private void setObsoleteDate(final LocalDate obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public boolean isObsolete() {
        return obsoleteDate != null;
    }

    public boolean isPublishable() {
        return publishable;
    }

    @Override
    public String toString() {
        return new ToStringHelper(this)
                .appendField("presetId", presetId)
                .appendField("id", id)
                .appendField("cameraId", cameraId)
                .appendField("lotjuId", lotjuId)
                .appendField("obsoleteDate", obsoleteDate)
                .appendField("roadStationLotjuId", getRoadStationLotjuId())
                .appendField("roadStationNaturalId", getRoadStationNaturalId())
                .toString();
    }
}
