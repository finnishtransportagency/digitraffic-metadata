package fi.livi.digitraffic.tie.model.v1;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Road station road address", value = "RoadAddress")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "roadNumber", "roadSection", "distanceFromRoadSectionStart", "carriagewayCode", "sideCode" })
@Entity
@DynamicUpdate
public class RoadAddress implements Cloneable {

    public enum Side {

        UNKNOWN(0),
        RIGHT(1), // oikea ( mittaussuunnan oikealla puolella)
        LEFT(2), // vasen ( mittaussuunnan vasemmalla puolella)
        BETWEEN(3), // välissä (ajoratojen välissä)
        END(7), // päässä ( tien päässä)
        MIDDLE(8), // keskellä
        CROSS (9); // poikki

        private final int code;

        private static final Logger log = LoggerFactory.getLogger(Side.class);

        Side(final int code) {
            this.code = code;
        }
        public static Side getByCode(final Integer code) {
            if (code != null) {
                for (final RoadAddress.Side side : values()) {
                    if (side.code == code) {
                        return side;
                    }
                }
                log.error("No Side found with code " + code);
            }
            return null;
        }
    }

    public enum Carriageway {

        ONE_CARRIAGEWAY(0), // yksiajoratainen osuus
        DUAL_CARRIAGEWAY_FIRST_MEASURING_DIRECTION(1), // kaksiajorataisen osuuden ykkösajorata (mittaussuunta)
        DUAL_CARRIAGEWAY_SECOND_UPSTREAM(2); // kaksiajorataisen osuuden kakkosajorata (vastasuunta)

        private final int code;
        private static final Logger log = LoggerFactory.getLogger(Side.class);

        Carriageway(final int code) {
            this.code = code;
        }
        public static Carriageway getByCode(final Integer code) {
            if (code != null) {
                for (final RoadAddress.Carriageway carriageway : values()) {
                    if (carriageway.code == code) {
                        return carriageway;
                    }
                }
                log.error("No Carriageway found with code " + code);
            }
            return null;
        }
    }

    @JsonIgnore
    @Id
    @GenericGenerator(name = "SEQ_ROAD_ADDRESS", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD_ADDRESS"))
    @GeneratedValue(generator = "SEQ_ROAD_ADDRESS")
    private Long id;
    @ApiModelProperty(value = "Road number (values 1–99999)")
    private Integer roadNumber;
    @ApiModelProperty(value = "Road section (values 1–999)")
    private Integer roadSection;

    @ApiModelProperty(value = "Distance from start of the road portion [m]")
    @Column(name="DISTANCE_FROM_ROAD_SECTION_ST")
    private Integer distanceFromRoadSectionStart;

    @ApiModelProperty(value = "Carriageway (" +
                              "0 = One carriageway portion, " +
                              "1 = First carriageway of dual carriageway portion (measuring direction) " +
                              "2 = Second carriageway of dual carriageway portion (upstream))")
    @Column(name = "CARRIAGEWAY")
    private Integer carriagewayCode;

    @ApiModelProperty(value = "Side of the road (" +
                              "0 = Unknown, " +
                              "1 = Right (on the right side of the measuring direction), " +
                              "2 = Right (on the right side of the measuring direction), " +
                              "3 = Between (between the tracks), " +
                              "7 = End (end of the road), " +
                              "8 = Middle, " +
                              "9 = Cross)")
    @Column(name = "SIDE")
    @JsonIgnore
    private Integer sideCode;

    @ApiModelProperty(value = "Road maintenance class")
    private String roadMaintenanceClass;

    @ApiModelProperty(value = "Road contract area")
    private String contractArea;

    @ApiModelProperty(value = "Road contract area code")
    private Integer contractAreaCode;

    public RoadAddress() {
        // Possibility to initialize empty road address
    }

    public Long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Carriageway getCarriageway() {
        return Carriageway.getByCode(getCarriagewayCode());
    }

    public Integer getCarriagewayCode() {
        return carriagewayCode;
    }

    public void setCarriagewayCode(final Integer carriagewayCode) {
        this.carriagewayCode = carriagewayCode;
    }

    public Integer getDistanceFromRoadSectionStart() {
        return distanceFromRoadSectionStart;
    }

    public void setDistanceFromRoadSectionStart(final Integer distanceFromRoadSectionStart) {
        this.distanceFromRoadSectionStart = distanceFromRoadSectionStart;
    }

    @ApiModelProperty(value = "Side of the as enum value")
    @JsonIgnore
    public Side getSide() {
        return Side.getByCode(getSideCode());
    }

    public Integer getSideCode() {
        return sideCode;
    }

    public void setSideCode(final Integer sideCode) {
        this.sideCode = sideCode;
    }

    public String getRoadMaintenanceClass() {
        return roadMaintenanceClass;
    }

    public void setRoadMaintenanceClass(final String roadMaintenanceClass) {
        this.roadMaintenanceClass = roadMaintenanceClass;
    }

    public void setContractArea(final String contractArea) {
        this.contractArea = contractArea;
    }

    public String getContractArea() {
        return contractArea;
    }

    public void setContractAreaCode(final Integer contractAreaCode) {
        this.contractAreaCode = contractAreaCode;
    }

    public Integer getContractAreaCode() {
        return contractAreaCode;
    }

    public Integer getRoadNumber() {
        return roadNumber;
    }

    public void setRoadNumber(final Integer roadNumber) {
        this.roadNumber = roadNumber;
    }

    public Integer getRoadSection() {
        return roadSection;
    }

    public void setRoadSection(final Integer roadSection) {
        this.roadSection = roadSection;
    }

    @Override
    public String toString() {
        return new ToStringHelper(this)
                .appendField("id", getId())
                .appendField("roadNumber", getRoadNumber())
                .appendField("roadSection", getRoadSection())
                .toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final RoadAddress that = (RoadAddress) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(roadNumber, that.roadNumber)
                .append(roadSection, that.roadSection)
                .append(distanceFromRoadSectionStart, that.distanceFromRoadSectionStart)
                .append(carriagewayCode, that.carriagewayCode)
                .append(sideCode, that.sideCode)
                .append(roadMaintenanceClass, that.roadMaintenanceClass)
                .append(contractArea, that.contractArea)
                .append(contractAreaCode, that.contractAreaCode)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(roadNumber)
                .append(roadSection)
                .append(distanceFromRoadSectionStart)
                .append(carriagewayCode)
                .append(sideCode)
                .append(roadMaintenanceClass)
                .append(contractArea)
                .append(contractAreaCode)
                .toHashCode();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
