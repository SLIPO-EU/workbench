
ALTER TABLE public.process_execution_step_file 
    ADD COLUMN output_part character varying(128);

COMMENT ON COLUMN public.process_execution_step_file.output_part 
    IS 'The key of the output part this file corresponds to';

