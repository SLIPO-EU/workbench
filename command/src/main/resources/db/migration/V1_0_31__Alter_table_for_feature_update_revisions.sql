DROP TABLE IF EXISTS public.feature_update_history;

DROP SEQUENCE IF EXISTS public.feature_update_history_seq;

CREATE SEQUENCE public.feature_update_history_seq
  INCREMENT 1
  MINVALUE 1;

CREATE TABLE public.feature_update_history
(
  id bigint NOT NULL DEFAULT nextval('feature_update_history_seq'::regclass),
  table_name uuid NOT NULL,
  feature_id bigint NOT NULL,
  properties character varying,
  the_geom geometry,
  the_geom_simple geometry,
  updated_on timestamp with time zone NOT NULL,
  updated_by integer NOT NULL,
  CONSTRAINT feature_update_history_pkey PRIMARY KEY (id),
  CONSTRAINT feature_update_history_updated_by_fkey FOREIGN KEY (updated_by)
      REFERENCES public.account (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
