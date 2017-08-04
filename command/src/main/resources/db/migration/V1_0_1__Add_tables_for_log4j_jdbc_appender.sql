
CREATE SEQUENCE web.log4j_message_id_seq
  INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 128;

CREATE TABLE web.log4j_message
(
  "id" bigint PRIMARY KEY DEFAULT nextval('web.log4j_message_id_seq'::regclass) NOT NULL,
  "generated" timestamp without time zone,
  "level" character varying(12),
  "message" text,
  "throwable" text,
  "logger" character varying(256),
  "client_address" character varying(16),
  "username" character varying(64)
);

--GRANT USAGE ON SCHEMA "web" TO log4j;
--GRANT ALL ON TABLE web.log4j_message TO log4j;
--GRANT ALL ON SEQUENCE web.log4j_message_id_seq TO log4j;

CREATE SEQUENCE rpc.log4j_message_id_seq
  INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 128;

CREATE TABLE rpc.log4j_message
(
  "id" bigint PRIMARY KEY DEFAULT nextval('rpc.log4j_message_id_seq'::regclass) NOT NULL,
  "generated" timestamp without time zone,
  "level" character varying(12),
  "message" text,
  "throwable" text,
  "logger" character varying(256)
);

--GRANT USAGE ON SCHEMA "rpc" TO log4j;
--GRANT ALL ON TABLE rpc.log4j_message TO log4j;
--GRANT ALL ON SEQUENCE rpc.log4j_message_id_seq TO log4j;
