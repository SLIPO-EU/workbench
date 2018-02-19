ALTER TABLE public.process_execution 
    ALTER COLUMN started_on DROP NOT NULL;

ALTER TABLE public.process_execution_step
    ADD CONSTRAINT uq_process_execution_step_execution_and_key UNIQUE (process_execution, step_key);


ALTER TABLE public.process_execution_step_file
    ADD CONSTRAINT uq_process_execution_step_file_step_and_path UNIQUE (process_execution_step, file_path);
