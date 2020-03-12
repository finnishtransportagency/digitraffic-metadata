UPDATE datex2
set message_type = 'TRAFFIC_INCIDENT'
where message_type = 'TRAFFIC_DISORDER';

ALTER TABLE datex2
ADD CONSTRAINT datex2_type_check CHECK (message_type IN ('TRAFFIC_INCIDENT', 'ROADWORK', 'WEIGHT_RESTRICTION'));
