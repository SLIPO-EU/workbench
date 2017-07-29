
-- Create schema for objects specific to the web application
CREATE SCHEMA IF NOT EXISTS "web"; 

-- Create schema for objects specific to the rpc server
CREATE SCHEMA IF NOT EXISTS "rpc";

--
-- Create "public".* objects
--

CREATE SEQUENCE public.account_id_seq INCREMENT 1 MINVALUE 1 START 1 CACHE 1;

CREATE TABLE account
(
  id integer NOT NULL DEFAULT nextval('account_id_seq'::regclass),
  "username" character varying(32) NOT NULL,
  "active" boolean,
  "blocked" boolean,
  "email" character varying(64) NOT NULL,
  "family_name" character varying(64),
  "given_name" character varying(64),
  "lang" character varying(2),
  "password" character varying(64),
  "registered_at" timestamp DEFAULT now(),
  CONSTRAINT account_pkey PRIMARY KEY (id),
  CONSTRAINT uq_account_email UNIQUE ("email"),
  CONSTRAINT uq_account_username UNIQUE ("username")
);


CREATE INDEX account_username_ix1 
    ON account USING btree  (username COLLATE pg_catalog."default");

CREATE SEQUENCE public.account_role_id_seq INCREMENT 1 MINVALUE 1 START 1 CACHE 1;

CREATE TABLE account_role
(
  id integer NOT NULL DEFAULT nextval('account_role_id_seq'::regclass),
  "role" character varying(64) NOT NULL,
  "account" integer NOT NULL,
  "granted_at" timestamp DEFAULT now(),
  "granted_by" integer,
  CONSTRAINT account_role_pkey PRIMARY KEY (id),
  CONSTRAINT fk_account_role_account FOREIGN KEY ("account")
      REFERENCES account (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_account_role_granted_by FOREIGN KEY ("granted_by")
      REFERENCES account (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uq_account_role UNIQUE ("account", "role")
); 
