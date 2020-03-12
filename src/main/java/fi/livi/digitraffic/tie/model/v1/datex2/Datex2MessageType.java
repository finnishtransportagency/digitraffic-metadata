package fi.livi.digitraffic.tie.model.v1.datex2;

public enum Datex2MessageType {
    TRAFFIC_INCIDENT,
    ROADWORK,
    WEIGHT_RESTRICTION;

    public String toParameter() {
        return name().toLowerCase().replace("_", "-");
    }
}
