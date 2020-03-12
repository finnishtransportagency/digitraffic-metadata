ALTER TABLE maintenance_realization
    ADD COLUMN start_time   TIMESTAMP(0) WITH TIME ZONE,
    ADD COLUMN end_time     TIMESTAMP(0) WITH TIME ZONE;

UPDATE maintenance_realization
set start_time = p.start_time,
    end_time = p.end_time
FROM (
    select max(p.time) start_time, min(p.time) end_time, p.realization_id
    from maintenance_realization_point p
    group by p.realization_id
) p
WHERE maintenance_realization.id = p.realization_id;

ALTER TABLE maintenance_realization
    ALTER COLUMN start_time SET NOT NULL,
    ALTER COLUMN end_time SET NOT NULL;

DROP TABLE MAINTENANCE_REALIZATION_POINT;
