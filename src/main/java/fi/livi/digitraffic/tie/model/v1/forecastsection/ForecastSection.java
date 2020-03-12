package fi.livi.digitraffic.tie.model.v1.forecastsection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.model.v1.Road;
import fi.livi.digitraffic.tie.model.v1.RoadSection;
import fi.livi.digitraffic.tie.service.v1.forecastsection.dto.Coordinate;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionNaturalIdHelper;
import io.swagger.annotations.ApiModelProperty;

@Entity
@DynamicUpdate
public class ForecastSection {

    private static final Logger log = LoggerFactory.getLogger(ForecastSection.class);

    @Id
    @GenericGenerator(name = "SEQ_FORECAST_SECTION", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_FORECAST_SECTION"))
    @GeneratedValue(generator = "SEQ_FORECAST_SECTION")
    @JsonIgnore
    private Long id;

    /**
     * Road section identifier 15 characters ie. 00004_112_000_0
     * 1. Road number 5 characters ie. 00004
     * 2. Road section 3 characters ie. 112
     * 3. Road section version 3 characters ie. 000
     * 4. Reserver for future needs 1 characters default 0
     * Delimiter is underscore "_"
     */

    @ApiModelProperty(value =
            "Forecast section identifier 15 characters ie. 00004_112_000_0: \n" +
            "1. Road number 5 characters ie. 00004, \n" +
            "2. Road section 3 characters ie. 112, \n" +
            "3. Road section version 3 characters ie. 000, \n" +
            "4. Reserved for future needs 1 characters default 0")
    @JsonProperty("roadId")
    private String naturalId;

    @JsonIgnore
    private Integer version;

    @ApiModelProperty(value = "Forecast section description")
    private String description;

    @ApiModelProperty(value = "Road section number")
    @Column(insertable = false, updatable = false)
    private int roadSectionNumber;

    @ApiModelProperty(value = "Forecast section road number")
    @Column(insertable = false, updatable = false)
    private int roadNumber;

    @ApiModelProperty(value = "Road section version number")
    @Column(insertable = false, updatable = false)
    private int roadSectionVersionNumber;

    @ApiModelProperty(value = "Forecast section start distance")
    private Integer startDistance;

    @ApiModelProperty(value = "Forecast section end distance")
    private Integer endDistance;

    @ApiModelProperty(value = "Forecast section length")
    private Integer length;

    @ApiModelProperty(value = "Forecast section obsolete date")
    private Date obsoleteDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="road_id")
    @Fetch(FetchMode.JOIN)
    private Road road;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="start_road_section_id")
    @Fetch(FetchMode.JOIN)
    private RoadSection startRoadSection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="end_road_section_id")
    @Fetch(FetchMode.JOIN)
    private RoadSection endRoadSection;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "forecastSectionCoordinateListPK.forecastSectionId", cascade = CascadeType.ALL)
    @OrderBy("forecastSectionCoordinateListPK.orderNumber")
    private List<ForecastSectionCoordinateList> forecastSectionCoordinateLists = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "forecastSectionWeatherPK.forecastSectionId", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("time")
    private List<ForecastSectionWeather> forecastSectionWeatherList = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "roadSegmentPK.forecastSectionId", cascade = CascadeType.ALL)
    @OrderBy("roadSegmentPK.orderNumber")
    private List<RoadSegment> roadSegments = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "linkIdPK.forecastSectionId", cascade = CascadeType.ALL)
    @OrderBy("linkIdPK.orderNumber")
    private List<LinkId> linkIds = new ArrayList<>();

    public ForecastSection() {
    }

    public ForecastSection(final String naturalId, final int version, final String description) {
        this.naturalId = naturalId;
        this.roadNumber = ForecastSectionNaturalIdHelper.getRoadNumber(naturalId);
        this.roadSectionNumber = ForecastSectionNaturalIdHelper.getRoadSectionNumber(naturalId);
        this.version = version;
        if (version == 1) {
            this.roadSectionVersionNumber = ForecastSectionNaturalIdHelper.getRoadSectionVersionNumber(naturalId);
        } else {
            this.roadSectionVersionNumber = 0;
        }
        this.description = description;
        this.forecastSectionCoordinateLists = new ArrayList<>();
        this.forecastSectionWeatherList = new ArrayList<>();
        this.obsoleteDate = null;
    }

    public Long getId() {
        return id;
    }

    public String getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(String naturalId) {
        this.naturalId = naturalId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRoadSectionNumber() {
        return roadSectionNumber;
    }

    public void setRoadSectionNumber(int roadSectionNumber) {
        this.roadSectionNumber = roadSectionNumber;
    }

    public int getRoadNumber() {
        return roadNumber;
    }

    public void setRoadNumber(int roadNumber) {
        this.roadNumber = roadNumber;
    }

    public int getRoadSectionVersionNumber() {
        return roadSectionVersionNumber;
    }

    public void setRoadSectionVersionNumber(int roadSectionVersionNumber) {
        this.roadSectionVersionNumber = roadSectionVersionNumber;
    }

    public Integer getStartDistance() {
        return startDistance;
    }

    public void setStartDistance(Integer startDistance) {
        this.startDistance = startDistance;
    }

    public Integer getEndDistance() {
        return endDistance;
    }

    public void setEndDistance(Integer endDistance) {
        this.endDistance = endDistance;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Date getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(Date obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public Road getRoad() {
        return road;
    }

    public void setRoad(Road road) {
        this.road = road;
    }

    public RoadSection getStartRoadSection() {
        return startRoadSection;
    }

    public void setStartRoadSection(RoadSection startRoadSection) {
        this.startRoadSection = startRoadSection;
    }

    public RoadSection getEndRoadSection() {
        return endRoadSection;
    }

    public void setEndRoadSection(RoadSection endRoadSection) {
        this.endRoadSection = endRoadSection;
    }

    public List<ForecastSectionWeather> getForecastSectionWeatherList() {
        return forecastSectionWeatherList;
    }

    public List<ForecastSectionCoordinateList> getForecastSectionCoordinateLists() {
        return forecastSectionCoordinateLists;
    }

    public List<RoadSegment> getRoadSegments() {
        return roadSegments;
    }

    public List<LinkId> getLinkIds() {
        return linkIds;
    }

    public void removeCoordinateLists() {
        forecastSectionCoordinateLists.forEach(l -> l.removeCoordinates());
        forecastSectionCoordinateLists.clear();
    }

    public void addCoordinates(final List<Coordinate> coordinates) {
        forecastSectionCoordinateLists = new ArrayList<>();

        final List<ForecastSectionCoordinate> coordinateList = new ArrayList<>();

        // FIXME: Move to V1MetadataUpdater
        long orderNumber = 1;
        for (final Coordinate coordinate : coordinates) {
            if (!coordinate.isValid()) {
                log.info("Invalid coordinates for forecast section " + getNaturalId() + ". Coordinates were: " + coordinate.toString());
            } else {
                coordinateList.add(new ForecastSectionCoordinate(
                    new ForecastSectionCoordinatePK(id, 1L, orderNumber), coordinate.longitude, coordinate.latitude));
                orderNumber++;
            }
        }
        forecastSectionCoordinateLists.add(new ForecastSectionCoordinateList(new ForecastSectionCoordinateListPK(id, 1L), coordinateList));
    }

    @Override
    public String toString() {
        return "ForecastSection{" +
               "id=" + id +
               ", naturalId='" + naturalId + '\'' +
               ", description='" + description + '\'' +
               ", roadSectionNumber=" + roadSectionNumber +
               ", roadNumber=" + roadNumber +
               ", roadSectionVersionNumber=" + roadSectionVersionNumber +
               ", startDistance=" + startDistance +
               ", endDistance=" + endDistance +
               ", length=" + length +
               ", obsoleteDate=" + obsoleteDate +
               ", road=" + road +
               ", startRoadSection=" + startRoadSection +
               ", endRoadSection=" + endRoadSection +
               ", forecastSectionCoordinateLists=" + forecastSectionCoordinateLists +
               '}';
    }
}
