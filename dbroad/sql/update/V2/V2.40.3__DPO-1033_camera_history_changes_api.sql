-- replace old columns with new ones
ALTER TABLE camera_preset_history
    DROP COLUMN created,
    DROP COLUMN last_modified;
ALTER TABLE camera_preset_history
    RENAME COLUMN created_tmp TO created;
ALTER TABLE camera_preset_history
    RENAME COLUMN last_modified_tmp TO last_modified;
-- not null constraints to all
ALTER TABLE camera_preset_history
    ALTER COLUMN preset_seq SET NOT NULL,
    ALTER COLUMN created SET NOT NULL,
    ALTER COLUMN modified SET NOT NULL,
    ALTER COLUMN last_modified SET NOT NULL;

-- Function to generate new sequence number on every insert per preset history
CREATE OR REPLACE FUNCTION update_preset_seq_column()
    RETURNS TRIGGER AS $$
DECLARE
    _preset_seq          BIGINT;
BEGIN
    SELECT MAX(preset_seq) + 1 INTO _preset_seq FROM camera_preset_history WHERE preset_id = NEW.preset_id;
    NEW.preset_seq := coalesce(_preset_seq, 1);
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Automatic modified update
DROP TRIGGER IF EXISTS camera_preset_history_modified_trigger on camera_preset_history;
CREATE TRIGGER camera_preset_history_modified_trigger BEFORE UPDATE ON camera_preset_history FOR EACH ROW EXECUTE PROCEDURE update_modified_column();
-- Automatic preset sequence increment
DROP TRIGGER IF EXISTS camera_preset_history_preset_seq_trigger on camera_preset_history;
CREATE TRIGGER camera_preset_history_preset_seq_trigger BEFORE INSERT ON camera_preset_history FOR EACH ROW EXECUTE PROCEDURE update_preset_seq_column();

CREATE UNIQUE INDEX camera_preset_history_preset_id_preset_seq_ui ON camera_preset_history USING BTREE (preset_id, preset_seq);
CREATE INDEX camera_preset_history_modified_i ON camera_preset_history USING BTREE (modified, preset_id);
