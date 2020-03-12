package fi.livi.digitraffic.tie.conf.postgres;

import java.sql.Types;

import org.hibernate.spatial.dialect.postgis.PostgisPG95Dialect;

/**
 * Dialect enables usage for PostgresSQL jsonb data type.
 */
public class JsonPostgisPG95Dialect extends PostgisPG95Dialect {

    public JsonPostgisPG95Dialect() {
        this.registerColumnType(Types.JAVA_OBJECT, "jsonb");
    }
}