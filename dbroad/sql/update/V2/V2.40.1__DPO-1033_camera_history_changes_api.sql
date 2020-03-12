-- add some new columns
ALTER TABLE camera_preset_history
    ADD COLUMN IF NOT EXISTS preset_seq BIGINT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS created_tmp TIMESTAMPTZ(0) DEFAULT now(),
    ADD COLUMN IF NOT EXISTS modified TIMESTAMPTZ(0) DEFAULT now(),
    ADD COLUMN IF NOT EXISTS last_modified_tmp TIMESTAMPTZ(0) DEFAULT now();
