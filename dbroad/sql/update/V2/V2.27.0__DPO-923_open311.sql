CREATE TABLE open311_service (
    service_code CHARACTER VARYING(3) PRIMARY KEY, -- e.g. 199
    service_name TEXT NOT NULL,
    description TEXT NOT NULL,
    metadata BOOLEAN NOT NULL,
    type CHARACTER VARYING(8) NOT NULL,
    keywords TEXT,
    "group" TEXT
);
ALTER TABLE open311_service ADD CONSTRAINT open311_service_service_type_check CHECK (type IN ('realtime', 'batch', 'blackbox'));

CREATE TABLE open311_service_request (
    service_request_id CHARACTER VARYING(13) PRIMARY KEY, -- e.g. SRQ0000001083
    status TEXT NOT NULL,
    status_notes TEXT,
    service_name TEXT,
    service_code TEXT,
    description TEXT NOT NULL,
    agency_responsible TEXT,
    service_notice TEXT,
    requested_datetime TIMESTAMP(0) WITH TIME ZONE NOT NULL,
    updated_datetime TIMESTAMP(0) WITH TIME ZONE,
    expected_datetime TIMESTAMP(0) WITH TIME ZONE,
    address TEXT,
    address_id TEXT,
    zipcode TEXT,
    geometry GEOMETRY,
    media_url TEXT
);
ALTER TABLE open311_service_request ADD CONSTRAINT open311_service_request_status_check CHECK (status IN ('open', 'closed'));
