# Installation Guide

## Requirements

```
git                   > 2.7.4
maven                 > 3.3.9
java (openjdk-8-jdk)  = 1.8.0
ruby                  > 2.3.1p112
PostgreSQL            > 9.5
PostGIS               > 2.2.1
Docker                > 19.03.6
```

Install sass required for building webapp client application:

`sudo gem install sass`

## Docker Images

Pull Docker images of SLIPO Toolkit components for rpc-server:

```
docker pull athenarc/triplegeo:2.0
docker pull athenarc/reverse-triplegeo:2.0
docker pull athenarc/limes:1.7
docker pull athenarc/fagi:1.2
docker pull athenarc/fagi-partitioner:1.2
docker pull athenarc/fagi-merger:1.2
docker pull athenarc/deer:2.2
```

## Database Configuration

Create databases in PostgreSQL

```
createdb slipo
createdb slipo-test
```

Install PostGIS extensions in both databases

```
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;
```

## Build

Clone repository

`git clone https://github.com/SLIPO-EU/workbench.git workbench`

Configure each application

See each project's README page for more details

https://github.com/SLIPO-EU/workbench/tree/master/command

https://github.com/SLIPO-EU/workbench/tree/master/rpc-server

https://github.com/SLIPO-EU/workbench/tree/master/webapp

For the webapp application, a default admin account must be configured. The account username and password are set using configuration properties as shown in the example:

https://github.com/SLIPO-EU/workbench/blob/master/webapp/config-example/application.properties#L13

Build all projects:

```
cd workbench
mvn clean package
```

## Initialize Database

Initialize database in PostgreSQL (run once)

`java -jar command/target/workbench-command.jar`

## Start SLIPO applications

Start rpc-server:

`java -jar rpc-server/target/workbench-rpc-server.jar`

Start webapp:

`java -jar webapp/target/workbench-webapp.jar`

Once the application has been started,  a message like “Default admin user [admin@domain.local] has been created.” will be written in the application log webapp.log file. Application log files are located in folder logs. If a password is not configured, the random password value will be also included in the message.




