#!/bin/sh

# Calls SQLCMD to verify that system and user databases return "0" which means all databases are in an "online" state,
# then run the configuration script (setup.sql)
# https://docs.microsoft.com/en-us/sql/relational-databases/system-catalog-views/sys-databases-transact-sql?view=sql-server-2017 

# Run the setup script to create the DB and the schema in the DB

./scripts/CreateDatabase.sh &

/opt/mssql/bin/sqlservr