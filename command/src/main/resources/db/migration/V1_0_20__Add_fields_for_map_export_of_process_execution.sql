ALTER TABLE public.process_execution ADD COLUMN exported_on timestamp with time zone;
ALTER TABLE public.process_execution ADD COLUMN exported_by integer;

ALTER TABLE public.process_execution ADD CONSTRAINT process_execution_exported_by_fkey FOREIGN KEY (exported_by)
  REFERENCES public.account (id) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION;
