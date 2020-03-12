package fi.livi.digitraffic.tie.model.v1;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.model.DataType;

@Entity
@DynamicUpdate
public class DataUpdated {
    @Id
    @GenericGenerator(name = "SEQ_DATA_UPDATED", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_DATA_UPDATED"))
    @GeneratedValue(generator = "SEQ_DATA_UPDATED")
    private Long id;

    @Enumerated(EnumType.STRING)
    private DataType dataType;

    @Column(name = "UPDATED")
    private ZonedDateTime updatedTime;

    private String version;

    private DataUpdated() {
        // Empty for repository
    }

    public DataUpdated(final DataType type, final ZonedDateTime updatedTime, final String version) {
        setDataType(type);
        setUpdatedTime(updatedTime);
        setVersion(version);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public ZonedDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(ZonedDateTime updated) {
        this.updatedTime = updated;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
