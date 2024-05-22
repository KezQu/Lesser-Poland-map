#!/bin/sh

sleep 10

echo "Running configuration script..."

echo "MalopolskaMap database id: "
/opt/mssql-tools/bin/sqlcmd -I -S localhost -U SA -P $MSSQL_SA_PASSWORD -d master -Q "PRINT DB_ID('MalopolskaMap');"

if [ $(/opt/mssql-tools/bin/sqlcmd -I -S localhost -U SA -P $MSSQL_SA_PASSWORD -d master -Q "PRINT DB_ID('MalopolskaMap');")=" \n" ]; then

	/opt/mssql-tools/bin/sqlcmd -I -S localhost -U SA -P $MSSQL_SA_PASSWORD -d master -i /MalopolskaMap_TSQL/CreateDatabase.sql
	/opt/mssql-tools/bin/sqlcmd -I -S localhost -U SA -P $MSSQL_SA_PASSWORD -d master -i /MalopolskaMap_TSQL/CreateTables.sql
	/opt/mssql-tools/bin/sqlcmd -I -S localhost -U SA -P $MSSQL_SA_PASSWORD -d master -i /MalopolskaMap_TSQL/Functions.sql
	/opt/mssql-tools/bin/sqlcmd -I -S localhost -U SA -P $MSSQL_SA_PASSWORD -d master -i /MalopolskaMap_TSQL/LoadMaps.sql
else
	echo "Skipping. Database already exists."
fi

echo "Configuration complete."
