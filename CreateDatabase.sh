#!/bin/sh

sleep 20

echo "Running configuration script..."

db_check=$(/opt/mssql-tools/bin/sqlcmd -I -S localhost -U SA -P $MSSQL_SA_PASSWORD -d master -Q "IF DB_ID('MalopolskaMap') IS NULL PRINT '0' ELSE PRINT '1';" | tr -d '\r' | tail -n 1)

if [ "$db_check" = "1" ]; then
	echo "Skipping. Database already exists."
else
	echo "MalopolskaMap database does not exist."

	/opt/mssql-tools/bin/sqlcmd -I -S localhost -U SA -P $MSSQL_SA_PASSWORD -d master -i /MalopolskaMap_TSQL/CreateDatabase.sql
	/opt/mssql-tools/bin/sqlcmd -I -S localhost -U SA -P $MSSQL_SA_PASSWORD -d master -i /MalopolskaMap_TSQL/CreateTables.sql
	/opt/mssql-tools/bin/sqlcmd -I -S localhost -U SA -P $MSSQL_SA_PASSWORD -d master -i /MalopolskaMap_TSQL/Functions.sql
	/opt/mssql-tools/bin/sqlcmd -I -S localhost -U SA -P $MSSQL_SA_PASSWORD -d master -i /MalopolskaMap_TSQL/LoadMaps.sql
fi

echo "Configuration complete."
