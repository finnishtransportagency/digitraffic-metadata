CREATE TABLE CAMERA_PRESET_HISTORY (
    preset_id       CHARACTER VARYING(32) NOT NULL, -- REFERENCES CAMERA_PRESET (preset_id),
    version_id      CHARACTER VARYING(32) NOT NULL,
    last_modified   TIMESTAMP(6) WITH TIME ZONE,
    publishable     BOOLEAN,
    size            INTEGER
);

ALTER TABLE CAMERA_PRESET_HISTORY
    ADD CONSTRAINT PRESET_HISTORY_PK PRIMARY KEY (preset_id, version_id);

CREATE INDEX CAMERA_PRESET_HISTORY_LAST_MODIFIED_PUBLISHABLE_I
    ON CAMERA_PRESET_HISTORY
        USING BTREE (last_modified ASC, preset_id ASC, version_id) where publishable = TRUE;