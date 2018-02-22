ALTER TABLE public.process_execution_step_file
   ADD COLUMN data_format character varying(31);

ALTER TABLE public.process_execution_step_file
   ADD COLUMN bbox geometry;

ALTER TABLE public.process_execution_step_file
   ADD COLUMN table_name uuid;
