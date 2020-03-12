package fi.livi.digitraffic.tie.model.v1.datex2;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.livi.digitraffic.tie.service.IllegalArgumentException;
import fi.livi.digitraffic.tie.datex2.AbnormalTraffic;
import fi.livi.digitraffic.tie.datex2.Accident;
import fi.livi.digitraffic.tie.datex2.AnimalPresenceObstruction;
import fi.livi.digitraffic.tie.datex2.AuthorityOperation;
import fi.livi.digitraffic.tie.datex2.CarParks;
import fi.livi.digitraffic.tie.datex2.Conditions;
import fi.livi.digitraffic.tie.datex2.ConstructionWorks;
import fi.livi.digitraffic.tie.datex2.DisturbanceActivity;
import fi.livi.digitraffic.tie.datex2.EnvironmentalObstruction;
import fi.livi.digitraffic.tie.datex2.EquipmentOrSystemFault;
import fi.livi.digitraffic.tie.datex2.GeneralInstructionOrMessageToRoadUsers;
import fi.livi.digitraffic.tie.datex2.GeneralNetworkManagement;
import fi.livi.digitraffic.tie.datex2.GeneralObstruction;
import fi.livi.digitraffic.tie.datex2.GenericSituationRecord;
import fi.livi.digitraffic.tie.datex2.InfrastructureDamageObstruction;
import fi.livi.digitraffic.tie.datex2.MaintenanceWorks;
import fi.livi.digitraffic.tie.datex2.NonWeatherRelatedRoadConditions;
import fi.livi.digitraffic.tie.datex2.PoorEnvironmentConditions;
import fi.livi.digitraffic.tie.datex2.PublicEvent;
import fi.livi.digitraffic.tie.datex2.ReroutingManagement;
import fi.livi.digitraffic.tie.datex2.RoadConditions;
import fi.livi.digitraffic.tie.datex2.RoadOperatorServiceDisruption;
import fi.livi.digitraffic.tie.datex2.RoadOrCarriagewayOrLaneManagement;
import fi.livi.digitraffic.tie.datex2.RoadsideAssistance;
import fi.livi.digitraffic.tie.datex2.RoadsideServiceDisruption;
import fi.livi.digitraffic.tie.datex2.SignSetting;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.datex2.SpeedManagement;
import fi.livi.digitraffic.tie.datex2.TransitInformation;
import fi.livi.digitraffic.tie.datex2.VehicleObstruction;
import fi.livi.digitraffic.tie.datex2.WeatherRelatedRoadConditions;
import fi.livi.digitraffic.tie.datex2.WinterDrivingManagement;
import io.swagger.annotations.ApiModel;

@ApiModel(value = "SituationRecordType", description = "Datex2 situation record type record")
public enum Datex2SituationRecordType {

    // GenericSituationRecord
    GENERIC_SITUATION_RECORD(GenericSituationRecord.class),
    // NonRoadEventInformation
    NON_ROADEVENT_INFORMATION_CAR_PARKS(CarParks.class),
    NON_ROADEVENT_INFORMATION_ROAD_OPERATOR_SERVICE_DISRUPTION(RoadOperatorServiceDisruption.class),
    NON_ROADEVENT_INFORMATION_ROADSIDE_SERVICE_DISRUPTION(RoadsideServiceDisruption.class),
    NON_ROADEVENT_INFORMATION_TRANSIT_INFORMATION(TransitInformation.class),
    // TrafficElement/
    TRAFFIC_ELEMENT_ABNORMAL_TRAFFIC(AbnormalTraffic.class),
    TRAFFIC_ELEMENT_ACCIDENT(Accident.class),
    TRAFFIC_ELEMENT_EQUIPMENT_OR_SYSTEMFAULT(EquipmentOrSystemFault.class),
    TRAFFIC_ELEMENT_ACTIVITY_DISTURBANCE_ACTIVITY(DisturbanceActivity.class),
    TRAFFIC_ELEMENT_ACTIVITY_AUTHORITY_OPERATION(AuthorityOperation.class),
    TRAFFIC_ELEMENT_ACTIVITY_PUBLIC_EVENT(PublicEvent.class),
    TRAFFIC_ELEMENT_OBSTRUCTION_ANIMAL_PRESENCE_OBSTRUCTION(AnimalPresenceObstruction.class),
    TRAFFIC_ELEMENT_OBSTRUCTION_ENVIRONMENTAL_OBSTRUCTION(EnvironmentalObstruction.class),
    TRAFFIC_ELEMENT_OBSTRUCTION_GENERAL_OBSTRUCTION(GeneralObstruction.class),
    TRAFFIC_ELEMENT_OBSTRUCTION_INFRASTRUCTURE_DAMAGEOBSTRUCTION(InfrastructureDamageObstruction.class),
    TRAFFIC_ELEMENT_OBSTRUCTION_VEHICLE_OBSTRUCTION(VehicleObstruction.class),
    TRAFFIC_ELEMENT_CONDITIONS(Conditions.class),
    TRAFFIC_ELEMENT_CONDITIONS_POOR_ENVIRONMENT_CONDITIONS(PoorEnvironmentConditions.class),
    TRAFFIC_ELEMENT_CONDITIONS_ROADCONDITIONS(RoadConditions.class),
    TRAFFIC_ELEMENT_CONDITIONS_NON_WEATHER_RELATED_ROAD_CONDITIONS(NonWeatherRelatedRoadConditions.class), // TRAFFIC_ELEMENT_CONDITIONS_NONWEATHERRELATEDROADCONDITIONS
    TRAFFIC_ELEMENT_CONDITIONS_WEATHER_RELATED_ROAD_CONDITIONS(WeatherRelatedRoadConditions.class), // TRAFFIC_ELEMENT_CONDITIONS_WEATHERRELATEDROADCONDITIONS
    // OperatorAction
    OPERATOR_ACTION_ROADSIDE_ASSISTANCE(RoadsideAssistance.class),
    OPERATOR_ACTION_ROADWORKS_CONSTRUCTION_WORKS(ConstructionWorks.class),
    OPERATOR_ACTION_ROADWORKS_MAINTENANCE_WORKS(MaintenanceWorks.class),
    OPERATOR_ACTION_SIGN_SETTING(SignSetting.class),
    OPERATOR_ACTION_NETWORK_MANAGEMENT_GENERAL_INSTRUCTION_OR_MESSAGE_TO_ROAD_USERS(GeneralInstructionOrMessageToRoadUsers.class),
    OPERATOR_ACTION_NETWORK_MANAGEMENT_GENERAL_NETWORK_MANAGEMENT(GeneralNetworkManagement.class),
    OPERATOR_ACTION_NETWORK_MANAGEMENT_REROUTING_MANAGEMENT(ReroutingManagement.class),
    OPERATOR_ACTION_NETWORK_MANAGEMENT_ROAD_OR_CARRIAGEWAY_OR_LANE_MANAGEMENT(RoadOrCarriagewayOrLaneManagement.class),
    OPERATOR_ACTION_NETWORK_MANAGEMENT_SPEED_MANAGEMENT(SpeedManagement.class),
    OPERATOR_ACTION_NETWORK_MANAGEMENT_WINTER_DRIVING_MANAGEMENT(WinterDrivingManagement.class),
    ;

    private static final Logger log = LoggerFactory.getLogger(Datex2SituationRecordType.class);

    private final Class<? extends SituationRecord> situationRecordClass;

    private static final Map<Class<? extends SituationRecord>, Datex2SituationRecordType> lookup = new HashMap();

    static{
        for (final Datex2SituationRecordType type : EnumSet.allOf(Datex2SituationRecordType.class)) {
            lookup.put(type.getSituationRecordClass(), type);
        }
    }

    Datex2SituationRecordType(Class<? extends SituationRecord> situationRecordClass) {
        this.situationRecordClass = situationRecordClass;
    }

    public Class<? extends SituationRecord> getSituationRecordClass() {
        return situationRecordClass;
    }

    public static Datex2SituationRecordType fromRecord(Class<? extends SituationRecord> situationRecordClass) {
        Datex2SituationRecordType type = lookup.get(situationRecordClass);
        if (type == null) {
            throw new IllegalArgumentException("Type " + situationRecordClass.getName() + " not suported!");
        }
        return type;
    }

}
