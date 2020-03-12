package fi.livi.digitraffic.tie.dao.v1;

import java.util.Collections;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DirectionDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public DirectionDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createOrUpdateDirection(final int directionNaturalId, final String nameFi, final String nameSv,
                                        final String nameEn, final String roadDirection) {

        final HashMap<String, Object> args = new HashMap<>();
        args.put("naturalId", directionNaturalId);
        args.put("nameFi", nameFi);
        args.put("nameSv", nameSv);
        args.put("nameEn", nameEn);
        args.put("roadDirection", roadDirection);

        jdbcTemplate.update(
            "MERGE INTO DIRECTION d " +
            "USING (SELECT :naturalId natural_id FROM DUAL) src " +
            "ON (d.natural_id = src.natural_id) " +
            "WHEN MATCHED THEN " +
                "UPDATE SET name_fi = :nameFi, name_sv = :nameSv, name_en = :nameEn, rdi = :roadDirection, obsolete_date = null " +
            "WHEN NOT MATCHED THEN " +
                "INSERT (natural_id, name_fi, name_sv, name_en, rdi, obsolete_date) " +
                "VALUES (:naturalId, :nameFi, :nameSv, :nameEn, :roadDirection, null)", args);
    }

    public void makeNonObsoleteDirectionsObsolete() {
        jdbcTemplate.update("UPDATE DIRECTION SET obsolete_date = current_timestamp WHERE obsolete_date IS NULL", Collections.emptyMap());
    }
}
