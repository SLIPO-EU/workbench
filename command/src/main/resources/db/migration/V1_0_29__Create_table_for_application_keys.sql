DROP TABLE IF EXISTS public.application_key;

DROP SEQUENCE IF EXISTS public.application_key_id_seq;

CREATE SEQUENCE public.application_key_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 37
  CACHE 1;


CREATE TABLE public.application_key (
  id                            bigint                   NOT NULL DEFAULT nextval('application_key_id_seq'::regclass),
  application_name              character varying        NOT NULL,
  application_key               character varying        NOT NULL,
  created_on                    timestamp with time zone NOT NULL,
  created_by                    integer                  NOT NULL,
  revoked_on                    timestamp with time zone     NULL,
  revoked_by                    integer                      NULL,
  mapped_account                integer                  NOT NULL,
  max_daily_request_limit       integer                  NOT NULL,
  max_concurrent_request_limit  integer                  NOT NULL,
  CONSTRAINT application_key_pkey PRIMARY KEY (id),
  CONSTRAINT created_by_fkey FOREIGN KEY (created_by)
      REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT revoked_by_fkey FOREIGN KEY (revoked_by)
      REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT mapped_account_fkey FOREIGN KEY (mapped_account)
      REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uq_application_name UNIQUE (application_name)
)
WITH (
  OIDS=FALSE
);
