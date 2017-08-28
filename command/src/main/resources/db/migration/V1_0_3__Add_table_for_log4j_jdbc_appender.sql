--
-- Drop per-application log4j tables
--

DROP SEQUENCE IF EXISTS rpc.log4j_message_id_seq CASCADE;
DROP TABLE IF EXISTS rpc.log4j_message;

DROP SEQUENCE IF EXISTS web.log4j_message_id_seq CASCADE;
DROP TABLE IF EXISTS web.log4j_message;


--
-- Create a single table for JDBC Log4j appender for all applications 
-- 

CREATE SEQUENCE public.log4j_message_id_seq
  INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 128;

CREATE TABLE public.log4j_message
(
  "id" bigint PRIMARY KEY DEFAULT nextval('public.log4j_message_id_seq'::regclass) NOT NULL,
  "application" character varying(64) NOT NULL,
  "generated" timestamp without time zone,
  "level" character varying(12),
  "message" text,
  "throwable" text,
  "logger" character varying(256),
  "client_address" character varying(16),
  "username" character varying(64)
);
