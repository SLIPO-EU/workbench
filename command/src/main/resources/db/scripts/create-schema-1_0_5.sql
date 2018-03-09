--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: rpc; Type: SCHEMA; Schema: -; Owner: slipo
--

CREATE SCHEMA rpc;


ALTER SCHEMA rpc OWNER TO slipo;

--
-- Name: topology; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA topology;


ALTER SCHEMA topology OWNER TO postgres;

--
-- Name: web; Type: SCHEMA; Schema: -; Owner: slipo
--

CREATE SCHEMA web;


ALTER SCHEMA web OWNER TO slipo;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';


--
-- Name: postgis_topology; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS postgis_topology WITH SCHEMA topology;


--
-- Name: EXTENSION postgis_topology; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis_topology IS 'PostGIS topology spatial types and functions';


--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


SET search_path = public, pg_catalog;

--
-- Name: account_id_seq; Type: SEQUENCE; Schema: public; Owner: slipo
--

CREATE SEQUENCE account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE account_id_seq OWNER TO slipo;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: account; Type: TABLE; Schema: public; Owner: slipo; Tablespace: 
--

CREATE TABLE account (
    id integer DEFAULT nextval('account_id_seq'::regclass) NOT NULL,
    username character varying(32) NOT NULL,
    active boolean,
    blocked boolean,
    email character varying(64) NOT NULL,
    family_name character varying(64),
    given_name character varying(64),
    lang character varying(2),
    password character varying(64),
    registered_at timestamp without time zone DEFAULT now()
);


ALTER TABLE account OWNER TO slipo;

--
-- Name: account_role_id_seq; Type: SEQUENCE; Schema: public; Owner: slipo
--

CREATE SEQUENCE account_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE account_role_id_seq OWNER TO slipo;

--
-- Name: account_role; Type: TABLE; Schema: public; Owner: slipo; Tablespace: 
--

CREATE TABLE account_role (
    id integer DEFAULT nextval('account_role_id_seq'::regclass) NOT NULL,
    role character varying(64) NOT NULL,
    account integer NOT NULL,
    granted_at timestamp without time zone DEFAULT now(),
    granted_by integer
);


ALTER TABLE account_role OWNER TO slipo;

--
-- Name: db_version; Type: TABLE; Schema: public; Owner: slipo; Tablespace: 
--

CREATE TABLE db_version (
    version_rank integer NOT NULL,
    installed_rank integer NOT NULL,
    version character varying(50) NOT NULL,
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE db_version OWNER TO slipo;

--
-- Name: log4j_message_id_seq; Type: SEQUENCE; Schema: public; Owner: slipo
--

CREATE SEQUENCE log4j_message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 128;


ALTER TABLE log4j_message_id_seq OWNER TO slipo;

--
-- Name: log4j_message; Type: TABLE; Schema: public; Owner: slipo; Tablespace: 
--

CREATE TABLE log4j_message (
    id bigint DEFAULT nextval('log4j_message_id_seq'::regclass) NOT NULL,
    application character varying(64) NOT NULL,
    generated timestamp without time zone,
    level character varying(12),
    message text,
    throwable text,
    logger character varying(256),
    client_address character varying(16),
    username character varying(64)
);


ALTER TABLE log4j_message OWNER TO slipo;

--
-- Name: process_id_seq; Type: SEQUENCE; Schema: public; Owner: slipo
--

CREATE SEQUENCE process_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE process_id_seq OWNER TO slipo;

--
-- Name: process; Type: TABLE; Schema: public; Owner: slipo; Tablespace: 
--

CREATE TABLE process (
    id bigint DEFAULT nextval('process_id_seq'::regclass) NOT NULL,
    version bigint DEFAULT 1 NOT NULL,
    row_version bigint DEFAULT 1,
    name character varying(80) NOT NULL,
    description character varying(200),
    created_by integer NOT NULL,
    updated_by integer NOT NULL,
    created_on timestamp with time zone,
    updated_on timestamp with time zone,
    executed_on timestamp with time zone,
    definition character varying NOT NULL,
    is_template boolean NOT NULL,
    task_type character varying(80) NOT NULL
);


ALTER TABLE process OWNER TO slipo;

--
-- Name: COLUMN process.version; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process.version IS 'initialized to 1, incremented every time a new version is created';


--
-- Name: COLUMN process.row_version; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process.row_version IS 'used by hibernate for optimistic locking';


--
-- Name: COLUMN process.executed_on; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process.executed_on IS 'most recent execution timestamp';


--
-- Name: COLUMN process.definition; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process.definition IS 'definition as an object serialized in JSON format';


--
-- Name: COLUMN process.is_template; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process.is_template IS 'a template is a recipe for a process whose configuration is only partially saved';


--
-- Name: process_execution_id_seq; Type: SEQUENCE; Schema: public; Owner: slipo
--

CREATE SEQUENCE process_execution_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE process_execution_id_seq OWNER TO slipo;

--
-- Name: process_execution; Type: TABLE; Schema: public; Owner: slipo; Tablespace: 
--

CREATE TABLE process_execution (
    id bigint DEFAULT nextval('process_execution_id_seq'::regclass) NOT NULL,
    process bigint NOT NULL,
    submitted_by integer,
    submitted_on timestamp with time zone,
    started_on timestamp with time zone NOT NULL,
    completed_on timestamp with time zone,
    status character varying(20) NOT NULL,
    error_message character varying
);


ALTER TABLE process_execution OWNER TO slipo;

--
-- Name: COLUMN process_execution.status; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process_execution.status IS 'the overall status; one of "RUNNING", "STOPPED", "FAILED", "COMPLETED"';


--
-- Name: COLUMN process_execution.error_message; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process_execution.error_message IS 'root error that has caused the execution to fail';


--
-- Name: process_execution_step_id_seq; Type: SEQUENCE; Schema: public; Owner: slipo
--

CREATE SEQUENCE process_execution_step_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE process_execution_step_id_seq OWNER TO slipo;

--
-- Name: process_execution_step; Type: TABLE; Schema: public; Owner: slipo; Tablespace: 
--

CREATE TABLE process_execution_step (
    id bigint DEFAULT nextval('process_execution_step_id_seq'::regclass) NOT NULL,
    job_execution bigint,
    process_execution bigint NOT NULL,
    step_key integer NOT NULL,
    step_name character varying(40) NOT NULL,
    tool_name character varying(40) NOT NULL,
    operation character varying(40),
    started_on timestamp with time zone,
    completed_on timestamp with time zone,
    status character varying(20) NOT NULL,
    error_message character varying
);


ALTER TABLE process_execution_step OWNER TO slipo;

--
-- Name: COLUMN process_execution_step.job_execution; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process_execution_step.job_execution IS 'the underlying job execution';


--
-- Name: COLUMN process_execution_step.step_key; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process_execution_step.step_key IS 'step key as provided by the process designer and stored in process configuration';


--
-- Name: COLUMN process_execution_step.tool_name; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process_execution_step.tool_name IS 'name of the SLIPO Toolkit Component that represents this step';


--
-- Name: COLUMN process_execution_step.operation; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process_execution_step.operation IS 'name of the operation carried out e.g. INTERLINK';


--
-- Name: COLUMN process_execution_step.status; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process_execution_step.status IS 'the status of underlying job execution, e.g. RUNNING, STOPPED, FAILED, COMPLETED etc';


--
-- Name: COLUMN process_execution_step.error_message; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process_execution_step.error_message IS 'root error that has caused this step to fail';


--
-- Name: process_execution_step_file_id_seq; Type: SEQUENCE; Schema: public; Owner: slipo
--

CREATE SEQUENCE process_execution_step_file_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE process_execution_step_file_id_seq OWNER TO slipo;

--
-- Name: process_execution_step_file; Type: TABLE; Schema: public; Owner: slipo; Tablespace: 
--

CREATE TABLE process_execution_step_file (
    id bigint DEFAULT nextval('process_execution_step_file_id_seq'::regclass) NOT NULL,
    process_execution_step bigint NOT NULL,
    type character varying(14) NOT NULL,
    file_path character varying(256) NOT NULL,
    file_size bigint NOT NULL,
    table_name uuid,
    resource bigint
);


ALTER TABLE process_execution_step_file OWNER TO slipo;

--
-- Name: TABLE process_execution_step_file; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON TABLE process_execution_step_file IS 'Represent every input/output of a (logical) step operation. This includes actual input files, configuration files, output results and any generated sample/QA/KPI data.';


--
-- Name: COLUMN process_execution_step_file.type; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process_execution_step_file.type IS 'one of "configuration", "input", "output", "sample", "kpi", "qa"';


--
-- Name: COLUMN process_execution_step_file.file_path; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process_execution_step_file.file_path IS 'file path relative to root of NFS share';


--
-- Name: COLUMN process_execution_step_file.file_size; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process_execution_step_file.file_size IS 'file size in bytes';


--
-- Name: COLUMN process_execution_step_file.table_name; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process_execution_step_file.table_name IS 'refer to table with with vector data (if any)';


--
-- Name: COLUMN process_execution_step_file.resource; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process_execution_step_file.resource IS 'resource id, if the file refers to a registered resource (e.g. an input file)';


--
-- Name: process_revision_id_seq; Type: SEQUENCE; Schema: public; Owner: slipo
--

CREATE SEQUENCE process_revision_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE process_revision_id_seq OWNER TO slipo;

--
-- Name: process_revision; Type: TABLE; Schema: public; Owner: slipo; Tablespace: 
--

CREATE TABLE process_revision (
    id bigint DEFAULT nextval('process_revision_id_seq'::regclass) NOT NULL,
    parent bigint NOT NULL,
    version bigint NOT NULL,
    name character varying(80) NOT NULL,
    description character varying(200),
    updated_by integer NOT NULL,
    updated_on timestamp with time zone,
    executed_on timestamp with time zone,
    definition character varying NOT NULL
);


ALTER TABLE process_revision OWNER TO slipo;

--
-- Name: COLUMN process_revision.parent; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN process_revision.parent IS 'refer to parent process';


--
-- Name: resource_id_seq; Type: SEQUENCE; Schema: public; Owner: slipo
--

CREATE SEQUENCE resource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE resource_id_seq OWNER TO slipo;

--
-- Name: resource; Type: TABLE; Schema: public; Owner: slipo; Tablespace: 
--

CREATE TABLE resource (
    id bigint DEFAULT nextval('resource_id_seq'::regclass) NOT NULL,
    version bigint NOT NULL,
    row_version bigint DEFAULT 1,
    type character varying(20) NOT NULL,
    source_type character varying(20) NOT NULL,
    input_format character varying(20) NOT NULL,
    output_format character varying(20) NOT NULL,
    process_execution bigint,
    name character varying(50) NOT NULL,
    description character varying(200),
    created_on timestamp with time zone,
    created_by integer NOT NULL,
    updated_on timestamp with time zone,
    updated_by integer NOT NULL,
    bbox geometry,
    number_of_entities integer,
    file_path character varying(256) NOT NULL,
    file_size bigint,
    table_name uuid,
    CONSTRAINT enforce_dims_bbox CHECK ((st_ndims(bbox) = 2)),
    CONSTRAINT enforce_srid_bbox CHECK ((st_srid(bbox) = 4326))
);


ALTER TABLE resource OWNER TO slipo;

--
-- Name: COLUMN resource.version; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN resource.version IS 'initialized to 1 and incremented every time a resource is updated';


--
-- Name: COLUMN resource.row_version; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN resource.row_version IS 'used by Hibernate for optimistic locking';


--
-- Name: COLUMN resource.type; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN resource.type IS 'one of "poi-data", "linked-data"';


--
-- Name: COLUMN resource.source_type; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN resource.source_type IS 'one of "harvester", "upload", "filesystem", "external-url", "computed"';


--
-- Name: COLUMN resource.process_execution; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN resource.process_execution IS 'the execution that created this resource';


--
-- Name: COLUMN resource.name; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN resource.name IS 'a user-friendly name';


--
-- Name: COLUMN resource.number_of_entities; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN resource.number_of_entities IS 'number of POIs for POI data; number of links for linked data';


--
-- Name: COLUMN resource.file_path; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN resource.file_path IS 'file path relative to NFS root share';


--
-- Name: COLUMN resource.file_size; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN resource.file_size IS 'file size in bytes';


--
-- Name: COLUMN resource.table_name; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN resource.table_name IS 'reference to PostGIS table with vector data (applicable only for POI data)';


--
-- Name: resource_revision_id_seq; Type: SEQUENCE; Schema: public; Owner: slipo
--

CREATE SEQUENCE resource_revision_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE resource_revision_id_seq OWNER TO slipo;

--
-- Name: resource_revision; Type: TABLE; Schema: public; Owner: slipo; Tablespace: 
--

CREATE TABLE resource_revision (
    id bigint DEFAULT nextval('resource_revision_id_seq'::regclass) NOT NULL,
    parent bigint NOT NULL,
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
    CONSTRAINT enforce_dims_bbox CHECK ((st_ndims(bbox) = 2)),
    CONSTRAINT enforce_srid_bbox CHECK ((st_srid(bbox) = 4326))
);


ALTER TABLE resource_revision OWNER TO slipo;

--
-- Name: COLUMN resource_revision.parent; Type: COMMENT; Schema: public; Owner: slipo
--

COMMENT ON COLUMN resource_revision.parent IS 'refers to parent resource';


SET search_path = rpc, pg_catalog;

--
-- Name: batch_job_execution; Type: TABLE; Schema: rpc; Owner: slipo; Tablespace: 
--

CREATE TABLE batch_job_execution (
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
    job_configuration_location character varying(2500)
);


ALTER TABLE batch_job_execution OWNER TO slipo;

--
-- Name: batch_job_execution_context; Type: TABLE; Schema: rpc; Owner: slipo; Tablespace: 
--

CREATE TABLE batch_job_execution_context (
    job_execution_id bigint NOT NULL,
    short_context character varying(2500) NOT NULL,
    serialized_context text
);


ALTER TABLE batch_job_execution_context OWNER TO slipo;

--
-- Name: batch_job_execution_params; Type: TABLE; Schema: rpc; Owner: slipo; Tablespace: 
--

CREATE TABLE batch_job_execution_params (
    job_execution_id bigint NOT NULL,
    type_cd character varying(6) NOT NULL,
    key_name character varying(128) NOT NULL,
    string_val character varying(32768),
    date_val timestamp without time zone,
    long_val bigint,
    double_val double precision,
    identifying character(1) NOT NULL
);


ALTER TABLE batch_job_execution_params OWNER TO slipo;

--
-- Name: batch_job_execution_seq; Type: SEQUENCE; Schema: rpc; Owner: slipo
--

CREATE SEQUENCE batch_job_execution_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE batch_job_execution_seq OWNER TO slipo;

--
-- Name: batch_job_instance; Type: TABLE; Schema: rpc; Owner: slipo; Tablespace: 
--

CREATE TABLE batch_job_instance (
    job_instance_id bigint NOT NULL,
    version bigint,
    job_name character varying(100) NOT NULL,
    job_key character varying(32) NOT NULL
);


ALTER TABLE batch_job_instance OWNER TO slipo;

--
-- Name: batch_job_seq; Type: SEQUENCE; Schema: rpc; Owner: slipo
--

CREATE SEQUENCE batch_job_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE batch_job_seq OWNER TO slipo;

--
-- Name: batch_step_execution; Type: TABLE; Schema: rpc; Owner: slipo; Tablespace: 
--

CREATE TABLE batch_step_execution (
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
    last_updated timestamp without time zone
);


ALTER TABLE batch_step_execution OWNER TO slipo;

--
-- Name: batch_step_execution_context; Type: TABLE; Schema: rpc; Owner: slipo; Tablespace: 
--

CREATE TABLE batch_step_execution_context (
    step_execution_id bigint NOT NULL,
    short_context character varying(2500) NOT NULL,
    serialized_context text
);


ALTER TABLE batch_step_execution_context OWNER TO slipo;

--
-- Name: batch_step_execution_seq; Type: SEQUENCE; Schema: rpc; Owner: slipo
--

CREATE SEQUENCE batch_step_execution_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE batch_step_execution_seq OWNER TO slipo;

--
-- Name: job_parameter_id_seq; Type: SEQUENCE; Schema: rpc; Owner: slipo
--

CREATE SEQUENCE job_parameter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE job_parameter_id_seq OWNER TO slipo;

--
-- Name: job_parameter; Type: TABLE; Schema: rpc; Owner: slipo; Tablespace: 
--

CREATE TABLE job_parameter (
    id integer DEFAULT nextval('job_parameter_id_seq'::regclass) NOT NULL,
    job_name character varying(128) NOT NULL,
    name character varying(128) NOT NULL,
    type character varying(32),
    default_expression character varying(255),
    default_value character varying(255),
    identifying boolean NOT NULL,
    required boolean NOT NULL,
    CONSTRAINT check_job_parameter_default_expression CHECK (((default_expression IS NULL) OR (default_value IS NULL))),
    CONSTRAINT check_job_parameter_type CHECK (((type IS NULL) OR ((type)::text = ANY ((ARRAY['STRING'::character varying, 'DOUBLE'::character varying, 'LONG'::character varying, 'DATE'::character varying])::text[]))))
);


ALTER TABLE job_parameter OWNER TO slipo;

SET search_path = web, pg_catalog;

--
-- Name: spring_session; Type: TABLE; Schema: web; Owner: slipo; Tablespace: 
--

CREATE TABLE spring_session (
    session_id character(36) NOT NULL,
    creation_time bigint NOT NULL,
    last_access_time bigint NOT NULL,
    max_inactive_interval integer NOT NULL,
    principal_name character varying(100)
);


ALTER TABLE spring_session OWNER TO slipo;

--
-- Name: spring_session_attributes; Type: TABLE; Schema: web; Owner: slipo; Tablespace: 
--

CREATE TABLE spring_session_attributes (
    session_id character(36) NOT NULL,
    attribute_name character varying(200) NOT NULL,
    attribute_bytes bytea NOT NULL
);


ALTER TABLE spring_session_attributes OWNER TO slipo;

SET search_path = public, pg_catalog;

--
-- Name: account_pkey; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY account
    ADD CONSTRAINT account_pkey PRIMARY KEY (id);


--
-- Name: account_role_pkey; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY account_role
    ADD CONSTRAINT account_role_pkey PRIMARY KEY (id);


--
-- Name: db_version_pk; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY db_version
    ADD CONSTRAINT db_version_pk PRIMARY KEY (version);


--
-- Name: log4j_message_pkey; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY log4j_message
    ADD CONSTRAINT log4j_message_pkey PRIMARY KEY (id);


--
-- Name: pk_process; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY process
    ADD CONSTRAINT pk_process PRIMARY KEY (id);


--
-- Name: pk_process_execution; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY process_execution
    ADD CONSTRAINT pk_process_execution PRIMARY KEY (id);


--
-- Name: pk_process_revision; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY process_revision
    ADD CONSTRAINT pk_process_revision PRIMARY KEY (id);


--
-- Name: process_execution_step_file_pkey; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY process_execution_step_file
    ADD CONSTRAINT process_execution_step_file_pkey PRIMARY KEY (id);


--
-- Name: process_execution_step_pkey; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY process_execution_step
    ADD CONSTRAINT process_execution_step_pkey PRIMARY KEY (id);


--
-- Name: resource_pkey; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_pkey PRIMARY KEY (id);


--
-- Name: resource_revision_pkey; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY resource_revision
    ADD CONSTRAINT resource_revision_pkey PRIMARY KEY (id);


--
-- Name: uq_account_email; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY account
    ADD CONSTRAINT uq_account_email UNIQUE (email);


--
-- Name: uq_account_role; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY account_role
    ADD CONSTRAINT uq_account_role UNIQUE (account, role);


--
-- Name: uq_account_username; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY account
    ADD CONSTRAINT uq_account_username UNIQUE (username);


--
-- Name: uq_process_parent_id_version; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY process_revision
    ADD CONSTRAINT uq_process_parent_id_version UNIQUE (parent, version);


--
-- Name: uq_resource_parent_id_version; Type: CONSTRAINT; Schema: public; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY resource_revision
    ADD CONSTRAINT uq_resource_parent_id_version UNIQUE (parent, version);


SET search_path = rpc, pg_catalog;

--
-- Name: batch_job_execution_context_pkey; Type: CONSTRAINT; Schema: rpc; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY batch_job_execution_context
    ADD CONSTRAINT batch_job_execution_context_pkey PRIMARY KEY (job_execution_id);


--
-- Name: batch_job_execution_pkey; Type: CONSTRAINT; Schema: rpc; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY batch_job_execution
    ADD CONSTRAINT batch_job_execution_pkey PRIMARY KEY (job_execution_id);


--
-- Name: batch_job_instance_pkey; Type: CONSTRAINT; Schema: rpc; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY batch_job_instance
    ADD CONSTRAINT batch_job_instance_pkey PRIMARY KEY (job_instance_id);


--
-- Name: batch_step_execution_context_pkey; Type: CONSTRAINT; Schema: rpc; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY batch_step_execution_context
    ADD CONSTRAINT batch_step_execution_context_pkey PRIMARY KEY (step_execution_id);


--
-- Name: batch_step_execution_pkey; Type: CONSTRAINT; Schema: rpc; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY batch_step_execution
    ADD CONSTRAINT batch_step_execution_pkey PRIMARY KEY (step_execution_id);


--
-- Name: job_inst_un; Type: CONSTRAINT; Schema: rpc; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY batch_job_instance
    ADD CONSTRAINT job_inst_un UNIQUE (job_name, job_key);


--
-- Name: job_parameter_pkey; Type: CONSTRAINT; Schema: rpc; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY job_parameter
    ADD CONSTRAINT job_parameter_pkey PRIMARY KEY (id);


--
-- Name: unique_job_parameter_name; Type: CONSTRAINT; Schema: rpc; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY job_parameter
    ADD CONSTRAINT unique_job_parameter_name UNIQUE (job_name, name);


SET search_path = web, pg_catalog;

--
-- Name: spring_session_attributes_pk; Type: CONSTRAINT; Schema: web; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY spring_session_attributes
    ADD CONSTRAINT spring_session_attributes_pk PRIMARY KEY (session_id, attribute_name);


--
-- Name: spring_session_pk; Type: CONSTRAINT; Schema: web; Owner: slipo; Tablespace: 
--

ALTER TABLE ONLY spring_session
    ADD CONSTRAINT spring_session_pk PRIMARY KEY (session_id);


SET search_path = public, pg_catalog;

--
-- Name: account_username_ix1; Type: INDEX; Schema: public; Owner: slipo; Tablespace: 
--

CREATE INDEX account_username_ix1 ON account USING btree (username);


--
-- Name: db_version_ir_idx; Type: INDEX; Schema: public; Owner: slipo; Tablespace: 
--

CREATE INDEX db_version_ir_idx ON db_version USING btree (installed_rank);


--
-- Name: db_version_s_idx; Type: INDEX; Schema: public; Owner: slipo; Tablespace: 
--

CREATE INDEX db_version_s_idx ON db_version USING btree (success);


--
-- Name: db_version_vr_idx; Type: INDEX; Schema: public; Owner: slipo; Tablespace: 
--

CREATE INDEX db_version_vr_idx ON db_version USING btree (version_rank);


--
-- Name: fk_account_create; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY process
    ADD CONSTRAINT fk_account_create FOREIGN KEY (created_by) REFERENCES account(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_account_role_account; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY account_role
    ADD CONSTRAINT fk_account_role_account FOREIGN KEY (account) REFERENCES account(id);


--
-- Name: fk_account_role_granted_by; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY account_role
    ADD CONSTRAINT fk_account_role_granted_by FOREIGN KEY (granted_by) REFERENCES account(id);


--
-- Name: fk_account_update; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY process
    ADD CONSTRAINT fk_account_update FOREIGN KEY (updated_by) REFERENCES account(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_account_update; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY process_revision
    ADD CONSTRAINT fk_account_update FOREIGN KEY (updated_by) REFERENCES account(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_process; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY process_revision
    ADD CONSTRAINT fk_process FOREIGN KEY (parent) REFERENCES process(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: fk_process; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY process_execution
    ADD CONSTRAINT fk_process FOREIGN KEY (process) REFERENCES process_revision(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: process_execution_step_file_process_execution_step_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY process_execution_step_file
    ADD CONSTRAINT process_execution_step_file_process_execution_step_id_fkey FOREIGN KEY (process_execution_step) REFERENCES process_execution_step(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: process_execution_step_file_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY process_execution_step_file
    ADD CONSTRAINT process_execution_step_file_resource_id_fkey FOREIGN KEY (resource) REFERENCES resource_revision(id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: process_execution_step_process_execution_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY process_execution_step
    ADD CONSTRAINT process_execution_step_process_execution_fkey FOREIGN KEY (process_execution) REFERENCES process_execution(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resource_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_created_by_fkey FOREIGN KEY (created_by) REFERENCES account(id);


--
-- Name: resource_process_execution_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_process_execution_fkey FOREIGN KEY (process_execution) REFERENCES process_execution(id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: resource_revision_parent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY resource_revision
    ADD CONSTRAINT resource_revision_parent_id_fkey FOREIGN KEY (parent) REFERENCES resource(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: resource_revision_process_execution_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY resource_revision
    ADD CONSTRAINT resource_revision_process_execution_fkey FOREIGN KEY (process_execution) REFERENCES process_execution(id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: resource_revision_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY resource_revision
    ADD CONSTRAINT resource_revision_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES account(id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: resource_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: slipo
--

ALTER TABLE ONLY resource
    ADD CONSTRAINT resource_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES account(id);


SET search_path = rpc, pg_catalog;

--
-- Name: job_exec_ctx_fk; Type: FK CONSTRAINT; Schema: rpc; Owner: slipo
--

ALTER TABLE ONLY batch_job_execution_context
    ADD CONSTRAINT job_exec_ctx_fk FOREIGN KEY (job_execution_id) REFERENCES batch_job_execution(job_execution_id);


--
-- Name: job_exec_params_fk; Type: FK CONSTRAINT; Schema: rpc; Owner: slipo
--

ALTER TABLE ONLY batch_job_execution_params
    ADD CONSTRAINT job_exec_params_fk FOREIGN KEY (job_execution_id) REFERENCES batch_job_execution(job_execution_id);


--
-- Name: job_exec_step_fk; Type: FK CONSTRAINT; Schema: rpc; Owner: slipo
--

ALTER TABLE ONLY batch_step_execution
    ADD CONSTRAINT job_exec_step_fk FOREIGN KEY (job_execution_id) REFERENCES batch_job_execution(job_execution_id);


--
-- Name: job_inst_exec_fk; Type: FK CONSTRAINT; Schema: rpc; Owner: slipo
--

ALTER TABLE ONLY batch_job_execution
    ADD CONSTRAINT job_inst_exec_fk FOREIGN KEY (job_instance_id) REFERENCES batch_job_instance(job_instance_id);


--
-- Name: step_exec_ctx_fk; Type: FK CONSTRAINT; Schema: rpc; Owner: slipo
--

ALTER TABLE ONLY batch_step_execution_context
    ADD CONSTRAINT step_exec_ctx_fk FOREIGN KEY (step_execution_id) REFERENCES batch_step_execution(step_execution_id);


SET search_path = web, pg_catalog;

--
-- Name: spring_session_attributes_fk; Type: FK CONSTRAINT; Schema: web; Owner: slipo
--

ALTER TABLE ONLY spring_session_attributes
    ADD CONSTRAINT spring_session_attributes_fk FOREIGN KEY (session_id) REFERENCES spring_session(session_id) ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: rpc; Type: ACL; Schema: -; Owner: slipo
--

REVOKE ALL ON SCHEMA rpc FROM PUBLIC;
REVOKE ALL ON SCHEMA rpc FROM slipo;
GRANT ALL ON SCHEMA rpc TO slipo;
GRANT USAGE ON SCHEMA rpc TO log4j;


--
-- Name: web; Type: ACL; Schema: -; Owner: slipo
--

REVOKE ALL ON SCHEMA web FROM PUBLIC;
REVOKE ALL ON SCHEMA web FROM slipo;
GRANT ALL ON SCHEMA web TO slipo;
GRANT USAGE ON SCHEMA web TO log4j;


--
-- PostgreSQL database dump complete
--

