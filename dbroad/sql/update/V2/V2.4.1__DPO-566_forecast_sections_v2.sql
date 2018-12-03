CREATE TABLE IF NOT EXISTS forecast_section_coordinate_list (
  forecast_section_id                 NUMERIC(10),
  forecast_section_coordinate_id      BIGINT,
  order_number                        INTEGER
);

CREATE SEQUENCE IF NOT EXISTS seq_forecast_section_coordinate INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS forecast_section_coordinate (
  id              BIGINT,
  order_number    INTEGER,
  longitude       NUMERIC(6,3),
  latitude        NUMERIC(6,3)
);

ALTER TABLE forecast_section_coordinate ADD CONSTRAINT forecast_section_coordinate_pk PRIMARY KEY (id);

ALTER TABLE forecast_section_coordinate_list ADD CONSTRAINT forsec_coord_list_pk PRIMARY KEY(forecast_section_id, forecast_section_coordinate_id);

ALTER TABLE forecast_section_coordinate_list
  ADD CONSTRAINT foresec_coord_list_fk FOREIGN KEY (forecast_section_id)
REFERENCES forecast_section (id)
ON DELETE NO ACTION;

ALTER TABLE forecast_section_coordinate_list
  ADD CONSTRAINT foresec_coord_list_coord_fk FOREIGN KEY (forecast_section_coordinate_id)
REFERENCES forecast_section_coordinate (id)
ON DELETE NO ACTION;