-- reindex after every db update
reindex table camera_preset;
reindex table camera_preset_history;
reindex table data_updated;
reindex table datex2;
reindex table datex2_situation;
reindex table datex2_situation_record;
reindex table device_data;
reindex table forecast_condition_reason;
reindex table forecast_section_coordinate_list;
reindex table forecast_section_weather;
reindex table forecast_section;
reindex table link_id;
reindex table locking_table;
reindex table maintenance_tracking;
reindex table maintenance_tracking_data;
reindex table maintenance_tracking_task;
reindex table road_segment;
reindex table road_station;
reindex table sensor_value;
reindex table sensor_value_history;

-- reindex qrtz_tables
reindex table qrtz_fired_triggers;
reindex table qrtz_simple_triggers;
reindex table qrtz_triggers;

