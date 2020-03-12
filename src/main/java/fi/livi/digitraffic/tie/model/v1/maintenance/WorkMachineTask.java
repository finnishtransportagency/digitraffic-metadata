package fi.livi.digitraffic.tie.model.v1.maintenance;

import java.util.Arrays;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
public class WorkMachineTask {

    public enum Task {
        PAVING("Asfaltointi","Asfaltering","Paving"),
        PLOUGHING_AND_SLUSH_REMOVAL("Auraus ja sohjonpoisto","Plog- och sörjröjning","Ploughing and slush removal"),
        SNOW_PLOUGHING_STICKS_AND_SNOW_FENCES("Aurausviitoitus ja kinostimet","Plogkäppsmarkering och snödrivor","Snow-ploughing sticks and snow fences"),
        BRUSHING("Harjaus","Borstning","Brushing"),
        COMPACTION_BY_ROLLING("Jyrays","Vibrering","Compaction by rolling"),
        ROAD_STATE_CHECKING("Kelintarkastus","Väglagsgranskning","Road state checking"),
        MECHANICAL_CUT("Koneellinen niitto","Vägslotter","Mechanical cut"),
        BRUSH_CLEARING("Koneellinen vesakonraivaus"," slyklippning","Brush clearing"),
        HEATING("Kuumennus","Upphettning","Heating"),
        CLEANSING_OF_REST_AREAS("L- ja p-alueiden puhdistus","Rast- och parkeringsplatsernas rengöring","Cleansing of rest areas"),
        CLEANSING_OF_TRAFFIC_SIGNS("Liikennemerkkien puhdistus","Rengöring av trafikmärken","Cleansing of traffic signs"),
        MAINTENANCE_OF_GUIDE_SIGNS_AND_REFLECTOR_POSTS("Liik. opast. ja ohjausl. hoito seka reunapaalujen kun.pito","Trafikanordnigars och kantpålars underhåll","Maintenance of guide signs and reflector posts"),
        LINE_SANDING("Linjahiekoitus","Linjesandning","Line sanding"),
        TRASFER_OF_SNOW("Lumensiirto","Bortforsling av snömassor","Trasfer of snow"),
        LOWERING_OF_SNOWBANKS("Lumivallien madaltaminen","Nedsänknig av plogvall","Lowering of snowbanks"),
        OTHER("Muu","Annat dylikt","Other"),
        DITCHING("Ojitus","Dikning","Ditching"),
        CRACK_FILLING("Paallysteiden juotostyot","Fyllnig av sprickor i beläggningen","Crack filling"),
        PATCHING("Paallysteiden paikkaus","Lappning av beläggning","Patching"),
        REMOVAL_OF_BULGE_ICE("Paannejaan poisto","Borttagning av svall-is","Removal of bulge ice"),
        LEVELLING_OF_ROAD_SHOULDERS("Palteen poisto","Kantskärning","Levelling of road shoulders"),
        LEVELLING_OF_ROAD_SURFACE("Pinnan tasaus","Utjämning av vägytan","Levelling of road surface"),
        SPOT_SANDING("Pistehiekoitus","Punktsandning","Spot sanding"),
        FILLING_OF_ROAD_SHOULDERS("Paallystetyn tien sorapientareen taytto","Belagda vägars kantfyllning","Filling of road shoulders"),
        MIXING_OR_STABILIZATION("Sekoitus tai stabilointi","Blandning och stabilisering","Mixing or stabilization"),
        CLEANSING_OF_BRIDGES("Siltojen puhdistus","Rengöring av broar","Cleansing of bridges"),
        SPREADING_OF_CRUSH("Sorastus","Grusning","Spreading of crush"),
        FILLING_OF_GRAVEL_ROAD_SHOULDERS("Sorapientareen taytto","Fyllning av gruskanter","Filling of gravel road shoulders"),
        RESHAPING_GRAVEL_ROAD_SURFACE("Sorateiden muokkaushoylays","Grusvägsytans omblandning ","Reshaping gravel road surface"),
        DUST_BINDING_OF_GRAVEL_ROAD_SURFACE("Sorateiden polynsidonta","Dammbindning av grusväg","Dust binding of gravel road surface"),
        LEVELLING_GRAVEL_ROAD_SURFACE("Sorateiden tasaus","Utjämning av grusväg","Levelling gravel road surface"),
        PREVENTING_MELTING_WATER_PROBLEMS("Sulamisveden haittojen torjunta","Smältvattensbekämpning","Preventing melting water problems"),
        SALTING("Suolaus","Saltning","Salting"),
        ROAD_MARKINGS("Tiemerkinta","Vägmarkering","Road markings"),
        ROAD_INSPECTIONS("Tiestotarkastus","Väggranskning","Road inspections "),
        CLIENTS_QUALITY_CONTROL("Tilaajan laadunvalvonta","Beställarens kvalitetsövervakning","Clients quality control"),
        SAFETY_EQUIPMENT("Turvalaite","Säkerhetsanordning","Safety equipment");


        private final String nameFi;
        private final String nameSv;
        private final String nameEn;

        Task(final String nameFi, final String nameSv, final String nameEn) {
            this.nameFi = nameFi;
            this.nameSv = nameSv;
            this.nameEn = nameEn;
        }

        public String getNameFi() {
            return nameFi;
        }

        public String getNameSv() {
            return nameSv;
        }

        public String getNameEn() {
            return nameEn;
        }

        public static Task getByName(String name) {
            return Arrays.stream(values())
                .filter(task -> task.name().equals(name))
                .findFirst().orElse(null);
        }
    }

    @EmbeddedId
    private WorkMachineTaskPK id;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="WORK_MACHINE_COORDINATE_OBSERVATION_ID", referencedColumnName = "WORK_MACHINE_OBSERVATION_ID", nullable = false, insertable=false, updatable = false),
        @JoinColumn(name="WORK_MACHINE_COORDINATE_ORDER_NUMBER", referencedColumnName = "ORDER_NUMBER", nullable = false, insertable=false, updatable = false)
    })
    private WorkMachineObservationCoordinate workMachineObservationCoordinate;

    public WorkMachineTask() {
    }

    public Task getTask() {
        return id.getTask();
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringExcluded(getTask());
    }
}
