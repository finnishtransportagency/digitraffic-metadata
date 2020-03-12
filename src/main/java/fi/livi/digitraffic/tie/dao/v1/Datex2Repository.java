package fi.livi.digitraffic.tie.dao.v1;

import java.time.Instant;
import java.util.List;

import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.model.v1.datex2.Datex2;

@Repository
public interface Datex2Repository extends JpaRepository<Datex2, Long> {
    @Query(value =
            "select max(datex2.import_date) as updated\n" +
            "from datex2\n" +
            "where message_type = :messageType",
            nativeQuery = true)
    Instant findLatestImportTime(@Param("messageType") final String messageType);

    @Query(value =
           "select max(d2.import_date) as updated\n" +
           "from datex2 d2\n" +
           "where d2.message_type = :messageType\n" +
           "  AND d2.json_message is not null",
           nativeQuery = true)
    Instant findLatestImportTimeWithJson(@Param("messageType") final String messageType);

    String FIND_ALL_ACTIVE_AS_D =
            "SELECT d.*\n" +
            "FROM datex2 d\n" +
            "WHERE d.id IN (\n" +
            "  SELECT datex2_id\n" +
            "  FROM (\n" +
            "         SELECT ROW_NUMBER() OVER (PARTITION BY situation.SITUATION_ID \n" +
            "                                   ORDER BY record.version_time DESC NULLS LAST, \n" +
            "                                            record.overall_end_time DESC NULLS FIRST) AS rnum\n" +
            "           , d.publication_time\n" +
            "           , d.id AS datex2_id\n" +
            "           , record.validy_status\n" +
            "           , record.overall_end_time\n" +
            "         FROM DATEX2 d\n" +
            "         INNER JOIN datex2_situation situation ON situation.datex2_id = d.id\n" +
            "         INNER JOIN datex2_situation_record record ON record.datex2_situation_id = situation.id\n" +
            "         WHERE d.message_type = :messageType\n" +
            "       ) disorder\n" +
            "  WHERE rnum = 1\n" +
            "    AND (disorder.overall_end_time IS NULL OR disorder.overall_end_time > current_timestamp - :activeInPastHours * interval '1 hour')\n" +
            "    AND (" +
            "           disorder.validy_status <> 'SUSPENDED'\n" +
            "       OR (disorder.validy_status = 'SUSPENDED' AND disorder.overall_end_time IS NOT null)\n" +
            "    )\n" +
            ")\n";
    String FIND_ALL_ACTIVE_AS_D_ORDER_BY = "order by d.publication_time, d.id";
    String FIND_ALL_ACTIVE_AS_D_WITH_JSON = FIND_ALL_ACTIVE_AS_D + "  AND d.json_message IS NOT NULL\n";

    @Query(value = FIND_ALL_ACTIVE_AS_D + FIND_ALL_ACTIVE_AS_D_ORDER_BY, nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findAllActive(final String messageType, final int activeInPastHours);

    @Query(value = FIND_ALL_ACTIVE_AS_D_WITH_JSON + FIND_ALL_ACTIVE_AS_D_ORDER_BY, nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findAllActiveWithJson(final String messageType, final int activeInPastHours);



    @Query(value =
        "SELECT d.*\n" +
        "FROM datex2 d\n" +
        "WHERE d.publication_time >= date_trunc('month', TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY'))\n" +
        "AND d.publication_time < LAST_DAY(TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY')) + 1\n" +
        "AND d.message_type = :messageType\n" +
        "ORDER BY d.publication_time desc, d.id desc",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findHistory(@Param("messageType") final String messageType,
                             @Param("year") final int year,
                             @Param("month") final int month);

    @Query(value =
        "SELECT d.*\n" +
        "FROM datex2 d\n" +
        "WHERE EXISTS (\n" +
        "    SELECT NULL FROM datex2_situation situation\n" +
        "    INNER JOIN datex2_situation_record record ON record.datex2_situation_id = situation.id\n" +
        "    WHERE situation.datex2_id = d.id\n" + "                AND situation.situation_id = :situationId\n" +
        "    AND d.publication_time >= date_trunc('month'. TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY'))\n" +
        "    AND d.publication_time < LAST_DAY(TO_DATE('1.' || :month || '.' || :year, 'DD.MM.YYYY')) + 1\n" +
        "    AND d.message_type = :messageType\n" + "            )\n" +
        "ORDER BY d.publication_time desc, d.id desc",
           nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findHistoryBySituationId(@Param("messageType") final String messageType,
                                          @Param("situationId") final String situationId,
                                          @Param("year") final int year,
                                          @Param("month") final int month);

    @Query(value =
        "SELECT d.*\n" +
        "FROM datex2 d\n" +
        "WHERE d.id in (\n" +
        "    SELECT situation.datex2_id\n" +
        "    FROM datex2_situation situation\n" +
        "    WHERE situation.situation_id = :situationId)\n" +
        "  AND message_type = :messageType", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findBySituationIdAndMessageType(@Param("situationId") final String situationId, @Param("messageType") final String messageType);

    @Query(value =
        "SELECT d.*\n" +
        "FROM datex2 d\n" +
        "WHERE d.id in (\n" +
        "    SELECT situation.datex2_id\n" +
        "    FROM datex2_situation situation\n" +
        "    WHERE situation.situation_id = :situationId)\n" +
        "  AND message_type = :messageType\n" +
        "  AND d.json_message IS NOT NULL", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findBySituationIdAndMessageTypeWithJson(@Param("situationId") final String situationId, @Param("messageType") final String messageType);


    @Query(value =
           "SELECT d.*\n" +
               "FROM datex2 d\n" +
               "WHERE d.id in (\n" +
               "    SELECT situation.datex2_id\n" +
               "    FROM datex2_situation situation\n" +
               "    WHERE situation.situation_id = :situationId)\n" +
               "  AND d.json_message is not null\n" +
               "  AND message_type = :messageType", nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Datex2> findBySituationIdAndMessageAndJson(final String situationId, final String messageType);


    @Query("SELECT CASE WHEN count(situation) > 0 THEN TRUE ELSE FALSE END\n" +
           "FROM Datex2Situation situation\n" +
           "WHERE situation.situationId = :situationId")
    boolean existsWithSituationId(@Param("situationId") final String situationId);

    @Query(value =
        "SELECT situation_id, version_time\n" +
        "FROM (\n" +
        "    SELECT ROW_NUMBER() OVER (PARTITION BY situation.SITUATION_ID ORDER BY record.version_time DESC) AS rnum\n" +
        "    , situation.situation_id, record.version_time\n" +
        "    FROM DATEX2 d\n" +
        "    INNER JOIN datex2_situation situation ON situation.datex2_id = d.id\n" +
        "    INNER JOIN datex2_situation_record record ON record.datex2_situation_id = situation.id\n" +
        "    WHERE d.message_type = :messageType\n" +
        ") d2\n" +
        "WHERE rnum = 1",
        nativeQuery = true)
    @QueryHints(@QueryHint(name="org.hibernate.fetchSize", value="1000"))
    List<Object[]> listDatex2SituationVersionTimes(@Param("messageType") final String messageType);

    @Query(value =
               "SELECT version_time\n" +
               "FROM (\n" +
               "    SELECT ROW_NUMBER() OVER (PARTITION BY situation.SITUATION_ID ORDER BY record.version_time DESC) AS rnum\n" +
               "         , record.version_time\n" +
               "    FROM DATEX2 d\n" +
               "    INNER JOIN datex2_situation situation ON situation.datex2_id = d.id\n" +
               "    INNER JOIN datex2_situation_record record ON record.datex2_situation_id = situation.id\n" +
               "    WHERE d.message_type = :messageType\n" +
               "      AND situation.situation_id = :situationId" +
               ") d2\n" +
               "WHERE rnum = 1",
           nativeQuery = true)
    Instant findDatex2SituationLatestVersionTime(final String situationId, final String messageType);
}
