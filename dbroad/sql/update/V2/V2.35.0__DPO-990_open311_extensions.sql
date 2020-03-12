ALTER TABLE open311_service_request ADD COLUMN status_id TEXT;
ALTER TABLE open311_service_request ADD COLUMN title TEXT;
ALTER TABLE open311_service_request ADD COLUMN service_object_type TEXT;
ALTER TABLE open311_service_request ADD COLUMN service_object_id TEXT;
ALTER TABLE open311_service_request ADD COLUMN media_urls TEXT[];

CREATE TABLE open311_service_request_state (
   key TEXT NOT NULL PRIMARY KEY,
   name TEXT NOT NULL
);
