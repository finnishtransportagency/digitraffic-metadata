create table IF NOT EXISTS nw2_annotation (
    id          text primary key,
    created_at  timestamp(0) with time zone not null,
    recorded_at  timestamp(0) with time zone not null,
    expires_at  timestamp(0) with time zone not null,
    type        text not null,
    location    GEOMETRY not null
);

ALTER TABLE nw2_annotation ADD CONSTRAINT nw2_annotation_type_check CHECK (type IN ('badWeather', 'slipperyRoad', 'visibilityReduced',
'slowVehicle', 'stationaryTraffic', 'animalsOnTheRoad', 'objectOnTheRoad', 'unprotectedAccidentArea', 'roadworks'));

insert into data_updated(id, data_type, updated, version)
values(nextval('seq_data_updated'), 'NW2_ANNOTATIONS', current_date, null);

