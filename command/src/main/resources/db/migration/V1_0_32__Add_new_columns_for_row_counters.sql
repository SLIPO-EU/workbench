ALTER TABLE public.resource
   ADD COLUMN row_count bigint;
   
ALTER TABLE public.resource_revision
   ADD COLUMN row_count bigint;
   
ALTER TABLE public.process_execution_step_file
   ADD COLUMN row_count bigint;