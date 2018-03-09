ALTER TABLE public.process_execution_step_file
   ADD COLUMN verified boolean;

COMMENT ON COLUMN public.process_execution_step_file.verified
   IS 'A flag to indicate if an expected output file is verified';

