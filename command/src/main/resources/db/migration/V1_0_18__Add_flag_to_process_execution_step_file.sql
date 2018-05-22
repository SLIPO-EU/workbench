ALTER TABLE public.process_execution_step_file
   ADD COLUMN primary_output boolean;
COMMENT ON COLUMN public.process_execution_step_file.primary_output
  IS 'A flag that marks an output file as the primary output of the processing step it belongs.';

