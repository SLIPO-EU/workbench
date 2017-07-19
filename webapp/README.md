# Slipo Workbench -- Web Application

Design and execute workflows on POI datasets.

## Quickstart

### Build

Install SASS globally to be used as CSS compiler:

    sudo gem install sass

Build the project:

    mvn clean package

Build documentation:

    mvn site

Run application:

    mvn exec:java -Dstart-class=eu.slipo.workbench.web.Application

### Deploy as standalone JAR

Deploy as a standalone JAR with an embedded server (here Tomcat 8.x). Our `pom.xml` has a packaging type of `jar`.

Run a standalone JAR:

    java -jar target/workbench-<version>.jar

During development, we usually deploy the application with `spring-boot:run`. 

    mvn spring-boot:run

### Deploy as WAR on server

Normally a WAR archive can be deployed at any servlet container. The following is only tested on a Tomcat 8.x.

Open `pom.xml` and change packaging type to `war`, in order to produce a WAR archive.

Ensure that the following section is uncommented (to avoid packaging an embedded server):

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
            <scope>provided</scope>
        </dependency>    
```

Rebuild, and deploy generated `target/workbench-<version>.war` on a Tomcat 8.x servlet container.

