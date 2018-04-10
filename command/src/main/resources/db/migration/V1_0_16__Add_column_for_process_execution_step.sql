ALTER TABLE public.process_execution_step ADD COLUMN node_name character varying(80);

COMMENT ON COLUMN public.process_execution_step.node_name
  IS 'The workflow-friendly name of a step';

UPDATE public.process_execution_step SET node_name = step_name ;  

ALTER TABLE public.process_execution_step ALTER COLUMN node_name SET NOT NULL;

