DROP TABLE IF EXISTS public.process_execution_api;

DROP SEQUENCE IF EXISTS public.process_execution_api_seq;

CREATE SEQUENCE public.process_execution_api_seq
  INCREMENT 1
  MINVALUE 1;

  
CREATE TABLE public.process_execution_api (
  id                      bigint                    NOT NULL DEFAULT nextval('process_execution_api_seq'::regclass),
  process_execution       bigint                    NOT NULL,
  application_key         bigint                    NOT NULL,
  operation               character varying(20)     NOT NULL,
  CONSTRAINT pk_process_execution_api PRIMARY KEY (id),
  CONSTRAINT fk_application_key FOREIGN KEY (application_key)
      REFERENCES public.application_key (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_process_execution FOREIGN KEY (process_execution)
      REFERENCES public.process_execution (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);
