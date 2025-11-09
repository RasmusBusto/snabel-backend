-- Create database and user for Snabel Accounting
CREATE DATABASE snabel_accounting;
CREATE USER snabel WITH PASSWORD 'snabel';
GRANT ALL PRIVILEGES ON DATABASE snabel_accounting TO snabel;

-- Connect to the database and grant schema permissions
\c snabel_accounting
GRANT ALL ON SCHEMA public TO snabel;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO snabel;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO snabel;
