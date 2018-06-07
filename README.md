# Slipo Workbench -- Project

[![Build Status](https://travis-ci.org/SLIPO-EU/workbench.svg?branch=master)](https://travis-ci.org/SLIPO-EU/workbench)

Design and execute workflows on POI datasets.

## 1. Build

## 1.1 Prepare resources

### 1.1.1 Configure modules

Each module must be configured by supplying configuration files under `<MODULE>/src/main/recources/config` directory. The documentation (along with examples) on module-specific configuration options can be found at a module's directory (see `README` and `config-examples` for each one).

### 1.1.2 Prepare vendor data

If vendor-supplied data are used (e.g. ontologies, classification schemes), they must be copied under `common/vendor` directory. After packaging `common` module, these data will be accessible as normal classpath resources (rooted under `/common/vendor`).

##### 1.1.2.1 Prepare vendor data : configuration profiles for Triplegeo 

This kind of vendor data provides configuration profiles (schema-To-RDF mappings and classifications) to be used inside a Triplegeo transformation. The data is expected to be found under the following directory hierarchy:   

   * `triplegeo/config/profiles/<PROFILE-NAME>/options.conf`: sample configuration
   * `triplegeo/config/profiles/<PROFILE-NAME>/mappings.(yml|ttl)`: schema-to-RDF mappings
   * `triplegeo/config/profiles/<PROFILE-NAME>/classification.csv`: a classification scheme

The `PROFILE-NAME` can be used to reference a mapping/classification inside Triplegeo configuration (without having to specify a filesystem path for it). 

## 1.2. Build

Create temporary directories to be used for unit/integration tests from several modules of this project (if default locations do not suit you, edit `<MODULE>/src/main/resources/config/application-testing.properties` accordingly):

    mkdir -p ~/var/slipo-workbench/jobs ~/var/slipo-workbench/catalog/ ~/var/slipo-workbench/workflows/ ~/var/slipo-workbench/temp

Build the project:

    mvn clean install

Prepare docker-build context directories (if planning to build docker images):

    ant -f rpc-server/build.xml prepare-docker-build
    ant -f webapp/build.xml prepare-docker-build
