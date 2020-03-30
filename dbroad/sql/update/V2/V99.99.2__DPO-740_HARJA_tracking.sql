-- ALTER SEQUENCE SEQ_WORK_MACHINE_TRACKING RENAME TO SEQ_WORK_MACHINE_TRACKING_V1;
-- ALTER TABLE WORK_MACHINE_TRACKING RENAME TO WORK_MACHINE_TRACKING_V1;
--
-- ALTER SEQUENCE SEQ_WORK_MACHINE RENAME TO SEQ_WORK_MACHINE_V1;
-- ALTER TABLE WORK_MACHINE RENAME TO WORK_MACHINE_V1;

CREATE SEQUENCE SEQ_MAINTENANCE_TRACKING_WORK_MACHINE;
CREATE SEQUENCE SEQ_MAINTENANCE_TRACKING;

CREATE TABLE IF NOT EXISTS MAINTENANCE_TRACKING_WORK_MACHINE (
    ID                BIGINT PRIMARY KEY,
    HARJA_ID          BIGINT NOT NULL,
    HARJA_URAKKA_ID   BIGINT NOT NULL,
    TYPE              TEXT   NOT NULL
);

CREATE UNIQUE INDEX ON MAINTENANCE_TRACKING_WORK_MACHINE (HARJA_ID, HARJA_URAKKA_ID);

CREATE TABLE IF NOT EXISTS MAINTENANCE_TRACKING
(
    id                      BIGINT NOT NULL PRIMARY KEY,
    sending_system          TEXT NOT NULL,
    sending_time            TIMESTAMP(0) WITH TIME ZONE NOT NULL,
    last_point              GEOMETRY(POINTZ, 4326) NOT NULL, -- 4326 = WGS84
    line_string             GEOMETRY(LINESTRINGZ, 4326),     -- 4326 = WGS84
    work_machine_id         BIGINT REFERENCES MAINTENANCE_TRACKING_WORK_MACHINE(id) NOT NULL,
    start_time              TIMESTAMP(0) WITH TIME ZONE NOT NULL,
    end_time                TIMESTAMP(0) WITH TIME ZONE NOT NULL,
    direction               NUMERIC(5,2),
    finished                BOOLEAN NOT NULL,
    created                 TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW(),
    modified                TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TRIGGER MAINTENANCE_TRACKING_MODIFIED_T BEFORE UPDATE ON MAINTENANCE_TRACKING FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

CREATE INDEX MAINTENANCE_TRACKING_WORK_MACHINE_ID_FKEY_I ON MAINTENANCE_TRACKING USING BTREE (work_machine_id);
CREATE INDEX MAINTENANCE_TRACKING_END_TIME_ID_I ON MAINTENANCE_TRACKING USING BTREE (end_time, id);
CREATE INDEX MAINTENANCE_TRACKING_LAST_POINT_I ON MAINTENANCE_TRACKING USING GIST (last_Point);

-- many-to-many data <-> tracking
CREATE TABLE IF NOT EXISTS MAINTENANCE_TRACKING_DATA_TRACKING
(
    data_id         BIGINT
        REFERENCES MAINTENANCE_TRACKING_DATA(id) ON DELETE CASCADE NOT NULL,
    tracking_id     BIGINT
        REFERENCES MAINTENANCE_TRACKING(id) ON DELETE CASCADE NOT NULL,
    PRIMARY KEY(data_id, tracking_id)
);

CREATE INDEX MAINTENANCE_TRACKING_DATA_TRACKING_TRACKING_ID_FKEY_I ON MAINTENANCE_TRACKING_DATA_TRACKING USING BTREE (tracking_id, data_id);
CREATE INDEX MAINTENANCE_TRACKING_DATA_TRACKING_DATA_ID_FKEY_I ON MAINTENANCE_TRACKING_DATA_TRACKING USING BTREE (data_id);
