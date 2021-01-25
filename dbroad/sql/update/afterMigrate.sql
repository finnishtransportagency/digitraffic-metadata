-- REINDEX is similar to a drop and recreate of the index in that the index
-- contents are rebuilt from scratch.
-- Indexes will become fragmented and unoptimized after some time, especially
-- if the ROWS IN THE TABLE ARE OFTEN UPDATED OR DELETED. In those cases it may be
-- required to perform a REINDEX leaving you with a balanced and optimized index.
-- https://www.postgresql.org/docs/9.4/sql-reindex.html
-- https://devcenter.heroku.com/articles/postgresql-indexes

-- reindex after every db update
reindex (VERBOSE) table camera_preset;
reindex (VERBOSE) table camera_preset_history;
reindex (VERBOSE) table data_updated;
reindex (VERBOSE) table datex2;
reindex (VERBOSE) table datex2_situation;
reindex (VERBOSE) table datex2_situation_record;
reindex (VERBOSE) table device_data;
reindex (VERBOSE) table forecast_condition_reason;
reindex (VERBOSE) table forecast_section_coordinate_list;
reindex (VERBOSE) table forecast_section_weather;
reindex (VERBOSE) table forecast_section;
reindex (VERBOSE) table link_id;
reindex (VERBOSE) table locking_table;
--reindex (VERBOSE) table maintenance_tracking;
--reindex (VERBOSE) table maintenance_tracking_data;
reindex (VERBOSE) table maintenance_tracking_task;
reindex (VERBOSE) table road_segment;
reindex (VERBOSE) table road_station;
reindex (VERBOSE) table sensor_value;
reindex (VERBOSE) table sensor_value_history;

-- reindex qrtz_tables
reindex (VERBOSE) table qrtz_fired_triggers;
reindex (VERBOSE) table qrtz_simple_triggers;
reindex (VERBOSE) table qrtz_triggers;

