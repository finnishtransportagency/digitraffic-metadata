INSERT INTO allowed_road_station_sensor (id, natural_id, road_station_type)
    SELECT nextval('seq_allowed_sensor'), s.natural_id, s.road_station_type
    FROM road_station_sensor s
    WHERE s.road_station_type = 'TMS_STATION'
    AND s.name_fi IN( 'KESKINOPEUS_5MIN_KIINTEA_SUUNTA1',
                      'KESKINOPEUS_5MIN_KIINTEA_SUUNTA2',
                      'OHITUKSET_5MIN_KIINTEA_SUUNTA1',
                      'OHITUKSET_5MIN_KIINTEA_SUUNTA2' )
    AND NOT EXISTS(
        SELECT null
        FROM allowed_road_station_sensor a
        where a.natural_id = s.natural_id
          AND a.road_station_type = s.road_station_type
    );

ALTER TABLE ALLOVED_TMS_SENSOR_CONSTANT
RENAME TO ALLOWED_TMS_SENSOR_CONSTANT;