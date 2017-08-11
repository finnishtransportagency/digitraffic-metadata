package fi.livi.digitraffic.tie.data.dao;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.model.Datex2;

@Repository
public interface Datex2Repository extends JpaRepository<Datex2, Long> {
    @Query(value =
            "select max(datex2.import_date) as updated\n" +
            "from datex2\n" +
            "where message_type = :messageType",
            nativeQuery = true)
    LocalDateTime findLatestImportTime(@Param("messageType") final String messageType);

    @Query(value =
            "SELECT d.*\n" +
            "FROM datex2 d\n" +
            "WHERE d.id IN (\n" +
            "  SELECT datex2_id\n" +
            "  FROM (\n" +
                      // Latest Datex2-message of situation and it's last record by end time\n" +
            "         SELECT ROW_NUMBER() OVER (PARTITION BY situation.SITUATION_ID ORDER BY d.PUBLICATION_TIME DESC, record.OVERALL_END_TIME DESC NULLS LAST) AS rnum\n" +
        "           , d.publication_time\n" +
        "           , d.id AS datex2_id\n" +
        "           , record.validy_status\n" +
        "           , nvl(record.overall_end_time, TO_DATE('9999', 'yyyy')) overall_end_time\n" +
        "         FROM DATEX2 d\n" +
        "         INNER JOIN datex2_situation situation ON situation.datex2_id = d.id\n" +
        "         INNER JOIN datex2_situation_record record ON record.datex2_situation_id = situation.id\n" +
        "         WHERE d.message_type = :messageType\n" +
            "       ) d2\n" +
            "  WHERE rnum = 1\n" +
            "        AND (d2.validy_status <> 'SUSPENDED'\n" +
            "             AND d2.overall_end_time > sysdate)\n" +
            // Skip old Datex2 messages of HÄTI system
            "        AND d2.publication_time > TO_DATE('201611', 'yyyymm')\n" +
            ")\n" +
            "order by d.publication_time, d.id",
            nativeQuery = true)
    List<Datex2> findAllActive(@Param("messageType") final String messageType);

    @Query(value =
            "SELECT d.*\n" +
            "FROM datex2 d\n" +
            "WHERE d.publication_time >= TRUNC(TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY'), 'MONTH')\n" +
            "  AND d.publication_time < LAST_DAY(TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY')) + 1\n" +
            "  AND d.message_type = :messageType",
            nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findHistory(@Param("messageType") final String messageType,
                             @Param("year") final int year,
                             @Param("month") final int month);

    @Query(value =
            "SELECT d.*\n" +
            "FROM datex2 d\n" +
            "WHERE EXISTS (\n" +
            "  SELECT NULL\n" +
            "  FROM datex2_situation situation\n" +
            "  INNER JOIN datex2_situation_record record ON record.datex2_situation_id = situation.id\n" +
            "  WHERE situation.datex2_id = d.id\n" +
            "    AND situation.situation_id = :situationId\n" +
            "    AND d.publication_time >= TRUNC(TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY'), 'MONTH')\n" +
            "    AND d.publication_time < LAST_DAY(TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY')) + 1\n" +
            ")",
            nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findHistoryBySituationId(@Param("situationId") final String situationId,
                             @Param("year") final int year,
                             @Param("month") final int month);

    @Query(value =
           "SELECT d.*\n" +
           "FROM datex2 d\n" +
           "WHERE d.id in (\n" +
           "  SELECT situation.datex2_id\n" +
           "  FROM datex2_situation situation\n" +
           "  WHERE situation.situation_id = :situationId\n" +
           ")",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="100"))
    List<Datex2> findBySituationId(@Param("situationId") final String situationId);

    @Query("SELECT CASE WHEN count(situation) > 0 THEN TRUE ELSE FALSE END\n" +
           "FROM Datex2Situation situation\n" +
           "WHERE situation.situationId = :situationId")
    boolean existsWithSituationId(@Param("situationId") final String situationId);

    @Query(value = "delete from datex2 where message_type='ROADWORK'", nativeQuery = true)
    void deleteAllRoadworks();
}
