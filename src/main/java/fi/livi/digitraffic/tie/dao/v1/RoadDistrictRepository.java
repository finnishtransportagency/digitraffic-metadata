package fi.livi.digitraffic.tie.dao.v1;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fi.livi.digitraffic.tie.model.v1.RoadDistrict;

public interface RoadDistrictRepository extends JpaRepository<RoadDistrict, Long> {

    RoadDistrict findByNaturalId(final int roadDistrictNaturalId);

    @Query(value =
            "SELECT RD.*\n" +
            "FROM ROAD_DISTRICT RD\n" +
            "INNER JOIN ROAD_SECTION RS ON RS.ROAD_DISTRICT_ID = RD.ID\n" +
            "INNER JOIN ROAD R ON R.ID = RS.ROAD_ID\n" +
            "WHERE RS.NATURAL_ID = :roadSectionNaturalId\n" +
            "  AND R.NATURAL_ID = :roadNaturalId",
            nativeQuery = true)
    RoadDistrict findByRoadSectionAndRoadNaturalId(@Param("roadSectionNaturalId") final int roadSectionNaturalId,
                                                   @Param("roadNaturalId") final int roadNaturalId);
}
