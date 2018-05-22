# Slipo Workbench -- Web Application

Design and execute workflows on POI datasets.


## Quickstart

### Build

Install SASS globally to be used as CSS compiler:

    sudo gem install sass

Copy configuration examples from `config-examples/` into `/src/main/resources/config/`, and edit to adjust to your needs.

    cp config-example/* /src/main/resources/config/

Build the project:

    mvn clean package

Build documentation:

    mvn site

### Run as standalone JAR

Run application (with an embedded Tomcat 8.x server):

     java -jar target/workbench-webapp.jar

### Run as WAR on a servlet container

#### Build the WAR package

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

Rebuild, and deploy generated `target/workbench-webapp.war` on a Tomcat 8.x servlet container.

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

## Build docker image ##

We use a dedicated Ant task `prepare-docker-build` to prepare a folder `target/docker-build` holding the necessary build context for a fοllowing `docker-build` command.

Prepare build context:

    ant prepare-docker-build

Build the docker image, say `local/slipo-workbench-webapp:0.1`:

    docker build -t local/slipo-workbench-webapp:0.1 target/docker-build

Run a container based on the newly created image:

    docker run --name slipo-workbench-webapp-1 -p 8080:8080 \
       --link postgres-1:postgres-1 \
       --link slipo-workbench-rpc-server-1:rpc-server \
       --volume "$(pwd)/.secrets/db-password:/etc/secrets/db-password" \
       --volume "/mnt/nfs-1/app-data/slipo-workbench:/mnt/nfs-1/app-data/slipo-workbench" \
       -e RPC_SERVER=http://rpc-server:8080/ \
       -e DB_HOST=postgres-1 -e DB_USERNAME=slipo -e DB_PASSWORD_FILE=/etc/secrets/db-password \
       local/slipo-workbench-webapp:0.1


