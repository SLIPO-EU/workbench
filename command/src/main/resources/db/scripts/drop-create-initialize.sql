
\set db_name "slipo-workbench"
\set owner "slipo"


--
-- Drop, create and initialize target database
--


DROP DATABASE IF EXISTS :db_name;
CREATE DATABASE :db_name OWNER :owner; 

\c :db_name :owner
\i ./initialize.sql
