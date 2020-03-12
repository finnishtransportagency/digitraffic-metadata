DROP INDEX road_station_ui;
CREATE UNIQUE INDEX road_station_ui
    ON road_station
        USING BTREE (natural_id ASC, road_station_type) where obsolete_date is null;