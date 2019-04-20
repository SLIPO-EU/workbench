CREATE SEQUENCE public.process_execution_step_logs_id_seq;

CREATE TABLE public.process_execution_step_logs (
    "id" bigint PRIMARY KEY DEFAULT nextval('process_execution_step_logs_id_seq'::regclass),
    "process_execution_step" bigint NOT NULL,
    "name" varchar(255) NOT NULL,
    "file_path" varchar(1023) NOT NULL,
    CONSTRAINT process_execution_step_logs_process_execution_step_id_fkey FOREIGN KEY ("process_execution_step")
       REFERENCES public.process_execution_step ("id") MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT uq_process_execution_step_logs_step_and_name UNIQUE ("process_execution_step", "name")
);

COMMENT ON COLUMN public.process_execution_step_logs."name" IS 'name of the corresponding Batch step';
