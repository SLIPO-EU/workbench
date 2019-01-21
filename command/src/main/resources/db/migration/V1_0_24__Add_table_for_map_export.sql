DROP TABLE IF EXISTS public.process_execution_map_export;

DROP SEQUENCE IF EXISTS public.process_execution_map_export_id_seq;

CREATE SEQUENCE public.process_execution_map_export_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 37
  CACHE 1;


CREATE TABLE public.process_execution_map_export (
  id bigint            NOT NULL DEFAULT nextval('process_execution_map_export_id_seq'::regclass),
  execution_workflow   bigint NOT NULL,
  execution_transform  bigint NULL,
  created_on           timestamp with time zone NOT NULL,
  created_by           integer NOT NULL,
  started_on           timestamp with time zone NULL,
  completed_on         timestamp with time zone NULL,
  status               character varying(20) NOT NULL,
  CONSTRAINT process_execution_map_export_pkey PRIMARY KEY (id),
  CONSTRAINT execution_workflow_fkey FOREIGN KEY (execution_workflow)
      REFERENCES public.process_execution (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT execution_transform_fkey FOREIGN KEY (execution_transform)
      REFERENCES public.process_execution (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT created_by_fkey FOREIGN KEY (created_by)
      REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
