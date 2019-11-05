DROP TABLE IF EXISTS public.process_draft;

DROP SEQUENCE IF EXISTS public.process_draft_seq;

CREATE SEQUENCE public.process_draft_seq
  INCREMENT 1
  MINVALUE 1;
  
CREATE TABLE public.process_draft
(
  sid         bigint                   NOT NULL DEFAULT nextval('process_draft_seq'::regclass),
  owner       integer                  NOT NULL,
  id          bigint                   NOT NULL,
  row_version bigint                   NOt NULL  DEFAULT 1, -- used by hibernate for optimistic locking
  is_template boolean                  NOT NULL,
  updated_on  timestamp with time zone NOT NULL,
  definition  character varying        NOT NULL,
  CONSTRAINT process_draft_pkey PRIMARY KEY (sid),
  CONSTRAINT process_draft_owner_fkey FOREIGN KEY (owner)
      REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uq_process_draft_owner_id UNIQUE (owner, id)
)
WITH (
  OIDS=FALSE
);
