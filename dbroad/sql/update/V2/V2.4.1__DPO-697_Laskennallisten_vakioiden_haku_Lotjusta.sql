CREATE TABLE TMS_SENSOR_CONSTANT
(
  LOTJU_ID            BIGINT,
  ROAD_STATION_ID     BIGINT NOT NULL,
  NAME                TEXT NOT NULL,
  UPDATED             TIMESTAMP(0) WITH TIME ZONE NOT NULL,
  OBSOLETE_DATE       TIMESTAMP(0) WITH TIME ZONE,
  CONSTRAINT TMS_SENSOR_CONSTANT_PKEY PRIMARY KEY (LOTJU_ID),
  CONSTRAINT U_TMS_SENSOR_CONSTANT UNIQUE(ROAD_STATION_ID, NAME)
);

CREATE TABLE TMS_SENSOR_CONSTANT_VALUE
(
  LOTJU_ID                  BIGINT,
  SENSOR_CONSTANT_LOTJU_ID  BIGINT NOT NULL REFERENCES TMS_SENSOR_CONSTANT (LOTJU_ID),
  VALUE                     INT NOT NULL,
  VALID_FROM                INT NOT NULL,
  VALID_TO                  INT NOT NULL,
  UPDATED                   TIMESTAMP(0) WITH TIME ZONE NOT NULL,
  OBSOLETE_DATE             TIMESTAMP(0) WITH TIME ZONE,
  CONSTRAINT TMS_SENSOR_CONSTANT_VALUE_PKEY PRIMARY KEY (LOTJU_ID)
);