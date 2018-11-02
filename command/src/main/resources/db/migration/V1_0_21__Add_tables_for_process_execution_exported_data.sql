DROP TABLE IF EXISTS public.process_execution_table;

DROP SEQUENCE IF EXISTS public.process_execution_table_id_seq;

CREATE SEQUENCE public.process_execution_table_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 37
  CACHE 1;


CREATE TABLE public.process_execution_table (
  id bigint NOT NULL DEFAULT nextval('process_execution_table_id_seq'::regclass),
  process_execution bigint NOT NULL,
  type character varying(20) NOT NULL,
  output_key int NOT NULL,
  created_on timestamp with time zone,
  table_name uuid,
  CONSTRAINT process_execution_table_pkey PRIMARY KEY (id),
  CONSTRAINT process_execution_fkey FOREIGN KEY (process_execution)
      REFERENCES public.process_execution (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT uq_process_execution_and_output_key UNIQUE (process_execution, output_key)
)
WITH (
  OIDS=FALSE
);

COMMENT ON COLUMN public.process_execution_table.process_execution IS 'The id of the parent process execution.';
COMMENT ON COLUMN public.process_execution_table.type IS '"POI_DATA" for TripleGeo output. "LINKED_DATA" for LIMES output.';
COMMENT ON COLUMN public.process_execution_table.output_key IS 'The unique output key.';
COMMENT ON COLUMN public.process_execution_table.table_name IS 'Table with vector data for POI_DATA data. Table with link pairs for LINKED_DATA data.';
