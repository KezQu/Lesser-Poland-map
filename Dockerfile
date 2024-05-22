FROM mcr.microsoft.com/mssql/server:2022-latest

ENV MSSQL_SA_PASSWORD=Passw0rd

ENV ACCEPT_EULA=Y

USER root

RUN mkdir /malopolska
RUN mkdir /scripts

COPY ./MalopolskaMap.osm /malopolska/

COPY ./MalopolskaMapDLL/bin/Release/MalopolskaMap.dll /malopolska/

COPY ./MalopolskaMap_TSQL /MalopolskaMap_TSQL

COPY ./CreateDatabase.sh /scripts/
COPY ./LaunchServer.sh /scripts/

ENTRYPOINT [ "./scripts/LaunchServer.sh" ]