# Slipo Workbench -- Project

Design and execute workflows on POI datasets.

## Build 

Create temporary directories to be used for unit/integration tests from several modules of this project (if default locations do not suit you, edit `<module>/src/main/resources/config/application-testing.properties` accordingly):

    mkdir -p ~/var/slipo-workbench/jobs ~/var/slipo-workbench/catalog/ ~/var/slipo-workbench/workflows/ ~/var/slipo-workbench/temp

Build the project:

    mvn clean install

Prepare docker-build context directories (if planning to build docker images):

    ant -f rpc-server/build.xml prepare-docker-build
    ant -f webapp/build.xml prepare-docker-build

