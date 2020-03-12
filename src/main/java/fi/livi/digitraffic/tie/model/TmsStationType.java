package fi.livi.digitraffic.tie.model;

import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaTyyppi;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "TMS station type", value = "TmsStationType")
public enum TmsStationType {

    DSL_4("DSL_4"),
    DSL_6("DSL_6"),
    E_18("E18"),
    OLD("OLD"),

    // legacy values
    DSL("DSL"),
    FINAVIA("FINAVIA");

    private final String value;

    TmsStationType(final String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TmsStationType convertFromLamasemaTyyppi(final LamAsemaTyyppi lamAsemaTyyppi) {
        if (lamAsemaTyyppi != null) {
            return valueOf(lamAsemaTyyppi.name());
        }
        return null;
    }
}
