ALTER TABLE WORK_MACHINE_OBSERVATION
    DROP CONSTRAINT work_machine_observation_work_machine_id_fkey,
    ADD CONSTRAINT work_machine_observation_work_machine_id_fkey
        FOREIGN KEY (WORK_MACHINE_ID)
            REFERENCES work_machine(id)
            ON DELETE CASCADE;

ALTER TABLE WORK_MACHINE_OBSERVATION_COORDINATE
    DROP CONSTRAINT work_machine_observation_coord_work_machine_observation_id_fkey,
    ADD CONSTRAINT work_machine_observation_coord_work_machine_observation_id_fkey
        FOREIGN KEY (work_machine_observation_id)
            REFERENCES WORK_MACHINE_OBSERVATION(id)
            ON DELETE CASCADE;

ALTER TABLE WORK_MACHINE_TASK
    DROP CONSTRAINT work_machine_task_work_machine_coordinate_observation_id_fkey,
    ADD CONSTRAINT work_machine_task_work_machine_coordinate_observation_id_fkey
        FOREIGN KEY (WORK_MACHINE_COORDINATE_OBSERVATION_ID, WORK_MACHINE_COORDINATE_ORDER_NUMBER)
            REFERENCES WORK_MACHINE_OBSERVATION_COORDINATE (WORK_MACHINE_OBSERVATION_ID, ORDER_NUMBER)
            ON DELETE CASCADE;
