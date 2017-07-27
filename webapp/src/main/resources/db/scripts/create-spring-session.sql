CREATE SCHEMA IF NOT EXISTS "web";

CREATE TABLE web.spring_session (
  session_id character(36) NOT NULL,
  creation_time bigint NOT NULL,
  last_access_time bigint NOT NULL,
  max_inactive_interval integer NOT NULL,
  principal_name character varying(100),
  CONSTRAINT spring_session_pk PRIMARY KEY (session_id)
);

CREATE INDEX web.spring_session_ix1 
    ON web.spring_session USING btree(last_access_time);

CREATE TABLE web.spring_session_attributes
(
  session_id character(36) NOT NULL,
  attribute_name character varying(200) NOT NULL,
  attribute_bytes bytea NOT NULL,
  CONSTRAINT spring_session_attributes_pk PRIMARY KEY (session_id, attribute_name),
  CONSTRAINT spring_session_attributes_fk FOREIGN KEY (session_id) REFERENCES web.spring_session (session_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE INDEX web.spring_session_attributes_ix1 
    ON web.spring_session_attributes USING btree(session_id);

