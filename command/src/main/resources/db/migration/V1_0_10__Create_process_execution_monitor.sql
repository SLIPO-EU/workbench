CREATE TABLE public.process_execution_monitor
(
  process bigint NOT NULL,
  modified_on timestamp without time zone NOT NULL,
  row_version bigint,
  CONSTRAINT process_execution_monitor_pkey PRIMARY KEY (process),
  CONSTRAINT fk_process_execution_monitor_1 FOREIGN KEY (process) REFERENCES public.process_revision (id) 
    MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
