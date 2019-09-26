-- reindex after every db update
reindex table camera_preset;
reindex table camera_preset_history;
reindex table data_updated;
reindex table datex2;
reindex table datex2_situation;
reindex table datex2_situation_record;
reindex table forecast_condition_reason;
reindex table forecast_section_coordinate_list;
reindex table forecast_section_weather;
reindex table link_id;
reindex table locking_table;
reindex table road_segment;
reindex table road_station;

-- reindex qrtz_tables
reindex table qrtz_fired_triggers;
reindex table qrtz_simple_triggers;
reindex table qrtz_triggers;
