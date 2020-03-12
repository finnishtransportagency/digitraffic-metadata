UPDATE camera_preset_history tgt
SET preset_seq = (
        SELECT preset_seq
        FROM (
                 SELECT row_number() OVER (PARTITION BY h.preset_id ORDER BY h.last_modified) preset_seq, h.preset_id, h.last_modified
                 FROM camera_preset_history h
        ) src
        WHERE src.preset_id = tgt.preset_id
          AND src.last_modified = tgt.last_modified),
    created_tmp = created,
    modified = last_modified,
    last_modified_tmp = last_modified;