package fi.livi.digitraffic.tie.data.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "SituationRecordValidyStatus", description = "Datex2 situation record validy status")
@JsonInclude(JsonInclude.Include.NON_NULL)
public enum Datex2SituationRecordValidyStatus {

    /**
     * The described event, action or item is currently active regardless of the definition of the validity time specification.
     */
    ACTIVE("active"),

    /**
     * The described event, action or item is currently suspended, that is inactive, regardless of the definition of the validity time specification.
     */
    SUSPENDED("suspended"),

    /**
     * The validity status of the described event, action or item is in accordance with the definition of the validity time specification.
     */
    DEFINED_BY_VALIDITY_TIME_SPEC("definedByValidityTimeSpec");

    private final String value;

    Datex2SituationRecordValidyStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Datex2SituationRecordValidyStatus fromValue(String value) {
        return Datex2SituationRecordValidyStatus.valueOf(value);
    }


}
