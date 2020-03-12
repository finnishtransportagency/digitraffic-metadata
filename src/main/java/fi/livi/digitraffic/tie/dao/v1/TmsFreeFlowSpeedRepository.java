package fi.livi.digitraffic.tie.dao.v1;

import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dto.v1.tms.TmsFreeFlowSpeedDto;

@Repository
public interface TmsFreeFlowSpeedRepository extends JpaRepository<TmsFreeFlowSpeedDto, Long> {

    @Query(value =
            "SELECT RS.NATURAL_ID AS ROAD_STATION_NATURAL_ID" +
            "     , LS.NATURAL_ID AS TMS_NATURAL_ID\n" +
            "     , CASE WHEN RD.SPEED_LIMIT_SEASON = 1\n" +
            "            THEN LS.SUMMER_FREE_FLOW_SPEED_1\n" +
            "            ELSE LS.WINTER_FREE_FLOW_SPEED_1\n" +
            "       END AS FREE_FLOW_SPEED1\n" +
            "     , CASE WHEN RD.SPEED_LIMIT_SEASON = 1\n" +
            "            THEN LS.SUMMER_FREE_FLOW_SPEED_2\n" +
            "            ELSE LS.WINTER_FREE_FLOW_SPEED_2\n" +
            "        END AS FREE_FLOW_SPEED2\n" +
            "FROM LAM_STATION LS\n" +
            "INNER JOIN ROAD_STATION RS ON RS.ID = LS.ROAD_STATION_ID\n" +
            "INNER JOIN ROAD_DISTRICT RD ON LS.ROAD_DISTRICT_ID = RD.ID\n" +
            "WHERE LS.OBSOLETE_DATE IS NULL\n" +
            "  AND RS.IS_PUBLIC = true",
            nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<TmsFreeFlowSpeedDto> listAllPublicTmsFreeFlowSpeeds();

    @Query(value =
            "SELECT RS.NATURAL_ID AS ROAD_STATION_NATURAL_ID" +
            "     , LS.NATURAL_ID AS TMS_NATURAL_ID\n" +
            "     , CASE WHEN RD.SPEED_LIMIT_SEASON = 1\n" +
            "            THEN LS.SUMMER_FREE_FLOW_SPEED_1\n" +
            "            ELSE LS.WINTER_FREE_FLOW_SPEED_1\n" +
            "       END AS FREE_FLOW_SPEED1\n" +
            "     , CASE WHEN RD.SPEED_LIMIT_SEASON = 1\n" +
            "            THEN LS.SUMMER_FREE_FLOW_SPEED_2\n" +
            "            ELSE LS.WINTER_FREE_FLOW_SPEED_2\n" +
            "        END AS FREE_FLOW_SPEED2\n" +
            "FROM LAM_STATION LS\n" +
            "INNER JOIN ROAD_STATION RS ON RS.ID = LS.ROAD_STATION_ID\n" +
            "INNER JOIN ROAD_DISTRICT RD ON LS.ROAD_DISTRICT_ID = RD.ID\n" +
            "WHERE LS.OBSOLETE_DATE IS NULL\n" +
            "  AND RS.IS_PUBLIC = true\n" +
            "  AND RS.NATURAL_ID = ?1",
            nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<TmsFreeFlowSpeedDto> listAllPublicTmsFreeFlowSpeeds(final long roadStationNaturalId);
}
