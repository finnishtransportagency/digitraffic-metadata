package fi.livi.digitraffic.tie.dto.v1;

import java.util.List;

public class VariableSignDescriptions {
    public final List<CodeDescription> signTypes;

    public VariableSignDescriptions(final List<CodeDescription> signTypes) {
        this.signTypes = signTypes;
    }
}
