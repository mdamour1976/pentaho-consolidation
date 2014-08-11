--
-- note: this script assumes pg_hba.conf is configured correctly
--

-- \connect postgres postgres

DROP DATABASE IF EXISTS hibernate;
DROP USER IF EXISTS hibuser;

CREATE USER hibuser PASSWORD 'password';

CREATE DATABASE hibernate WITH OWNER = hibuser ENCODING = 'UTF8' TABLESPACE = pg_default;

GRANT ALL PRIVILEGES ON DATABASE hibernate TO hibuser;
