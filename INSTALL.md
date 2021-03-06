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

Only a very basic familiarity with Docker containers is required (what is a container, how can i pull an image from a Docker registry).

## Install SASS

Install SASS (compiler for CSS stylesheets) required for building web application:

`sudo gem install sass`

## Fetch Docker images

Pull Docker images of SLIPO Toolkit components for RPC server:

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

Clone repository for the top-level project:

    git clone https://github.com/SLIPO-EU/workbench.git workbench
    
Enter into project and and fetch subprojects (Git submodules):

    cd workbench
    git submodule init
    git submodule update

Configure each application. See each project's README page for more details.

  * Command Line Suite: https://github.com/SLIPO-EU/workbench/tree/master/command/README.md
  * RPC Server (internal component for executing workflows): https://github.com/SLIPO-EU/workbench/tree/master/rpc-server/README.md
  * Web application: https://github.com/SLIPO-EU/workbench/tree/master/webapp/README.md

For the webapp application, a default admin account must be configured. The account username and password are set using configuration properties as shown in the example:

https://github.com/SLIPO-EU/workbench/blob/master/webapp/config-example/application.properties#L13

Build all projects:
    
    mvn clean package


## Initialize Database

Initialize database in PostgreSQL (run once):

`java -jar command/target/workbench-command.jar`

## Start SLIPO applications

Start RPC server:

`java -jar rpc-server/target/workbench-rpc-server.jar`

Start web application:

`java -jar webapp/target/workbench-webapp.jar`

Once the application has been started,  a message like “Default admin user `[admin@domain.local]` has been created.” will be written in the application log webapp.log file. Application log files are located in folder logs. If a password is not configured, the random password value will be also included in the message.




