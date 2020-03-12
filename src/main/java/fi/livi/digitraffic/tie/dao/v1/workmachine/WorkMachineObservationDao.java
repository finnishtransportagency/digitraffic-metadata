package fi.livi.digitraffic.tie.dao.v1.workmachine;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WorkMachineObservationDao {
    private static final Logger log = LoggerFactory.getLogger(WorkMachineObservationDao.class);

    @PersistenceContext
    EntityManager entityManager;

    private static final String INSERT_WITH_OBSERVATION_TIME =
            "INSERT INTO WORK_MACHINE_OBSERVATION_COORDINATE (work_machine_observation_id, order_number, longitude, latitude, observation_time)\n" +
            "VALUES (:observationId , :orderNumber, :longitude, :latitude, :observationTime)";

    private static final String INSERT_WITHOUT_OBSERVATION_TIME =
            "INSERT INTO WORK_MACHINE_OBSERVATION_COORDINATE (work_machine_observation_id, order_number, longitude, latitude, observation_time)\n" +
            "VALUES (:observationId , :orderNumber, :longitude, :latitude, null)";

    private static final String GET_LAST_COORDINATE_ORDER_NUMBER =
            "SELECT COALESCE(MAX(c.order_number), -1) as lastOrder\n" +
            "FROM WORK_MACHINE_OBSERVATION_COORDINATE c\n" +
            "WHERE c.work_machine_observation_id = :observationId";

    @Autowired
    public WorkMachineObservationDao(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public int addCoordinates(final long observationId,
                              final int orderNumber,
                              final BigDecimal longitude,
                              final BigDecimal latitude,
                              final ZonedDateTime observationTime) {

        Query query = entityManager.createNativeQuery(observationTime != null ?
                                                      INSERT_WITH_OBSERVATION_TIME: INSERT_WITHOUT_OBSERVATION_TIME);

        query.setParameter("observationId", observationId);
        query.setParameter("orderNumber", orderNumber);
        query.setParameter("longitude", longitude);
        query.setParameter("latitude", latitude);
        // Workaround for a bug in Hibernate
        // https://stackoverflow.com/questions/8211195/postgresql-jdbc-null-string-taken-as-a-bytea
        // Caused by: org.postgresql.util.PSQLException: ERROR: column "observation_time" is of type timestamp with time zone but expression is of type bytea
        // Hint: You will need to rewrite or cast the expression.
        if (observationTime != null) {
            query.setParameter("observationTime", observationTime);
        }
        return query.executeUpdate();
    }

    /**
     *
     * @param observationId to find coordinate order
     * @return last coordinate order or -1 if not exists.
     */
    public int getLastCoordinateOrder(final long observationId) {
        Query query = entityManager.createNativeQuery(GET_LAST_COORDINATE_ORDER_NUMBER);
        query.setParameter("observationId", observationId);
        return (Integer) query.getSingleResult();
    }
}
