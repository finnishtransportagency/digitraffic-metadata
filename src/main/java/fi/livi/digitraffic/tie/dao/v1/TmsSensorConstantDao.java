package fi.livi.digitraffic.tie.dao.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;

@Repository
public class TmsSensorConstantDao {

    private static final Logger log = LoggerFactory.getLogger(TmsSensorConstantDao.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final static String UPSERT_CONSTANTS_STATEMENT =
        "WITH TMS_RS AS (\n" +
        "    SELECT rs.id as rs_id, rs.lotju_id rs_lotju_id\n" +
        "    FROM road_station rs\n" +
        "    WHERE rs.road_station_type = 'TMS_STATION'\n" +
        "      AND rs.lotju_id = :sensorConstantRoadStationLotjuId\n" +
        ")\n" +
        "INSERT INTO TMS_SENSOR_CONSTANT (LOTJU_ID, ROAD_STATION_ID, NAME, UPDATED)\n" +
        "SELECT :sensorConstantLotjuId, tms_rs.rs_id, :sensorConstantName, now()\n" +
        "FROM TMS_RS\n" +
        "ON CONFLICT (LOTJU_ID)\n" +
        "DO UPDATE SET\n" +
        "  name = :sensorConstantName,\n" +
        "  road_station_id = excluded.ROAD_STATION_ID,\n" +
        "  obsolete_date = null,\n" +
        "  updated = now()\n" +
        "WHERE TMS_SENSOR_CONSTANT.name <> :sensorConstantName\n" +
        "   OR TMS_SENSOR_CONSTANT.road_station_id <> excluded.ROAD_STATION_ID\n" +
        "   OR TMS_SENSOR_CONSTANT.obsolete_date is not null";

    private final static String UPSERT_CONSTANT_VALUES_STATEMENT =
        "WITH sc AS (\n" +
        "  SELECT lotju_id\n" +
        "  FROM TMS_SENSOR_CONSTANT\n" +
        "  WHERE LOTJU_ID = :sensorConstantLotjuId\n" +
        ")\n" +
        "INSERT INTO TMS_SENSOR_CONSTANT_VALUE (LOTJU_ID, SENSOR_CONSTANT_LOTJU_ID, VALUE, VALID_FROM, VALID_TO, UPDATED)\n" +
        "SELECT :sensorConstantValueLotjuId, sc.LOTJU_ID, :sensorConstantValue, :sensorConstantValidFrom, :sensorConstantValidTo, now()\n" +
        "FROM sc\n" +
        "ON CONFLICT (LOTJU_ID)\n" +
        "DO UPDATE set\n" +
        "    sensor_constant_lotju_id = EXCLUDED.sensor_constant_lotju_id,\n" +
        "    value = EXCLUDED.value,\n" +
        "    valid_from = EXCLUDED.valid_from,\n" +
        "    valid_to = EXCLUDED.valid_to,\n" +
        "    obsolete_date = null,\n" +
        "    updated = now()\n" +
        "WHERE TMS_SENSOR_CONSTANT_VALUE.sensor_constant_lotju_id <> EXCLUDED.sensor_constant_lotju_id\n" +
        "   OR TMS_SENSOR_CONSTANT_VALUE.value <> EXCLUDED.value\n" +
        "   OR TMS_SENSOR_CONSTANT_VALUE.valid_from <> EXCLUDED.valid_from\n" +
        "   OR TMS_SENSOR_CONSTANT_VALUE.valid_to <> EXCLUDED.valid_to\n" +
        "   OR TMS_SENSOR_CONSTANT_VALUE.obsolete_date is not null";

    // Updates free flow speeds by getting speeds for winter at 1.1. and for summer at 1.7.
    private final String UPDATE_FREE_FLOW_SPEEDS =
        "WITH sv AS (\n" +
        "    select sc.ROAD_STATION_ID, sc.NAME, scv.VALID_FROM, scv.VALID_TO, scv.VALUE\n" +
        "    from TMS_SENSOR_CONSTANT sc\n" +
        "    inner join TMS_SENSOR_CONSTANT_VALUE scv on scv.SENSOR_CONSTANT_LOTJU_ID = sc.LOTJU_ID\n" +
        "    inner join road_station rs on rs.id = sc.ROAD_STATION_ID\n" +
        "    where sc.NAME like 'VVAPAAS%'\n" +
        "      and sc.OBSOLETE_DATE is null)\n" +
        ", winter_free_flow_speed_1 as (\n" +
        "    SELECT sv.ROAD_STATION_ID, sv.NAME, sv.VALID_FROM, sv.VALID_TO, sv.VALUE\n" +
        "    from sv\n" +
        "    where (sv.VALID_FROM = 101 or sv.VALID_TO = 101 OR (sv.VALID_FROM >= 101 and sv.VALID_TO > 101 and sv.VALID_FROM >= sv.VALID_TO))\n" +
        "      and sv.name = 'VVAPAAS1'\n" +
        "), winter_free_flow_speed_2 as (\n" +
        "    SELECT sv.ROAD_STATION_ID, sv.NAME, sv.VALID_FROM, sv.VALID_TO, sv.VALUE\n" +
        "    from sv\n" +
        "    where (sv.VALID_FROM = 101 or sv.VALID_TO = 101 OR (sv.VALID_FROM >= 101 and sv.VALID_TO > 101 and sv.VALID_FROM >= sv.VALID_TO))\n" +
        "      and sv.name = 'VVAPAAS2'\n" +
        "), summer_free_flow_speed_1 as (\n" +
        "    SELECT sv.ROAD_STATION_ID, sv.NAME, sv.VALID_FROM, sv.VALID_TO, sv.VALUE\n" +
        "    from sv\n" +
        "    where ((sv.VALID_FROM <= 701 and sv.VALID_TO >= 701)\n" +
        "        OR (sv.VALID_FROM >= 701 and sv.VALID_TO >= 701 and sv.VALID_FROM >= sv.VALID_TO)\n" +
        "        OR (sv.VALID_FROM <= 701 and sv.VALID_TO <= 701 and sv.VALID_FROM >= sv.VALID_TO))\n" +
        "      and sv.name = 'VVAPAAS1'\n" +
        "), summer_free_flow_speed_2 as (\n" +
        "    SELECT sv.ROAD_STATION_ID, sv.NAME, sv.VALID_FROM, sv.VALID_TO, sv.VALUE\n" +
        "    from sv\n" +
        "    where ((sv.VALID_FROM <= 701 and sv.VALID_TO >= 701)\n" +
        "        OR (sv.VALID_FROM >= 701 and sv.VALID_TO >= 701 and sv.VALID_FROM >= sv.VALID_TO)\n" +
        "        OR (sv.VALID_FROM <= 701 and sv.VALID_TO <= 701 and sv.VALID_FROM >= sv.VALID_TO))\n" +
        "      and sv.name = 'VVAPAAS2'\n" +
        "), data as (\n" +
        "    select distinct\n" +
        "           sv.ROAD_STATION_ID,\n" +
        "           w1.VALUE as winter_free_flow_speed_1,\n" +
        "           w2.VALUE as winter_free_flow_speed_2,\n" +
        "           s1.VALUE as summer_free_flow_speed_1,\n" +
        "           s2.VALUE as summer_free_flow_speed_2\n" +
        "    from sv\n" +
        "    left outer join winter_free_flow_speed_1 w1 on w1.ROAD_STATION_ID = sv.ROAD_STATION_ID\n" +
        "    left outer join winter_free_flow_speed_2 w2 on w2.ROAD_STATION_ID = sv.ROAD_STATION_ID\n" +
        "    left outer join summer_free_flow_speed_1 s1 on s1.ROAD_STATION_ID = sv.ROAD_STATION_ID\n" +
        "    left outer join summer_free_flow_speed_2 s2 on s2.ROAD_STATION_ID = sv.ROAD_STATION_ID\n" +
        ")\n" +
        "update lam_station as lam\n" +
        "set summer_free_flow_speed_1 = coalesce(data.summer_free_flow_speed_1, -1),\n" +
        "    summer_free_flow_speed_2 = coalesce(data.summer_free_flow_speed_2, -1),\n" +
        "    winter_free_flow_speed_1 = coalesce(data.winter_free_flow_speed_1, -1),\n" +
        "    winter_free_flow_speed_2 = coalesce(data.winter_free_flow_speed_2, -1)\n" +
        "from data\n" +
        "where lam.road_station_id = data.ROAD_STATION_ID\n" +
        " and (lam.summer_free_flow_speed_1 <> data.summer_free_flow_speed_1\n" +
        "   or lam.summer_free_flow_speed_2 <> data.summer_free_flow_speed_2\n" +
        "   or lam.winter_free_flow_speed_1 <> data.winter_free_flow_speed_1\n" +
        "   or lam.winter_free_flow_speed_2 <> data.winter_free_flow_speed_2)";


    @Autowired
    public TmsSensorConstantDao(final NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public int obsoleteSensorConstants(Collection<Long> excludeLotjuIds) {
        final Map<String, Collection<Long>> paramMap = Collections.singletonMap("ids", excludeLotjuIds);
        return namedParameterJdbcTemplate.update(
            "UPDATE TMS_SENSOR_CONSTANT\n" +
            "SET OBSOLETE_DATE = now()\n" +
            "WHERE lotju_id not in (:ids)\n" +
            "  AND obsolete_date is null",
            paramMap);
    }

    public int updateSensorConstants(List<LamAnturiVakioVO> allLamAnturiVakios) {

        final ArrayList<Map<String, Object>> batchData = new ArrayList<>();
        allLamAnturiVakios.forEach(lamAnturiVakio -> {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("sensorConstantLotjuId", lamAnturiVakio.getId());
            params.put("sensorConstantName", lamAnturiVakio.getNimi());
            params.put("sensorConstantRoadStationLotjuId", lamAnturiVakio.getAsemaId());
            batchData.add(params);
        });

        int[] upserts = namedParameterJdbcTemplate.batchUpdate(
            UPSERT_CONSTANTS_STATEMENT,
            batchData.toArray(new Map[0]));

        return countUpserts(upserts);
    }

    public int updateSensorConstantValues(final List<LamAnturiVakioArvoVO> allLamAnturiVakioArvos) {


        final ArrayList<Map<String, Object>> batchData = new ArrayList<>();
        allLamAnturiVakioArvos.forEach(v -> {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("sensorConstantValueLotjuId", v.getId());
            params.put("sensorConstantLotjuId", v.getAnturiVakioId());
            params.put("sensorConstantValue", v.getArvo());
            params.put("sensorConstantValidFrom", v.getVoimassaAlku());
            params.put("sensorConstantValidTo", v.getVoimassaLoppu());
            batchData.add(params);
        });

        int[] upserts = namedParameterJdbcTemplate.batchUpdate(
                UPSERT_CONSTANT_VALUES_STATEMENT,
                batchData.toArray(new Map[0]));

        return countUpserts(upserts);
    }

    private int countUpserts(final int[] upserts) {
        int count = 0;
        for (int value : upserts) {
            if (value >= 0) {
                count += value;
            } else if (value == -2) {
                count += 1;
            } else {
                log.error("Upsert return value {}", value);
            }
        }
        return count;
    }

    public int updateFreeFlowSpeedsOfTmsStations() {
        return namedParameterJdbcTemplate.getJdbcTemplate().update(UPDATE_FREE_FLOW_SPEEDS);
    }

    public int obsoleteSensorConstantValues(Collection<Long> excludeLotjuIds) {
        final Map<String, Collection<Long>> paramMap = Collections.singletonMap("ids", excludeLotjuIds);
        return namedParameterJdbcTemplate.update(
                "UPDATE TMS_SENSOR_CONSTANT_VALUE\n" +
                "SET OBSOLETE_DATE = now()\n" +
                "WHERE lotju_id not in (:ids)\n" +
                "AND obsolete_date is null",
            paramMap);
    }
}