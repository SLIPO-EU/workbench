CREATE TABLE public.workflow
(
  id uuid NOT NULL,
  process bigint NOT NULL,
  CONSTRAINT workflow_pkey PRIMARY KEY (id),
  CONSTRAINT fk_workflow_process FOREIGN KEY (process)
      REFERENCES public.process_revision (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uq_workflow_process UNIQUE (process)
);
