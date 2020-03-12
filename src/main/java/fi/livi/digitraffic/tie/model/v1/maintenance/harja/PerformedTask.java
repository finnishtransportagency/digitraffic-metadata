
package fi.livi.digitraffic.tie.model.v1.maintenance.harja;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PerformedTask {

    PAVING("asfaltointi"),
    PLOUGHING_AND_SLUSH_REMOVAL("auraus ja sohjonpoisto"),
    SNOW_PLOUGHING_STICKS_AND_SNOW_FENCES("aurausviitoitus ja kinostimet"),
    BRUSHING("harjaus"),
    COMPACTION_BY_ROLLING("jyrays"),
    ROAD_STATE_CHECKING("kelintarkastus"),
    MECHANICAL_CUT("koneellinen niitto"),
    BRUSH_CLEARING("koneellinen vesakonraivaus"),
    HEATING("kuumennus"),
    CLEANSING_OF_REST_AREAS("l- ja p-alueiden puhdistus"),
    CLEANSING_OF_TRAFFIC_SIGNS("liikennemerkkien puhdistus"),
    MAINTENANCE_OF_GUIDE_SIGNS_AND_REFLECTOR_POSTS("liik. opast. ja ohjausl. hoito seka reunapaalujen kun.pito"),
    LINE_SANDING("linjahiekoitus"),
    TRASFER_OF_SNOW("lumensiirto"),
    LOWERING_OF_SNOWBANKS("lumivallien madaltaminen"),
    OTHER("muu"),
    DITCHING("ojitus"),
    CRACK_FILLING("paallysteiden juotostyot"),
    PATCHING("paallysteiden paikkaus"),
    REMOVAL_OF_BULGE_ICE("paannejaan poisto"),
    LEVELLING_OF_ROAD_SHOULDERS("palteen poisto"),
    LEVELLING_OF_ROAD_SURFACE("pinnan tasaus"),
    SPOT_SANDING("pistehiekoitus"),
    FILLING_OF_ROAD_SHOULDERS("paallystetyn tien sorapientareen taytto"),
    MIXING_OR_STABILIZATION("sekoitus tai stabilointi"),
    CLEANSING_OF_BRIDGES("siltojen puhdistus"),
    SPREADING_OF_CRUSH("sorastus"),
    FILLING_OF_GRAVEL_ROAD_SHOULDERS("sorapientareen taytto"),
    RESHAPING_GRAVEL_ROAD_SURFACE("sorateiden muokkaushoylays"),
    DUST_BINDING_OF_GRAVEL_ROAD_SURFACE("sorateiden polynsidonta"),
    LEVELLING_GRAVEL_ROAD_SURFACE("sorateiden tasaus"),
    PREVENTING_MELTING_WATER_PROBLEMS("sulamisveden haittojen torjunta"),
    SALTING("suolaus"),
    ROAD_MARKINGS("tiemerkinta"),
    ROAD_INSPECTIONS("tiestotarkastus"),
    CLIENTS_QUALITY_CONTROL("tilaajan laadunvalvonta"),
    SAFETY_EQUIPMENT("turvalaite");

    private final String value;
    private final static Map<String, PerformedTask> CONSTANTS = new HashMap<String, PerformedTask>();

    static {
        for (PerformedTask c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private PerformedTask(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    public static PerformedTask fromValue(final String value) {
        final PerformedTask constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
