package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

public class Directiontext {

    @JacksonXmlElementWrapper(useWrapping = false)
    public final List<Text> text;

    public final String dirindex;

    public final String RDI;

    public Directiontext(@JsonProperty("text") final List<Text> text,
                         @JsonProperty("dirindex") final String dirindex,
                         @JsonProperty("RDI") final String RDI) {
        this.text = text;
        this.dirindex = dirindex;
        this.RDI = RDI;
    }
}
