
--
-- Table: process
--

CREATE SEQUENCE public.process_id_seq INCREMENT 1 MINVALUE 1 START 1 CACHE 1;

CREATE TABLE process
(
  id bigint NOT NULL DEFAULT nextval('public.process_id_seq'::regclass),
  version bigint NOT NULL DEFAULT 1, -- initialized to 1, incremented every time a new version is created
  row_version bigint DEFAULT 1, -- used by hibernate for optimistic locking
  name character varying(80) NOT NULL,
  description character varying(200),
  created_by integer NOT NULL,
  updated_by integer NOT NULL,
  created_on timestamp with time zone,
  updated_on timestamp with time zone,
  executed_on timestamp with time zone, -- most recent execution timestamp
  definition character varying NOT NULL, -- definition as an object serialized in JSON format
  is_template boolean NOT NULL, -- a template is a recipe for a process whose configuration is only partially saved
  task_type character varying(80) NOT NULL,
  CONSTRAINT pk_process PRIMARY KEY (id),
  CONSTRAINT fk_account_create FOREIGN KEY (created_by)
      REFERENCES account (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_account_update FOREIGN KEY (updated_by)
      REFERENCES account (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

ALTER TABLE process OWNER TO slipo;

COMMENT ON COLUMN process.version IS 'initialized to 1, incremented every time a new version is created';
COMMENT ON COLUMN process.row_version IS 'used by hibernate for optimistic locking';
COMMENT ON COLUMN process.executed_on IS 'most recent execution timestamp';
COMMENT ON COLUMN process.definition IS 'definition as an object serialized in JSON format';
COMMENT ON COLUMN process.is_template IS 'a template is a recipe for a process whose configuration is only partially saved';

--
-- Table: process_revision
--

CREATE SEQUENCE public.process_revision_id_seq INCREMENT 1 MINVALUE 1 START 1 CACHE 1;

CREATE TABLE process_revision
(
  id bigint NOT NULL DEFAULT nextval('public.process_revision_id_seq'::regclass),
  parent bigint NOT NULL, -- refer to parent process
  version bigint NOT NULL,
  name character varying(80) NOT NULL,
  description character varying(200),
  updated_by integer NOT NULL,
  updated_on timestamp with time zone,
  executed_on timestamp with time zone,
  definition character varying NOT NULL,
  CONSTRAINT pk_process_revision PRIMARY KEY (id),
  CONSTRAINT fk_account_update FOREIGN KEY (updated_by)
      REFERENCES account (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_process FOREIGN KEY (parent)
      REFERENCES process (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT uq_process_parent_id_version UNIQUE (parent, version)
)
WITH (
  OIDS=FALSE
);

ALTER TABLE process_revision OWNER TO slipo;

COMMENT ON COLUMN process_revision.parent IS 'refer to parent process';

--
-- Table: process_execution
--

CREATE SEQUENCE public.process_execution_id_seq INCREMENT 1 MINVALUE 1 START 1 CACHE 1;

CREATE TABLE process_execution
(
  id bigint NOT NULL DEFAULT nextval('public.process_execution_id_seq'::regclass),
  process bigint NOT NULL,
  submitted_by integer,
  submitted_on timestamp with time zone,
  started_on timestamp with time zone NOT NULL,
  completed_on timestamp with time zone,
  status character varying(20) NOT NULL, -- the overall status; one of "RUNNING", "STOPPED", "FAILED", "COMPLETED"
  error_message character varying, -- root error that has caused the execution to fail
  CONSTRAINT pk_process_execution PRIMARY KEY (id),
  CONSTRAINT fk_process FOREIGN KEY (process)
      REFERENCES process_revision (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);

ALTER TABLE process_execution OWNER TO slipo;

COMMENT ON COLUMN process_execution.status IS 'the overall status; one of "RUNNING", "STOPPED", "FAILED", "COMPLETED"';
COMMENT ON COLUMN process_execution.error_message IS 'root error that has caused the execution to fail';

--
-- Table: resource
--


CREATE SEQUENCE public.resource_id_seq INCREMENT 1 MINVALUE 1 START 1 CACHE 1;

CREATE TABLE resource
(
  id bigint NOT NULL DEFAULT nextval('resource_id_seq'::regclass),
  version bigint NOT NULL, -- initialized to 1 and incremented every time a resource is updated
  row_version bigint DEFAULT 1, -- used by Hibernate for optimistic locking
  type character varying(20) NOT NULL, -- one of "poi-data", "linked-data"
  source_type character varying(20) NOT NULL, -- one of "harvester", "upload", "filesystem", "external-url", "computed"
  input_format character varying(20) NOT NULL,
  output_format character varying(20) NOT NULL,
  process_execution bigint, -- the execution that created this resource
  name character varying(50) NOT NULL, -- a user-friendly name
  description character varying(200),
  created_on timestamp with time zone,
  created_by integer NOT NULL,
  updated_on timestamp with time zone,
  updated_by integer NOT NULL,
  bbox geometry,
  number_of_entities integer, -- number of POIs for POI data; number of links for linked data
  file_path character varying(256) NOT NULL, -- file path relative to NFS root share
  file_size bigint, -- file size in bytes
  table_name uuid, -- reference to PostGIS table with vector data (applicable only for POI data)
  CONSTRAINT resource_pkey PRIMARY KEY (id),
  CONSTRAINT resource_created_by_fkey FOREIGN KEY (created_by)
      REFERENCES account (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT resource_process_execution_fkey FOREIGN KEY (process_execution)
      REFERENCES process_execution (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT resource_updated_by_fkey FOREIGN KEY (updated_by)
      REFERENCES account (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT enforce_dims_bbox CHECK (st_ndims(bbox) = 2),
  CONSTRAINT enforce_srid_bbox CHECK (st_srid(bbox) = 4326)
)
WITH (
  OIDS=FALSE
);

ALTER TABLE resource OWNER TO slipo;

COMMENT ON COLUMN resource.version IS 'initialized to 1 and incremented every time a resource is updated';
COMMENT ON COLUMN resource.row_version IS 'used by Hibernate for optimistic locking';
COMMENT ON COLUMN resource.type IS 'one of "poi-data", "linked-data"';
COMMENT ON COLUMN resource.source_type IS 'one of "harvester", "upload", "filesystem", "external-url", "computed"';
COMMENT ON COLUMN resource.process_execution IS 'the execution that created this resource';
COMMENT ON COLUMN resource.name IS 'a user-friendly name';
COMMENT ON COLUMN resource.number_of_entities IS 'number of POIs for POI data; number of links for linked data';
COMMENT ON COLUMN resource.file_path IS 'file path relative to NFS root share';
COMMENT ON COLUMN resource.file_size IS 'file size in bytes';
COMMENT ON COLUMN resource.table_name IS 'reference to PostGIS table with vector data (applicable only for POI data)';

--
-- Table: resource_revision
--

CREATE SEQUENCE public.resource_revision_id_seq INCREMENT 1 MINVALUE 1 START 1 CACHE 1;

CREATE TABLE resource_revision
(
  id bigint NOT NULL DEFAULT nextval('resource_revision_id_seq'::regclass),
  parent bigint NOT NULL, -- refers to parent resource
  version bigint NOT NULL,
  type character varying(20) NOT NULL,
  source_type character varying(20) NOT NULL,
  input_format character varying(20) NOT NULL,
  output_format character varying(20) NOT NULL,
  process_execution bigint,
  name character varying(50) NOT NULL,
  description character varying(200),
  updated_on timestamp with time zone,
  updated_by integer NOT NULL,
  bbox geometry,
  number_of_entities integer,
  file_path character varying(256) NOT NULL,
  file_size bigint NOT NULL,
  table_name uuid,
  CONSTRAINT resource_revision_pkey PRIMARY KEY (id),
  CONSTRAINT resource_revision_parent_id_fkey FOREIGN KEY (parent)
      REFERENCES resource (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT resource_revision_process_execution_fkey FOREIGN KEY (process_execution)
      REFERENCES process_execution (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT resource_revision_updated_by_fkey FOREIGN KEY (updated_by)
      REFERENCES account (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT uq_resource_parent_id_version UNIQUE (parent, version),
  CONSTRAINT enforce_dims_bbox CHECK (st_ndims(bbox) = 2),
  CONSTRAINT enforce_srid_bbox CHECK (st_srid(bbox) = 4326)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE resource_revision OWNER TO slipo;

COMMENT ON COLUMN resource_revision.parent IS 'refers to parent resource';


--
-- Table: process_execution_step
--

CREATE SEQUENCE public.process_execution_step_id_seq INCREMENT 1 MINVALUE 1 START 1 CACHE 1;

CREATE TABLE process_execution_step
(
  id bigint NOT NULL DEFAULT nextval('process_execution_step_id_seq'::regclass),
  job_execution bigint, -- the underlying job execution
  process_execution bigint NOT NULL,
  step_key integer NOT NULL, -- step key as provided by the process designer and stored in process configuration
  step_name character varying(40) NOT NULL,
  tool_name character varying(40) NOT NULL, -- name of the SLIPO Toolkit Component that represents this step
  operation character varying(40), -- name of the operation carried out e.g. INTERLINK
  started_on timestamp with time zone,
  completed_on timestamp with time zone,
  status character varying(20) NOT NULL, -- the status of underlying job execution, e.g. RUNNING, STOPPED, FAILED, COMPLETED etc
  error_message character varying, -- root error that has caused this step to fail
  CONSTRAINT process_execution_step_pkey PRIMARY KEY (id),
  CONSTRAINT process_execution_step_process_execution_fkey FOREIGN KEY (process_execution)
      REFERENCES process_execution (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE process_execution_step OWNER TO slipo;

COMMENT ON COLUMN process_execution_step.job_execution IS 'the underlying job execution';
COMMENT ON COLUMN process_execution_step.step_key IS 'step key as provided by the process designer and stored in process configuration';
COMMENT ON COLUMN process_execution_step.tool_name IS 'name of the SLIPO Toolkit Component that represents this step';
COMMENT ON COLUMN process_execution_step.operation IS 'name of the operation carried out e.g. INTERLINK';
COMMENT ON COLUMN process_execution_step.status IS 'the status of underlying job execution, e.g. RUNNING, STOPPED, FAILED, COMPLETED etc';
COMMENT ON COLUMN process_execution_step.error_message IS 'root error that has caused this step to fail';

--
-- Table: process_execution_step_file
--

CREATE SEQUENCE public.process_execution_step_file_id_seq INCREMENT 1 MINVALUE 1 START 1 CACHE 1;

CREATE TABLE process_execution_step_file
(
  id bigint NOT NULL DEFAULT nextval('process_execution_step_file_id_seq'::regclass),
  process_execution_step bigint NOT NULL,
  type character varying(14) NOT NULL, -- one of "configuration", "input", "output", "sample", "kpi", "qa"
  file_path character varying(256) NOT NULL, -- file path relative to root of NFS share
  file_size bigint NOT NULL, -- file size in bytes
  table_name uuid, -- refer to table with with vector data (if any)
  "resource" bigint, -- resource id, if the file refers to a registered resource (e.g. an input file)
  CONSTRAINT process_execution_step_file_pkey PRIMARY KEY (id),
  CONSTRAINT process_execution_step_file_process_execution_step_id_fkey FOREIGN KEY (process_execution_step)
      REFERENCES process_execution_step (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT process_execution_step_file_resource_id_fkey FOREIGN KEY ("resource")
      REFERENCES resource_revision (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE SET NULL
)
WITH (
  OIDS=FALSE
);

ALTER TABLE process_execution_step_file OWNER TO slipo;

COMMENT ON TABLE process_execution_step_file
  IS 'Represent every input/output of a (logical) step operation. This includes actual input files, configuration files, output results and any generated sample/QA/KPI data.';
COMMENT ON COLUMN process_execution_step_file.type IS 'one of "configuration", "input", "output", "sample", "kpi", "qa"';
COMMENT ON COLUMN process_execution_step_file.file_path IS 'file path relative to root of NFS share';
COMMENT ON COLUMN process_execution_step_file.file_size IS 'file size in bytes';
COMMENT ON COLUMN process_execution_step_file.table_name IS 'refer to table with with vector data (if any)';
COMMENT ON COLUMN process_execution_step_file.resource IS 'resource id, if the file refers to a registered resource (e.g. an input file)';

