--
-- Create tables/sequences for Spring-Batch repository.
-- Note: This only differs from default script included in Spring-Batch in that
-- it creates objects into a non-default ("rpc") schema (i.e namespace).
--

CREATE SCHEMA IF NOT EXISTS "rpc";


CREATE TABLE rpc.batch_job_instance
(
  job_instance_id bigint NOT NULL,
  version bigint,
  job_name character varying(100) NOT NULL,
  job_key character varying(32) NOT NULL,
  CONSTRAINT batch_job_instance_pkey PRIMARY KEY (job_instance_id),
  CONSTRAINT job_inst_un UNIQUE (job_name, job_key)
);

CREATE TABLE rpc.batch_job_execution
(
  job_execution_id bigint NOT NULL,
  version bigint,
  job_instance_id bigint NOT NULL,
  create_time timestamp without time zone NOT NULL,
  start_time timestamp without time zone,
  end_time timestamp without time zone,
  status character varying(10),
  exit_code character varying(2500),
  exit_message character varying(4096),
  last_updated timestamp without time zone,
  job_configuration_location character varying(2500),
  CONSTRAINT batch_job_execution_pkey PRIMARY KEY (job_execution_id),
  CONSTRAINT job_inst_exec_fk FOREIGN KEY (job_instance_id)
      REFERENCES rpc.batch_job_instance (job_instance_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE rpc.batch_job_execution_params
(
  job_execution_id bigint NOT NULL,
  type_cd character varying(6) NOT NULL,
  key_name character varying(128) NOT NULL,
  string_val character varying(32768),
  date_val timestamp without time zone,
  long_val bigint,
  double_val double precision,
  identifying character(1) NOT NULL,
  CONSTRAINT job_exec_params_fk FOREIGN KEY (job_execution_id)
      REFERENCES rpc.batch_job_execution (job_execution_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);


CREATE TABLE rpc.batch_step_execution
(
  step_execution_id bigint NOT NULL,
  version bigint NOT NULL,
  step_name character varying(100) NOT NULL,
  job_execution_id bigint NOT NULL,
  start_time timestamp without time zone NOT NULL,
  end_time timestamp without time zone,
  status character varying(10),
  commit_count bigint,
  read_count bigint,
  filter_count bigint,
  write_count bigint,
  read_skip_count bigint,
  write_skip_count bigint,
  process_skip_count bigint,
  rollback_count bigint,
  exit_code character varying(2500),
  exit_message character varying(2500),
  last_updated timestamp without time zone,
  CONSTRAINT batch_step_execution_pkey PRIMARY KEY (step_execution_id),
  CONSTRAINT job_exec_step_fk FOREIGN KEY (job_execution_id)
      REFERENCES rpc.batch_job_execution (job_execution_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE rpc.batch_step_execution_context
(
  step_execution_id bigint NOT NULL,
  short_context character varying(2500) NOT NULL,
  serialized_context text,
  CONSTRAINT batch_step_execution_context_pkey PRIMARY KEY (step_execution_id),
  CONSTRAINT step_exec_ctx_fk FOREIGN KEY (step_execution_id)
      REFERENCES rpc.batch_step_execution (step_execution_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE rpc.batch_job_execution_context
(
  job_execution_id bigint NOT NULL,
  short_context character varying(2500) NOT NULL,
  serialized_context text,
  CONSTRAINT batch_job_execution_context_pkey PRIMARY KEY (job_execution_id),
  CONSTRAINT job_exec_ctx_fk FOREIGN KEY (job_execution_id)
      REFERENCES rpc.batch_job_execution (job_execution_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);


CREATE SEQUENCE rpc.batch_step_execution_seq MAXVALUE 9223372036854775807 NO CYCLE;

CREATE SEQUENCE rpc.batch_job_execution_seq MAXVALUE 9223372036854775807 NO CYCLE;

CREATE SEQUENCE rpc.batch_job_seq MAXVALUE 9223372036854775807 NO CYCLE;

