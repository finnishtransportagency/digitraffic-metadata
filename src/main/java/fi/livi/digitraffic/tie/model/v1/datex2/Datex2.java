package fi.livi.digitraffic.tie.model.v1.datex2;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@DynamicUpdate
@Table(name = "DATEX2")
public class Datex2 {

    @Id
    @GenericGenerator(name = "SEQ_DATEX2", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_DATEX2"))
    @GeneratedValue(generator = "SEQ_DATEX2")
    private Long id;

    @Column(name = "IMPORT_DATE")
    @NotNull
    private ZonedDateTime importTime;

    @NotNull
    private String message;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Datex2MessageType messageType;

    private ZonedDateTime publicationTime;

    @OneToMany(mappedBy = "datex2", cascade = CascadeType.ALL)
    private List<Datex2Situation> situations;

    private String jsonMessage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public ZonedDateTime getImportTime() {
        return importTime;
    }

    public void setImportTime(final ZonedDateTime importTime) {
        this.importTime = importTime;
    }

    public ZonedDateTime getPublicationTime() {
        return publicationTime;
    }


    public void setPublicationTime(final ZonedDateTime publicationTime) {
        this.publicationTime = publicationTime;
    }

    public List<Datex2Situation> getSituations() {
        return situations;
    }

    public void setSituations(final List<Datex2Situation> situations) {
        this.situations = situations;
    }

    public void addSituation(final Datex2Situation situation) {
        if (this.situations == null) {
            this.situations = new ArrayList<>();
        }
        situations.add(situation);
        situation.setDatex2(this);
    }

    public Datex2MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(final Datex2MessageType messageType) {
        this.messageType = messageType;
    }

    public void setJsonMessage(final String jsonMessage) {
        this.jsonMessage = jsonMessage;
    }

    public String getJsonMessage() {
        return jsonMessage;
    }
}
