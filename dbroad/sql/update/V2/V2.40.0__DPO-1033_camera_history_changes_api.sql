-- Delete over 7 d history
DELETE FROM camera_preset_history
WHERE last_modified < now() - interval '168 hour';