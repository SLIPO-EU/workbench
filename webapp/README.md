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

### Deploy as WAR on servlet container

Normally a WAR archive can be deployed at any servlet container. The following is only tested on a Tomcat 8.x.

#### Build the WAR package

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

#### Provide context for JNDI resources

If JNDI datasources are to be used (say `jdbc/slipo-workbench`), then an application-specific context file should also be created to hold
a `ResourceLink` element (pointing to a global container-level `Resource`). 

Assuming the application is deployed under a context path of `/workbench`, place
a `workbench.xml` under `${CATALINA_HOME}/conf/Catalina/localhost` (or `${CATALINA_HOME}/conf/${engine_name}/${vhost}` in general):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context path="/workbench">
    <ResourceLink 
       name="jdbc/slipo-workbench" global="jdbc/slipo-workbench" type="javax.sql.DataSource" auth="Container" 
     />
</Context>
```

The global resource is a normal JNDI resource (see [Tomcat docs on JNDI resources](http://tomcat.apache.org/tomcat-8.0-doc/jndi-resources-howto.html#JDBC_Data_Sources)).
For example:
```xml
<GlobalNamingResources>
    ...
    <Resource name="jdbc/slipo-workbench" global="jdbc/slipo-workbench" auth="Container" 
        type="javax.sql.DataSource" 
        driverClassName="org.postgresql.Driver" url="jdbc:postgresql://localhost:5432/slipo-workbench" 
        username="slipo" password="slipo"/>
    ...
</GlobalNamingResources>
```

