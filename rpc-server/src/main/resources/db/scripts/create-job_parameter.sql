CREATE SEQUENCE rpc.job_parameter_id_seq
  INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1;
  
--ALTER TABLE rpc.job_parameter_id_seq OWNER TO slipo;
 
CREATE TABLE rpc.job_parameter
(
  "id" integer NOT NULL DEFAULT nextval('rpc.job_parameter_id_seq'::regclass),
  "job_name" character varying(128) NOT NULL,
  "name" character varying(128) NOT NULL,
  "type" character varying(32),
  "default_expression" character varying(255),
  "default_value" character varying(255),
  "identifying" boolean NOT NULL,
  "required" boolean NOT NULL,
  
  CONSTRAINT job_parameter_pkey PRIMARY KEY (id),
  CONSTRAINT unique_job_parameter_name UNIQUE (job_name, name),
  CONSTRAINT check_job_parameter_default_expression CHECK (
      "default_expression" is NULL OR "default_value" is NULL),
  CONSTRAINT check_job_parameter_type CHECK (
      "type" is NULL OR "type" in ('STRING', 'DOUBLE', 'LONG', 'DATE'))
);

--ALTER TABLE rpc.job_parameter OWNER TO slipo;